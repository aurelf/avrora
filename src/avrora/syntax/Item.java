package avrora.syntax;

import avrora.Avrora;
import avrora.util.StringUtil;
import avrora.core.Register;
import avrora.core.InstrPrototype;
import avrora.core.Instr;

/**
 * I T E M   C L A S S E S
 * --------------------------------------------------------
 * <p/>
 * These item classes represent the various parts of the assembly
 * file that are recorded in the module.
 */
public abstract class Item {

    protected final Module module;
    protected final Module.Seg segment;
    protected final int byteAddress;

    public abstract void simplify();

    Item(Module.Seg seg) {
        byteAddress = seg.getCurrentAddress();
        segment = seg;
        module = seg.getModule();
    }

    public int itemSize() {
        return 0;
    }

    public static class NamedConstant extends Item {
        private final AbstractToken name;
        private final Expr value;

        NamedConstant(Module.Seg s, AbstractToken n, Expr v) {
            super(s);
            name = n;
            value = v;
        }

        public void simplify() {
            int result = value.evaluate(byteAddress, module);
            module.addVariable(name.image, result);
        }

        public String toString() {
            return ".equ " + name;
        }
    }

    public static class RegisterAlias extends Item {
        private final AbstractToken name;
        private final AbstractToken register;

        RegisterAlias(Module.Seg s, AbstractToken n, AbstractToken r) {
            super(s);
            name = n;
            register = r;
        }

        public void simplify() {
            module.addRegisterName(name.image, register.image);
        }

        public String toString() {
            return ".def " + name + " = " + register;
        }
    }


    public static class Instruction extends Item {
        protected final String variant;
        protected final AbstractToken name;
        protected final SyntacticOperand[] operands;
        protected final InstrPrototype proto;

        Instruction(Module.Seg s, String v, AbstractToken n, InstrPrototype p, SyntacticOperand[] ops) {
            super(s);
            variant = v;
            name = n;
            operands = ops;
            proto = p;
        }

        public void simplify() {

            for (int cntr = 0; cntr < operands.length; cntr++)
                operands[cntr].simplify(byteAddress + proto.getSize(), module);

//            try {
            segment.writeInstr(name, byteAddress, proto.build(byteAddress >> 1, operands));

//            } catch (Instr.ImmediateRequired e) {
//                ERROR.ConstantExpected((SyntacticOperand)e.operand);
//            } catch (Instr.InvalidImmediate e) {
//                ERROR.ConstantOutOfRange(operands[e.number - 1], e.value, StringUtil.interval(e.low, e.high));
//            } catch (Instr.InvalidRegister e) {
//                ERROR.IncorrectRegister(operands[e.number - 1], e.register, e.set.toString());
//            } catch (Instr.RegisterRequired e) {
//                ERROR.RegisterExpected((SyntacticOperand)e.operand);
//            } catch (Instr.WrongNumberOfOperands e) {
//                ERROR.WrongNumberOfOperands(name, e.found, e.expected);
//            }
        }

        public int itemSize() {
            return proto.getSize();
        }

        public String toString() {
            return "instr: " + variant + " @ " + byteAddress;
        }
    }

    public static class Label extends Item {
        private final AbstractToken name;

        Label(Module.Seg s, AbstractToken n) {
            super(s);
            name = n;
        }

        public void simplify() {
            segment.addLabel(byteAddress, name.image);
        }

        public int getByteAddress() {
            return byteAddress;
        }

        public String toString() {
            return "label: " + name + " in " + segment.getName() + " @ " + byteAddress;
        }
    }

    public static class InitializedData extends Item {

        private final ExprList list;
        private final int width;
        private final int size;

        InitializedData(Module.Seg s, ExprList l, int w) {
            super(s);
            list = l;
            width = w;
            size = computeSize(l, w);
        }

        public void simplify() {
            int cursor = byteAddress;

            for (ExprList.ExprItem item = list.head; item != null; item = item.next) {
                Expr e = item.expr;
                if (e instanceof Expr.StringLiteral) {
                    cursor = writeString(e, segment, cursor);
                } else {
                    cursor = writeValue(e, module, segment, cursor);
                }
            }
        }

        private int writeValue(Expr e, Module module, Module.Seg s, int cursor) {
            int val = e.evaluate(byteAddress, module);
            for (int cntr = 0; cntr < width; cntr++) {
                s.writeDataByte(e, cursor, (byte) val);
                val = val >> 8;
                cursor++;
            }
            return cursor;
        }

        private int writeString(Expr e, Module.Seg s, int cursor) {
            String str = ((Expr.StringLiteral) e).value;
            // TODO: should this string literal be evaluated first?
            s.writeDataBytes(e, cursor, str.getBytes());
            // align the cursor
            return align(cursor, width);
        }

        private int computeSize(ExprList l, int width) {
            int count = 0;
            for (ExprList.ExprItem item = l.head; item != null; item = item.next) {
                Expr e = item.expr;
                if (e instanceof Expr.StringLiteral) {
                    // TODO: is this the right size?
                    count += align(((Expr.StringLiteral) e).value.length(), width);
                } else {
                    count += width;
                }
            }
            return count;
        }

        private int align(int cursor, int width) {
            if ( width > 1) {
                int i = cursor % width;
                cursor = i == 0 ? cursor : cursor + (i - width);
            }
            return cursor;
        }

        public int itemSize() {
            return size;
        }

        public String toString() {
            return "initialized data @ " + byteAddress;
        }
    }

    public static class UninitializedData extends Item {
        private final int length;

        UninitializedData(Module.Seg s, int l) {
            super(s);
            length = l;
        }

        public void simplify() {
            // nothing to build with a reserve item
        }

        public String toString() {
            return "reserve " + length + " in " + segment.getName();
        }

        public int itemSize() {
            return length;
        }
    }
}
