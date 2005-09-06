package avrora.arch.msp430;
/**
 * The <code>MSP430Operand</code> interface represents operands that are
 * allowed to instructions in this architecture. Inner classes of this
 * interface enumerate the possible operand types to instructions and
 * their constructors allow for dynamic checking of correctness
 * constraints as expressed in the instruction set description.
 */
public interface MSP430Operand {
    public void accept(MSP430OperandVisitor v);
    abstract static class Int implements MSP430Operand {
        public final int value;
        Int(int val) {
            this.value = val;
        }
        public String toString() {
            return Integer.toString(value);
        }
    }

    abstract static class Sym implements MSP430Operand {
        public final MSP430Symbol symbol;
        Sym(MSP430Symbol sym) {
            if ( sym == null ) throw new Error();
            this.symbol = sym;
        }
        public String toString() {
            return symbol.symbol;
        }
    }

    abstract static class Addr implements MSP430Operand {
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

    abstract static class Rel implements MSP430Operand {
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

    public class SREG extends Sym {
        SREG(String s) {
            super(MSP430Symbol.get_GPR(s));
        }
        SREG(MSP430Symbol.GPR sym) {
            super(sym);
        }
        public void accept(MSP430OperandVisitor v) {
            v.visit(this);
        }
    }

    public class AIREG_B extends Sym {
        AIREG_B(String s) {
            super(MSP430Symbol.get_GPR(s));
        }
        AIREG_B(MSP430Symbol.GPR sym) {
            super(sym);
        }
        public void accept(MSP430OperandVisitor v) {
            v.visit(this);
        }
    }

    public class AIREG_W extends Sym {
        AIREG_W(String s) {
            super(MSP430Symbol.get_GPR(s));
        }
        AIREG_W(MSP430Symbol.GPR sym) {
            super(sym);
        }
        public void accept(MSP430OperandVisitor v) {
            v.visit(this);
        }
    }

    public class IREG extends Sym {
        IREG(String s) {
            super(MSP430Symbol.get_GPR(s));
        }
        IREG(MSP430Symbol.GPR sym) {
            super(sym);
        }
        public void accept(MSP430OperandVisitor v) {
            v.visit(this);
        }
    }

    public class IMM extends Int {
        public static final int low = -32768;
        public static final int high = 65536;
        IMM(int val) {
            super(MSP430InstrBuilder.checkValue(val, low, high));
        }
        public void accept(MSP430OperandVisitor v) {
            v.visit(this);
        }
    }

    public class INDX implements MSP430Operand {
        public final MSP430Operand.SREG reg;
        public final MSP430Operand.IMM index;
        public INDX(MSP430Operand.SREG reg, MSP430Operand.IMM index)  {
            this.reg = reg;
            this.index = index;
        }
        public void accept(MSP430OperandVisitor v) {
            v.visit(this);
        }
    }

    public class SYM extends Rel {
        public static final int low = -32768;
        public static final int high = 32767;
        SYM(int pc, int rel) {
            super(pc + 2 + 2 * rel, MSP430InstrBuilder.checkValue(rel, low, high));
        }
        public void accept(MSP430OperandVisitor v) {
            v.visit(this);
        }
    }

    public class ABSO extends Addr {
        public static final int low = 0;
        public static final int high = 65535;
        ABSO(int val) {
            super(MSP430InstrBuilder.checkValue(val, low, high));
        }
        public void accept(MSP430OperandVisitor v) {
            v.visit(this);
        }
    }

    public class JUMP extends Rel {
        public static final int low = -512;
        public static final int high = 511;
        JUMP(int pc, int rel) {
            super(pc + 2 + 2 * rel, MSP430InstrBuilder.checkValue(rel, low, high));
        }
        public void accept(MSP430OperandVisitor v) {
            v.visit(this);
        }
    }

}
