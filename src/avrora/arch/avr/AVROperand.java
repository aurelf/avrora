package avrora.arch.avr;
/**
 * The <code>AVROperand</code> interface represents operands that are
 * allowed to instructions in this architecture. Inner classes of this
 * interface enumerate the possible operand types to instructions and
 * their constructors allow for dynamic checking of correctness
 * constraints as expressed in the instruction set description.
 */
public interface AVROperand {
    public void accept(AVROperandVisitor v);
    abstract static class Int implements AVROperand {
        public final int value;
        Int(int val) {
            this.value = val;
        }
        public String toString() {
            return Integer.toString(value);
        }
    }

    abstract static class Sym implements AVROperand {
        public final AVRSymbol symbol;
        Sym(AVRSymbol sym) {
            if ( sym == null ) throw new Error();
            this.symbol = sym;
        }
        public String toString() {
            return symbol.symbol;
        }
    }

    abstract static class Addr implements AVROperand {
        public final int address;
        Addr(int addr) {
            this.address = addr;
        }
        public String toString() {
            String hs = Integer.toHexString(address);
            StringBuffer buf = new StringBuffer("0x");
            for ( int cntr = hs.length(); cntr < 4; cntr++ ) buf.append('0');
            buf.append(hs);
            return buf.toString();
        }
    }

    abstract static class Rel implements AVROperand {
        public final int address;
        public final int relative;
        Rel(int addr, int rel) {
            this.address = addr;
            this.relative = rel;
        }
        public String toString() {
            if ( relative >= 0 ) return ".+"+relative;
            else return "."+relative;
        }
    }

    public class GPR extends Sym {
        GPR(String s) {
            super(AVRSymbol.get_GPR(s));
        }
        GPR(AVRSymbol.GPR sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class HGPR extends Sym {
        HGPR(String s) {
            super(AVRSymbol.get_HGPR(s));
        }
        HGPR(AVRSymbol.HGPR sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class MGPR extends Sym {
        MGPR(String s) {
            super(AVRSymbol.get_MGPR(s));
        }
        MGPR(AVRSymbol.MGPR sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class YZ extends Sym {
        YZ(String s) {
            super(AVRSymbol.get_YZ(s));
        }
        YZ(AVRSymbol.YZ sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class EGPR extends Sym {
        EGPR(String s) {
            super(AVRSymbol.get_EGPR(s));
        }
        EGPR(AVRSymbol.EGPR sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class RDL extends Sym {
        RDL(String s) {
            super(AVRSymbol.get_RDL(s));
        }
        RDL(AVRSymbol.RDL sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class IMM3 extends Int {
        public static final int low = 0;
        public static final int high = 7;
        IMM3(int val) {
            super(AVRInstrBuilder.checkValue(val, low, high));
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class IMM5 extends Int {
        public static final int low = 0;
        public static final int high = 31;
        IMM5(int val) {
            super(AVRInstrBuilder.checkValue(val, low, high));
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class IMM6 extends Int {
        public static final int low = 0;
        public static final int high = 63;
        IMM6(int val) {
            super(AVRInstrBuilder.checkValue(val, low, high));
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class IMM7 extends Int {
        public static final int low = 0;
        public static final int high = 127;
        IMM7(int val) {
            super(AVRInstrBuilder.checkValue(val, low, high));
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class IMM8 extends Int {
        public static final int low = 0;
        public static final int high = 255;
        IMM8(int val) {
            super(AVRInstrBuilder.checkValue(val, low, high));
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class SREL extends Rel {
        public static final int low = -64;
        public static final int high = 63;
        SREL(int pc, int rel) {
            super(pc + 2 + 2 * rel, AVRInstrBuilder.checkValue(rel, low, high));
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class LREL extends Rel {
        public static final int low = -1024;
        public static final int high = 1023;
        LREL(int pc, int rel) {
            super(pc + 2 + 2 * rel, AVRInstrBuilder.checkValue(rel, low, high));
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class PADDR extends Int {
        public static final int low = 0;
        public static final int high = 65536;
        PADDR(int val) {
            super(AVRInstrBuilder.checkValue(val, low, high));
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class DADDR extends Addr {
        public static final int low = 0;
        public static final int high = 65536;
        DADDR(int val) {
            super(AVRInstrBuilder.checkValue(val, low, high));
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class R0_B extends Sym {
        R0_B(String s) {
            super(AVRSymbol.get_R0(s));
        }
        R0_B(AVRSymbol.R0 sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class RZ_W extends Sym {
        RZ_W(String s) {
            super(AVRSymbol.get_RZ(s));
        }
        RZ_W(AVRSymbol.RZ sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class AI_RZ_W extends Sym {
        AI_RZ_W(String s) {
            super(AVRSymbol.get_RZ(s));
        }
        AI_RZ_W(AVRSymbol.RZ sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class XYZ extends Sym {
        XYZ(String s) {
            super(AVRSymbol.get_ADR(s));
        }
        XYZ(AVRSymbol.ADR sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class AI_XYZ extends Sym {
        AI_XYZ(String s) {
            super(AVRSymbol.get_ADR(s));
        }
        AI_XYZ(AVRSymbol.ADR sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

    public class PD_XYZ extends Sym {
        PD_XYZ(String s) {
            super(AVRSymbol.get_ADR(s));
        }
        PD_XYZ(AVRSymbol.ADR sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }

}
