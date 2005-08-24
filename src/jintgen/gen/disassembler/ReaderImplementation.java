/**
 * Copyright (c) 2005, Regents of the University of California
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

package jintgen.gen.disassembler;

import jintgen.isdl.EncodingDecl;
import jintgen.isdl.AddrModeDecl;
import jintgen.isdl.OperandTypeDecl;
import jintgen.isdl.EnumDecl;
import jintgen.jigir.*;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

import avrora.util.StringUtil;
import avrora.util.Util;
import avrora.util.Printer;

/**
 * The <code>ReaderImplementation</code> class implements the functionality needed to generate
 * the code that reads various bit patterns from the instruction stream, as well as generate
 * classes in the disassembler that can read the operands and package them into an array.
 *
 * @author Ben L. Titzer
 */
class ReaderImplementation extends DGenerator {

    HashMap<EncodingDecl, EncodingReader> encodingInfo = new HashMap<EncodingDecl, EncodingReader>();
    HashMap<String, EncodingReader> encodingRev = new HashMap<String, EncodingReader>();
    HashMap<String, ReadMethod> operandDecodeMethods = new HashMap<String, ReadMethod>();

    int maxoperands;
    int readMethods;

    public ReaderImplementation(DisassemblerGenerator disassemblerGenerator) {
        super(disassemblerGenerator);
    }

    class ReadMethod {
        final int number;
        final int[] decode_bits;
        final List<Field> fields;

        ReadMethod(int[] decode) {
            number = readMethods++;
            decode_bits = decode;
            fields = computeFieldList(decode);
        }

        private List<Field> computeFieldList(int[] decode) {
            List<Field> list = new LinkedList<Field>();
            Field f = null;
            for ( int bit = 0; bit < decode.length; bit++ ) {
                if ( f == null || startNewField(f, decode, bit) ) {
                    f = new Field();
                    f.low_bit = decode[bit];
                    f.length = 1;
                    list.add(f);
                } else {
                    f.length++;
                }
            }
            return list;
        }

        private boolean startNewField(Field f, int[] decode, int bit) {
            boolean startNewWord = (decode[bit] % DisassemblerGenerator.WORD_SIZE == 0);
            boolean notContiguous = (decode[bit] != f.low_bit + f.length);
            return startNewWord || notContiguous;
        }

        public String toString() {
            return "readop_"+number+"(d)";
        }

        void generate() {
            p.startblock(tr("static int readop_$1($disassembler d)", number));
            if ( fields.size() == 0 ) {
                p.println("return 0;");
            } else {
                int offset = 0;
                for ( Field f : fields ) {
                    if ( offset == 0 ) p.print("int result = ");
                    else p.print("result |= ");
                    int word = f.low_bit / DisassemblerGenerator.WORD_SIZE;
                    int off = f.low_bit % DisassemblerGenerator.WORD_SIZE;
                    int mask = ((1 << f.length)-1);
                    if ( off == 0 )
                        p.print("(d.word"+word+" & ");
                    else
                        p.print("((d.word"+word+" >>> "+off+") & ");
                    p.print(StringUtil.to0xHex(mask, DisassemblerGenerator.WORD_SIZE / 4));
                    if ( offset == 0 ) p.print(");");
                    else p.print(") << "+offset+";");
                    offset += f.length;
                    p.nextln();
                }
                p.println("return result;");
            }
            p.endblock();
        }
    }

    class Field {
        int low_bit;
        int length;
    }

    void generateReads() {
        for ( ReadMethod m : operandDecodeMethods.values() ) m.generate();
    }

    void addEncoding(String eName, EncodingDecl ed, AddrModeDecl am) {
        int no = am.operands.size();
        if ( no > maxoperands ) maxoperands = no;
        if ( !encodingInfo.containsKey(ed) ) {
            EncodingReader er = new EncodingReader();
            er.decl = ed;
            er.name = eName;
            er.addrMode = am;
            encodingInfo.put(ed, er);
            encodingRev.put(eName, er);
            dGen.numEncodings++;
        }
        int bitWidth = ed.getBitWidth();
        if ( bitWidth < dGen.minInstrLength )
            dGen.minInstrLength = bitWidth;
        if ( bitWidth > dGen.maxInstrLength )
            dGen.maxInstrLength = bitWidth;
    }

    String getName(EncodingDecl ed) {
        return encodingInfo.get(ed).name;
    }

    ReadMethod getReadMethod(int[] d) {
        String dc = getDecoderString(d);
        ReadMethod rm = operandDecodeMethods.get(dc);
        if ( rm != null ) return rm;
        rm = new ReadMethod(d);
        operandDecodeMethods.put(dc, rm);
        return rm;
    }

    String getDecoderString(int[] decoder) {
        StringBuffer buf = new StringBuffer();
        for ( int i : decoder ) {
            buf.append(i);
            buf.append('.');
        }
        return buf.toString();
    }

    class EncodingReader {
        String name;
        EncodingDecl decl;
        AddrModeDecl addrMode;
        HashMap<AddrModeDecl.Operand, String> operandDecodeString = new HashMap<AddrModeDecl.Operand, String>();

        void computeDecoders() {
            computeDecoders("", addrMode.operands);
        }

        void computeDecoders(String prefix, Iterable<AddrModeDecl.Operand> operands) {
            List<EncodingDecl.BitField> nl = DGUtil.reduceEncoding(decl, null, addrMode);
            for ( AddrModeDecl.Operand o : operands ) {
                OperandTypeDecl ot = dGen.arch.getOperandDecl(o.type.image);
                EncodingDecl.Cond cond = decl.getCond();
                String opname = prefix+o.name.image;
                if ( cond != null && cond.name.image.equals(opname) ) {
                    String et = getEnumType(ot);
                    if ( et != null )
                        operandDecodeString.put(o, DisassemblerGenerator.symbolClassName+"."+et+"."+cond.expr.toString());
                    else
                        operandDecodeString.put(o, cond.expr.toString());
                    continue;
                }
                if ( ot.isValue() ) {
                    String et = getEnumType(ot);
                    int[] decoder = computeScatter(opname, (OperandTypeDecl.Value)ot, nl);
                    ReadMethod rm = getReadMethod(decoder);
                    if ( et != null )
                        operandDecodeString.put(o, et+"_table["+rm.toString()+"]");
                    else
                        operandDecodeString.put(o, rm.toString());
                } else if ( ot.isCompound() ) {
                    computeDecoders(prefix+opname+".", ot.subOperands);
                }
            }
        }

        String getEnumType(OperandTypeDecl ot) {
            if ( ot.isValue() ) {
                OperandTypeDecl.Value vt = (OperandTypeDecl.Value)ot;
                EnumDecl ed = dGen.arch.getEnum(vt.kind.image);
                if ( ed != null ) return ed.name.image;
                return null;
            }
            return null;
        }

        int[] computeScatter(String name, OperandTypeDecl.Value vd, List<EncodingDecl.BitField> fs) {
            int bit = 0;
            int[] result = new int[vd.size];
            Arrays.fill(result, -1);
            for ( EncodingDecl.BitField f : fs) {
                visitExpr(f, name, bit, result);
                bit += f.getWidth();
            }
            for ( int cntr = 0; cntr < result.length; cntr++ ) {
                if ( result[cntr] < 0 )
                    throw Util.failure("bit "+cntr+" of operand "+StringUtil.quote(name)+" in encoding "+
                            decl.name+" at "+DGUtil.pos(decl.name)+" is not present in the encoding");
            }
            return result;
        }

        private void visitExpr(EncodingDecl.BitField f, String name, int bit, int[] result) {
            Expr e = f.field;
            if ( matches(e, name) ) {
                for ( int cntr = 0; cntr < f.getWidth(); cntr++ ) result[cntr] = cntr + bit;
            } else if ( e.isBitRangeExpr() ) {
                BitRangeExpr bre = (BitRangeExpr)e;
                if ( matches(bre.operand, name) ) {
                    for ( int cntr = 0; cntr < f.getWidth(); cntr++ ) {
                        int indx = cntr+bre.low_bit;
                         // we don't care about bits beyond the end of our declared operand
                        if ( indx < result.length )
                            result[indx] = cntr + bit;
                    }
                }
            } else if ( e instanceof BitExpr ) {
                BitExpr be = (BitExpr)e;
                if ( matches(be.expr, name) && be.bit.isLiteral() ) {
                    int value = ((Literal.IntExpr)be.bit).value;
                    result[value] = bit;
                }
            }
        }

        private boolean matches(Expr e, String name) {
            if ( e instanceof VarExpr ) {
                VarExpr ve = (VarExpr)e;
                return name.equals(ve.variable.image);
            } else if ( e instanceof DotExpr ) {
                DotExpr de = (DotExpr)e;
                return name.equals(de.operand+"."+de.field);
            }
            return false;
        }

        void generateReader() {
            dGen.properties.setProperty("reader", name);
            p.startblock(tr("static class $reader_reader extends OperandReader"));
            p.startblock(tr("$operand[] read($disassembler d)"));
            int size = addrMode.operands.size();
            for ( AddrModeDecl.Operand o : addrMode.operands )
                generateOperandRead("", o);
            if ( addrMode.operands.size() == 0 )
                p.print("return d.OPERANDS_0;");
            else {
                p.beginList(tr("return d.fill_$1(", size));
                for ( AddrModeDecl.Operand o : addrMode.operands ) {
                    p.print(o.name.image);
                }
                p.endList(");");
            }
            p.nextln();
            p.endblock();
            p.endblock();
        }

        void generateOperandRead(String prefix, AddrModeDecl.Operand o) {
            String oname = o.name.image;
            String vname = prefix+oname;
            String vn = vname.replace('.', '_');
            OperandTypeDecl td = dGen.arch.getOperandDecl(o.type.image);
            if ( td.isCompound() ) {
                generateCompound(td, vname);
            } else if ( td.isValue() ) {
                OperandTypeDecl.Value vtd = (OperandTypeDecl.Value)td;
                // not a conditional encoding; load bits and generate tables
                String type = DisassemblerGenerator.operandClassName+"."+vtd.name;
                p.print(type+" "+vn +" = new "+type+"(");
                generateRawRead(o);
                p.println(");");
            }
        }

        private void generateCompound(OperandTypeDecl td, String vname) {
            for ( AddrModeDecl.Operand so : td.subOperands )
                generateOperandRead(vname+".", so);
            String vn = vname.replace('.', '_');
            String type = DisassemblerGenerator.operandClassName+"."+td.name;
            p.beginList(type+" "+vn+" = new "+type+"(");
            for ( AddrModeDecl.Operand so : td.subOperands ) {
                p.print(vname+"_"+so.name);
            }
            p.endList(");");
            p.nextln();
        }

        private void generateRawRead(AddrModeDecl.Operand o) {
            String str = operandDecodeString.get(o);
            assert str != null;
            p.print(str);
        }
    }

    public void generateOperandReaders() {

        for ( EncodingReader r : encodingInfo.values() ) {
            r.computeDecoders();
        }

        generateReads();

        for ( EncodingReader er : encodingInfo.values() )
            er.generateReader();

        for ( int cntr = 0; cntr <= maxoperands; cntr++ ) {
            //String str = "final "+DisassemblerGenerator.operandClassName+"[] OPERANDS_"+cntr+" = new "+DisassemblerGenerator.operandClassName+"["+cntr+"];";
            p.println(tr("final $operand[] OPERANDS_$1 = new $operand[$1];", cntr));
        }

        generateFills(maxoperands);
    }

    private void generateFills(int max_operands) {
        for ( int loop = 1; loop <= max_operands; loop++ ) {
            p.beginList(DisassemblerGenerator.operandClassName+"[] fill_"+loop+"(");
            for ( int cntr = 0; cntr < loop; cntr++ ) {
                p.print(DisassemblerGenerator.operandClassName+" o"+cntr);
            }
            p.endList(")");
            p.startblock();
            for ( int cntr = 0; cntr < loop; cntr++ ) {
                //String str = "OPERANDS_"+loop+"["+cntr+"] = o"+cntr+";";
                p.println(tr("OPERANDS_$1[$2] = o$2;", loop, cntr));
            }
            p.println("return OPERANDS_"+loop+";");
            p.endblock();
        }
    }

}
