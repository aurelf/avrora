/**
 * Copyright (c) 2004-2005, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package avrora.core.isdl.gen;

import avrora.core.isdl.*;
import avrora.core.isdl.ast.*;
import avrora.util.*;
import avrora.Avrora;

import java.util.*;

/**
 * @author Ben L. Titzer
 */
public class DisassemblerGenerator implements Architecture.InstrVisitor {

    private static final byte ENC_ONE  = 1;
    private static final byte ENC_ZERO = 2;
    private static final byte ENC_USED_ONE = 3;
    private static final byte ENC_USED_ZERO = 4;
    private static final byte ENC_VAR  = 0;

    protected static final int LARGEST_INSTR = 15;
    protected static final int WORD_SIZE = 16;

    final Printer printer;
    Verbose.Printer verbose = Verbose.getVerbosePrinter("isdl.disassem");

    class EncodingField extends CodeVisitor.Default {
        final EncodingInfo ei;
        final int bitsize;
        final Expr expr;
        final int offset;

        EncodingField(EncodingInfo ei, int o, int s, Expr e) {
            this.ei = ei;
            offset = o;
            expr = e;
            bitsize = s;
        }

        void generateDecoder() {
            printer.print("// logical["+offset+":"+(offset+bitsize-1)+"] -> ");
            expr.accept(this);
        }

        public void visit(VarExpr ve) {
            printer.println(ve.variable.toString());
            printer.println(ve.variable+" = ");
            generateRead(offset, offset+bitsize-1);
            printer.println(";");
        }

        public void visit(BitExpr bre) {
            if ( !bre.expr.isVariable() ) {
                throw Avrora.failure("bit range use not invertible: value is not a variable or constant");
            } else if ( !bre.bit.isLiteral() ) {
                throw Avrora.failure("bit range use not invertible: bit is not a constant");
            }
            VarExpr ve = (VarExpr)bre.expr;
            int bit = ((Literal.IntExpr)bre.bit).value;
            printer.println(ve.variable+"["+bit+"]");
            printer.println(ve.variable+" = Arithmetic.setBit("+ve.variable+", "+bit+", Arithmetic.getBit(word1, "+nativeBitOrder(offset)+"));");
        }

        public void visit(BitRangeExpr bre) {
            if ( bre.operand.isVariable() ) {
                VarExpr ve = (VarExpr)bre.operand;
                printer.println(ve.variable+"["+bre.high_bit+":"+bre.low_bit+"]");
                printer.print(ve.variable+" |= ");
                generateRead(offset, offset+bitsize-1);
                if ( bre.low_bit > 0)
                    printer.println(" << "+bre.low_bit+";");
                else printer.println(";");
            } else {
                throw Avrora.failure("bit range use not invertible");
            }
        }

        public void visit(Literal.IntExpr e) {
            printer.nextln();
            // do nothing for literals.
        }

        public void visit(Literal.BoolExpr e) {
            printer.nextln();
            // do nothing for literals.
        }

        public void error(Expr e) {
            // this method is called when the expression does not match any of the overridden methods
            throw Avrora.failure("expression not invertible");
        }
    }

    class EncodingInfo {
        final InstrDecl instr;
        final EncodingDecl encoding;
        final int encodingNumber;
        final byte[] bitStates;
        final List simplifiedExprs;

        EncodingInfo(InstrDecl id, int encNum, EncodingDecl ed) {
            instr = id;
            bitStates = new byte[id.getEncodingSize()];
            simplifiedExprs = new LinkedList();
            encodingNumber = encNum;
            encoding = ed;

            initializeBitStates(id);
        }

        String getName() {
            return instr.innerClassName+"_"+encodingNumber;
        }

        private void initializeBitStates(InstrDecl id) {
            EncodingDecl ed = encoding;
            Iterator i1; // iterator over expressions in the encoding

            // create a constant propagator needed to evaluate integer literals and operands
            ConstantPropagator cp = new ConstantPropagator();
            ConstantPropagator.ConstantEnvironment ce = cp.createEnvironment();

            if ( ed instanceof EncodingDecl.Derived ) {
                EncodingDecl.Derived dd = (EncodingDecl.Derived)ed;
                i1 = dd.parent.fields.iterator();

                // put all the substitutions into the map
                Iterator si = dd.subst.iterator();
                while ( si.hasNext() ) {
                    EncodingDecl.Substitution s = (EncodingDecl.Substitution)si.next();
                    ce.put(s.name.toString(), s.expr);
                }
            } else {
                i1 = ed.fields.iterator();
            }

            // scan through the expressions corresponding to the fields that make up this encoding
            // and initialize the bitState array to either ENC_ONE, ENC_ZERO, or ENC_VAR
            int offset = 0;
            while ( i1.hasNext() ) {
                // get the expression of the parent encoding
                Expr e = (Expr)i1.next();
                // get the bit width of the parent encoding field
                int size = e.getBitWidth();


                int endbit = offset + size - 1;
                if ( (offset / WORD_SIZE) != (endbit / WORD_SIZE) ) {
                    // this field spans a word boundary; we will need to split it up
                    int f_offset = offset;
                    int h_bit = size - 1;
                    while ( f_offset < endbit ) {
                        int bits = WORD_SIZE - (f_offset % WORD_SIZE);
                        if ( bits > WORD_SIZE ) bits = WORD_SIZE;
                        // evaluate the expression with a smaller bit interval
                        Expr simpleExpr = eval(e, cp, ce, h_bit, h_bit-bits+1);
                        addExpr(f_offset, bits, simpleExpr);

                        f_offset += bits;
                        h_bit -= bits;
                    }
                } else {
                    // evaluate the parent encoding expression, given values for operands
                    Expr simpleExpr = e.accept(cp,ce);

                    addExpr(offset, size, simpleExpr);
                }

                offset += size;
            }
            print();
        }

        private void addExpr(int offset, int size, Expr simpleExpr) {
            // store the expression for future use
            EncodingField ee = new EncodingField(this, offset, size, simpleExpr);
            simplifiedExprs.add(ee);

            setBitStates(simpleExpr, size, offset);
        }

        Expr eval(Expr e, ConstantPropagator cp, ConstantPropagator.ConstantEnvironment ce, int h_bit, int l_bit) {
            if ( e.isBitRangeExpr() ) {
                BitRangeExpr orig = (BitRangeExpr)e;
                int nmax = h_bit + orig.low_bit;
                if ( orig.high_bit - orig.low_bit < h_bit - l_bit )
                    nmax = orig.high_bit;
                int nmin = l_bit + orig.low_bit;
                e = new BitRangeExpr(orig.operand, nmin, nmax);
            }

            return e.accept(cp, ce);
        }

        private void setBitStates(Expr simpleExpr, int size, int offset) {
            // if this field corresponds to an integer literal, initialize each bit to
            // either ENC_ZERO or ENC_ONE
            if ( simpleExpr instanceof Literal.IntExpr ) {
                Literal.IntExpr l = (Literal.IntExpr)simpleExpr;
                for ( int cntr = 0; cntr < size; cntr++) {
                    boolean bit = Arithmetic.getBit(l.value, size-cntr-1);
                    bitStates[offset++] = bit ? ENC_ONE : ENC_ZERO;
                }
            } else if (simpleExpr instanceof Literal.BoolExpr) {
                // if it is a boolean literal, initialize one bit
                Literal.BoolExpr l = (Literal.BoolExpr)simpleExpr;
                bitStates[offset++] = l.value ? ENC_ONE : ENC_ZERO;
            } else {
                // not a known value; initialize each bit to variable
                for ( int cntr = 0; cntr < size; cntr++) {
                    bitStates[offset++] = ENC_VAR;
                }
            }
        }

        void print() {
            if ( !verbose.enabled ) return;
            verbose.print(StringUtil.leftJustify(instr.name.toString(), 8)+": ");
            for ( int cntr = 0; cntr < bitStates.length; cntr++ ) {
                switch ( bitStates[cntr] ) {
                    case ENC_ZERO:
                        verbose.print("0");
                        break;
                    case ENC_ONE:
                        verbose.print("1");
                        break;
                    case ENC_USED_ONE:
                        verbose.print("U");
                        break;
                    case ENC_USED_ZERO:
                        verbose.print("u");
                        break;
                    case ENC_VAR:
                        verbose.print(".");
                        break;
                }
            }
            verbose.nextln();
        }
    }

    class EncodingSet {
        HashSet encodings = new HashSet();
        String methodname;
        int left_bit;
        int right_bit = LARGEST_INSTR;
        int value;
        int depth;

        HashMap children = new HashMap();

        void computeRange() {
            if ( encodings.size() == 0) {
                // this should not happen. how is it possible to create a new encoding set with no members?
                verbose.println("scanning...[empty]");
                return;
            } else if ( encodings.size() == 1 ) {
                // this encoding set has only one member, meaning that it is a leaf and needs no further
                // children.
                Iterator i = encodings.iterator();
                EncodingInfo ei = (EncodingInfo)i.next();
                verbose.println("singleton: ");
                ei.print();
                return;
            }

            // scan for the leftmost concrete bit range common to all encodings in this set.
            Iterator i = encodings.iterator();
            verbose.println("scanning...");
            while ( i.hasNext() ) {
                EncodingInfo ei = (EncodingInfo)i.next();
                ei.print();

                int lb = scanForLeftBit(ei);
                if ( lb >= ei.bitStates.length ) {
                    // there are no concrete bits in this encoding!
                    // It cannot be disambiguated from the other members of the set!
                    throw Avrora.failure("cannot disambiguate "+ei.instr.name.toString()+" at depth "+depth);
                }

                int rb = scanForRightBit(lb, ei);

                if ( lb > rb ) {
                    // there is no common bit among all of the instructions of this set!
                    // there is an ambiguity that needs to be resolved.
                    throw Avrora.failure("cannot disambiguate at depth "+depth);
                }

                left_bit = lb;
                right_bit = rb;
            }
        }

        private int scanForRightBit(int lb, EncodingInfo ei) {
            int rb = right_bit;
            // scan from the left_bit (known to be concrete) to the first unknown bit
            // move right_bit if necessary
            for ( int cntr = lb; cntr <= rb; cntr++ ) {
                byte bitState = ei.bitStates[cntr];
                if ( bitState != ENC_ZERO && bitState != ENC_ONE ) {
                    rb = cntr-1;
                    break;
                }
            }
            return rb;
        }

        private int scanForLeftBit(EncodingInfo ei) {
            int lb = left_bit;
            // start at left bit and scan until a concrete bit is found
            while ( lb < ei.bitStates.length ) {
                byte bitState = ei.bitStates[lb];
                if ( bitState == ENC_ZERO ) break;
                if ( bitState == ENC_ONE ) break;
                lb++;
            }
            return lb;
        }

        void createChildren() {
            if ( encodings.size() <= 1) return;
            Iterator i = encodings.iterator();
            // iterate through the bit states of this encoding for this bit range
            // set the bit states to either MATCHED_ONE or MATCHED_ZERO
            while ( i.hasNext() ) {
                EncodingInfo ei = (EncodingInfo)i.next();
                int value = 0;
                for ( int cntr = left_bit; cntr <= right_bit; cntr++ ) {
                    byte bitState = ei.bitStates[cntr];
                    switch (bitState) {
                        case ENC_ZERO:
                            value = value << 1;
                            ei.bitStates[cntr] = ENC_USED_ZERO;
                            break;
                        case ENC_ONE:
                            value = value << 1 | 1;
                            ei.bitStates[cntr] = ENC_USED_ONE;
                            break;
                        default:
                            throw Avrora.failure("invalid bit state at "+cntr+" in "+ei.instr.name);
                    }
                }

                // add the instruction to the encoding set corresponding to the value of
                // the bits in this range
                Integer iv = new Integer(value);
                EncodingSet es = (EncodingSet)children.get(iv);
                if ( es == null ) {
                    es = new EncodingSet();
                    es.depth = depth+1;
                    children.put(iv, es);
                }

                es.encodings.add(ei);
            }
        }

        void compute() {
            printer.indent();
            computeRange();
            createChildren();
            recurse();
            printer.unindent();
        }

        void recurse() {
            Iterator i = children.values().iterator();
            while ( i.hasNext() ) {
                EncodingSet es = (EncodingSet)i.next();
                es.compute();
            }
        }

        void generateCode() {
            // recursively generate code for each of the children
            Iterator i = children.values().iterator();
            while ( i.hasNext() ) {
                EncodingSet es = (EncodingSet)i.next();
                es.generateCode();
            }

            if ( methodname == null ) {
                if ( children.size() > 0)
                    methodname = "decode_"+(methods++);
                else {
                    EncodingInfo ei = (EncodingInfo)encodings.iterator().next();
                    methodname = "decode_"+ei.getName();
                }
            }

            printer.startblock("private Instr "+methodname+"(int word1)");

            if ( children.size() > 0 ) {
                // if there are any children, we need to generate a switch statement over
                // the possible values of this bit range
                generateSwitch();
            } else {
                // this encoding set has no children; it therefore decodes to one and only
                // one instruction.
                generateLeaf();
            }
            printer.endblock();
        }

        private void generateSwitch() {
            int high_bit = nativeBitOrder(left_bit);
            int low_bit = nativeBitOrder(right_bit);
            int mask = Arithmetic.getBitRangeMask(low_bit, high_bit);
            printer.println("// get value of bits logical["+left_bit+":"+right_bit+"]");
            printer.println("int value = (word1 >> "+low_bit+") & 0x"+StringUtil.toHex(mask, 5)+";");
            printer.startblock("switch ( value )");

            Iterator i = children.keySet().iterator();
            // generate a case for each value of the bits in this test.
            while ( i.hasNext() ) {
                Integer value = (Integer)i.next();
                int val = value.intValue();
                printer.print("case 0x"+StringUtil.toHex(val, 5)+": ");
                EncodingSet child = (EncodingSet)children.get(value);
                printer.println("return "+child.methodname+"(word1);");
            }

            printer.println("default:");
            invalidInstr();
            printer.endblock();
        }

        private void generateLeaf() {
            boolean check = false;
            int mask = 0;
            int value = 0;
            // first check for any left over concrete bits that must match
            EncodingInfo ei = (EncodingInfo)encodings.iterator().next();

            if ( ei.encoding.isConditional() ) {
                EncodingDecl.Cond c = ei.encoding.getCond();
                printer.println("// this method decodes "+ei.instr.innerClassName+" when "+c.name+" == "+c.expr);
            }

            // go through each of the bits in the bit states. if any of the bits have
            // not been matched yet, then they need to be checked to make sure that
            // they match.
            for ( int cntr = 0; cntr < ei.bitStates.length; cntr++ ) {
                byte bitState = ei.bitStates[cntr];
                if ( bitState == ENC_ZERO ) {
                    check = true;
                    value = value << 1;
                    mask = mask << 1 | 1;
                } else if ( bitState == ENC_ONE ) {
                    check = true;
                    value = value << 1 | 1;
                    mask = mask << 1 | 1;
                } else {
                    value = value << 1;
                    mask = mask << 1;
                }
            }

            if ( check ) {
                // generate a check on the left over bits to verify they match this encoding.
                printer.startblock("if ( (word1 & 0x"+StringUtil.toHex(mask, 5)+") != 0x"+StringUtil.toHex(value, 5)+" )");
                invalidInstr();
                printer.endblock();
            }

            // declare each operand
            declareOperands(ei);

            // generate the code that reads the operands from the instruction encoding
            generateDecodeStatements(ei);

            // generate the call to the Instr class constructor
            generateConstructorCall(ei);
        }

        private void generateDecodeStatements(EncodingInfo ei) {
            Iterator i = ei.simplifiedExprs.iterator();
            while ( i.hasNext() ) {
                EncodingField e = (EncodingField)i.next();
                e.generateDecoder();
            }
        }

        private void generateConstructorCall(EncodingInfo ei) {
            printer.print("return new "+ei.instr.getClassName()+"(pc");
            Iterator i1 = ei.instr.getOperandIterator();
            while ( i1.hasNext() ) {
                CodeRegion.Operand o = (CodeRegion.Operand)i1.next();
                printer.print(", ");
                String getexpr = getValue(ei, o);
                printer.print(getexpr);
            }
            printer.println(");");
        }

        private void declareOperands(EncodingInfo ei) {
            int size = ei.bitStates.length / 8;
            for ( int cntr = 2; size > 2; cntr += 2, size -= 2 ) {
                int wordnum = (cntr / 2) + 1;
                if ( size > 3 )
                    printer.println("int word"+wordnum+" = getWord("+(wordnum-1)+");");
                else
                    printer.println("int word"+wordnum+" = getByte("+(wordnum-1)+");");
            }
            Iterator i = ei.instr.getOperandIterator();
            while ( i.hasNext() ) {
                CodeRegion.Operand o = (CodeRegion.Operand)i.next();
                if ( !isFixed(ei, o) )
                    printer.println("int "+o.name+" = 0;");
            }
        }

        private boolean isFixed(EncodingInfo ei, CodeRegion.Operand o) {
            if ( ei.encoding.isConditional() ) {
                EncodingDecl.Cond c = ei.encoding.getCond();
                if ( o.name.image.equals(c.name.image) )
                    return true;
            }
            // if this is a register, we have to look it up in the table
            return false;
        }

        private String getValue(EncodingInfo ei, CodeRegion.Operand o) {
            if ( ei.encoding.isConditional() ) {
                String prefix = o.isRegister() ? "Register." : "";
                EncodingDecl.Cond c = ei.encoding.getCond();
                if ( o.name.image.equals(c.name.image) )
                    return prefix+c.expr.toString();
            }
            // if this is a register, we have to look it up in the table
            if ( o.isRegister() )
                return "getReg("+o.type+"_table, "+o.name+")";
            else return o.name.image;
        }
    }

    int methods;

    EncodingSet rootSet;
    HashSet pseudo;

    Architecture architecture;

    public DisassemblerGenerator(Architecture a, Printer p) {
        rootSet = new EncodingSet();
        pseudo = new HashSet();
        rootSet.methodname = "decode_root";
        architecture = a;
        printer = p;
    }

    public void generate() {
        architecture.accept(this);
        rootSet.compute();
        printer.indent();
        generateDecodeTables();
        rootSet.generateCode();
        printer.unindent();
    }

    public void visit(InstrDecl d) {
        // for now, we ignore pseudo instructions.
        Iterator i = d.encodingList.iterator();
        for ( int cntr = 0; i.hasNext(); cntr++) {
            EncodingDecl ed = (EncodingDecl)i.next();
            EncodingInfo ei = new EncodingInfo(d, cntr, ed);
            if ( d.pseudo ) {
                pseudo.add(ei);
            } else {
                rootSet.encodings.add(ei);
            }
        }
    }

    private void invalidInstr() {
        printer.println("throw Avrora.failure(\"INVALID INSTRUCTION\");");
    }

    private void generateDecodeTables() {
        architecture.accept(new OperandDeclVisitor());
    }

    class OperandDeclVisitor implements Architecture.OperandVisitor {
        public void visit(OperandDecl od) {
            // if the operand is a register set declaration, then we need to
            // generate a decoding table
            if ( od.isRegister() )
                generateRegisterTable(od);
        }

        private void generateRegisterTable(OperandDecl od) {
            int tablesize = 1 << od.bitSize;
            String register[] = new String[tablesize];
            OperandDecl.RegisterSet rs = (OperandDecl.RegisterSet)od;
            // for each member in the operand declaration set, store the name of the register
            // corresponding to the value of the binary encoding
            Iterator i = rs.members.iterator();
            while ( i.hasNext() ) {
                OperandDecl.RegisterEncoding re = (OperandDecl.RegisterEncoding)i.next();
                if ( register[re.value] != null )
                    throw Avrora.failure("AMBIGUOUS REGISTER SET ENCODING");
                register[re.value] = re.name.toString();
            }

            // generate an array of register references that is indexed by the value
            // of the operand from the encoding
            String tablename = od.name+"_table";
            printer.startblock("static final Register[] "+tablename+" =");
            for ( int cntr = 0; cntr < register.length; cntr++ ) {
                if ( register[cntr] == null ) printer.print("null");
                else printer.print("Register."+register[cntr].toUpperCase());
                if ( cntr != register.length - 1) printer.print(", ");
                printer.nextln();
            }
            printer.endblock(";");
            printer.nextln();
        }
    }

    private void generateRead(int left_bit, int right_bit) {
        int high_bit = nativeBitOrder(left_bit);
        int low_bit = nativeBitOrder(right_bit);
        int mask = Arithmetic.getBitRangeMask(low_bit, high_bit);

        int word = 1 + (left_bit / WORD_SIZE);

        if ( low_bit > 0 )
            printer.print("((word"+word+" >> "+low_bit+") & 0x"+StringUtil.toHex(mask, 5)+")");
        else
            printer.print("(word"+word+" & 0x"+StringUtil.toHex(mask, 5)+")");
    }

    private int nativeBitOrder(int bit) {
        return 15-(bit % WORD_SIZE);
    }
}
