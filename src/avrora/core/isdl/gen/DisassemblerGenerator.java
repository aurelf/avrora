package avrora.core.isdl.gen;

import avrora.core.isdl.Architecture;
import avrora.core.isdl.InstrDecl;
import avrora.core.isdl.EncodingDecl;
import avrora.core.isdl.OperandDecl;
import avrora.core.isdl.ast.Expr;
import avrora.core.isdl.ast.Literal;
import avrora.util.Terminal;
import avrora.util.StringUtil;
import avrora.util.Arithmetic;
import avrora.util.Printer;
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

    Printer printer = new Printer(System.out);

    class EncodingInfo {
        final InstrDecl instr;
        final byte[] bitStates;
        final List exprs;

        EncodingInfo(InstrDecl id) {
            instr = id;
            bitStates = new byte[id.getEncodingSize()];
            exprs = new LinkedList();

            initializeBitStates(id);
        }

        private void initializeBitStates(InstrDecl id) {


            EncodingDecl ed = id.encoding;
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
            int bitNum = 0;
            while ( i1.hasNext() ) {
                // get the expression of the parent encoding
                Expr e = (Expr)i1.next();
                // evaluate the parent encoding expression, given values for operands
                Expr e1 = e.accept(cp,ce);
                // store the expression for future use
                exprs.add(e1);
                // get the bit width of the parent encoding field
                int size = e.getBitWidth();

                // if this field corresponds to an integer literal, initialize each bit to
                // either ENC_ZERO or ENC_ONE
                if ( e1 instanceof Literal.IntExpr ) {
                    Literal.IntExpr l = (Literal.IntExpr)e1;
                    for ( int cntr = 0; cntr < size; cntr++) {
                        boolean bit = Arithmetic.getBit(l.value, size-cntr-1);
                        bitStates[bitNum++] = bit ? ENC_ONE : ENC_ZERO;
                    }
                } else if (e1 instanceof Literal.BoolExpr) {
                    // if it is a boolean literal, initialize one bit
                    Literal.BoolExpr l = (Literal.BoolExpr)e1;
                    bitStates[bitNum++] = l.value ? ENC_ONE : ENC_ZERO;
                } else {
                    // not a known value; initialize each bit to variable
                    for ( int cntr = 0; cntr < size; cntr++) {
                        bitStates[bitNum++] = ENC_VAR;
                    }
                }
            }
            print();
        }

        void print() {
            printer.print(StringUtil.leftJustify(instr.name.toString(), 8)+": ");
            for ( int cntr = 0; cntr < bitStates.length; cntr++ ) {
                switch ( bitStates[cntr] ) {
                    case ENC_ZERO:
                        printer.print("0");
                        break;
                    case ENC_ONE:
                        printer.print("1");
                        break;
                    case ENC_USED_ONE:
                        printer.print("U");
                        break;
                    case ENC_USED_ZERO:
                        printer.print("u");
                        break;
                    case ENC_VAR:
                        printer.print(".");
                        break;
                }
            }
            printer.nextln();
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
                printer.println("scanning...[empty]");
                return;
            } else if ( encodings.size() == 1 ) {
                // this encoding set has only one member, meaning that it is a leaf and needs no further
                // children.
                Iterator i = encodings.iterator();
                EncodingInfo ei = (EncodingInfo)i.next();
                printer.println("singleton: ");
                ei.print();
                return;
            }

            // scan for the leftmost concrete bit range common to all encodings in this set.
            Iterator i = encodings.iterator();
            printer.println("scanning...");
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
                            value = value << 1 | 1;
                            ei.bitStates[cntr] = ENC_USED_ZERO;
                            break;
                        case ENC_ONE:
                            value = value << 1;
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

            // recursively generate code for each of the children
            Iterator i = children.values().iterator();
            while ( i.hasNext() ) {
                EncodingSet es = (EncodingSet)i.next();
                es.generateCode();
            }
        }

        private void generateSwitch() {
            int high_bit = 16-left_bit;
            int low_bit = 16-right_bit;
            int mask = Arithmetic.getBitRangeMask(low_bit, high_bit);
            printer.println("// get value of bits word["+left_bit+":"+right_bit+"]");
            printer.println("int value = (word1 >> "+low_bit+") & 0x"+StringUtil.toHex(mask, 5)+";");
            printer.startblock("switch ( value )");

            Iterator i = children.keySet().iterator();
            // generate a case for each value of the bits in this test.
            while ( i.hasNext() ) {
                Integer value = (Integer)i.next();
                int val = value.intValue();
                printer.print("case "+StringUtil.toHex(val, 5)+": ");
                EncodingSet child = (EncodingSet)children.get(value);
                String mname = "decode_"+(methods++);
                child.methodname = mname;
                printer.println("return "+mname+"(word1);");
            }

            printer.println("default:");
            invalidInstr();
            printer.endblock();
        }

        private void invalidInstr() {
            printer.println("throw Avrora.failure(\"INVALID INSTRUCTION\");");
        }

        private void generateLeaf() {
            boolean check = false;
            int mask = 0;
            int value = 0;
            // TODO: double check bit ordering for this test.
            // first check for any left over concrete bits that must match
            EncodingInfo ei = (EncodingInfo)encodings.iterator().next();

            printer.println("// this method matches and decodes the "+ei.instr.name+" instruction");

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
        }
    }

    int methods;

    EncodingSet rootSet;
    HashSet pseudo;

    Architecture architecture;

    public DisassemblerGenerator(Architecture a) {
        rootSet = new EncodingSet();
        pseudo = new HashSet();
        rootSet.methodname = "decode_root";
        architecture = a;
    }


    public void visit(InstrDecl d) {
        // for now, we ignore pseudo instructions.
        EncodingInfo ei = new EncodingInfo(d);
        if ( d.pseudo ) {
            pseudo.add(ei);
        } else {
            rootSet.encodings.add(ei);
        }
    }

    public void compute() {
        rootSet.compute();
        generateDecodeTables();
        rootSet.generateCode();
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

}
