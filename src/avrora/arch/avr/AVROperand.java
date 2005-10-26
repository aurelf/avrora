package avrora.arch.avr;
import avrora.arch.*;
import java.util.HashMap;

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
        public final AVRSymbol value;
        Sym(AVRSymbol sym) {
            if ( sym == null ) throw new Error();
            this.value = sym;
        }
        public String toString() {
            return value.symbol;
        }
    }
    
    abstract static class Addr implements AVROperand {
        public final int value;
        Addr(int addr) {
            this.value = addr;
        }
        public String toString() {
            String hs = Integer.toHexString(value);
            StringBuffer buf = new StringBuffer("0x");
            for ( int cntr = hs.length(); cntr < 4; cntr++ ) buf.append('0');
            buf.append(hs);
            return buf.toString();
        }
    }
    
    abstract static class Rel implements AVROperand {
        public final int value;
        public final int relative;
        Rel(int addr, int rel) {
            this.value = addr;
            this.relative = rel;
        }
        public String toString() {
            if ( relative >= 0 ) return ".+"+relative;
            else return "."+relative;
        }
    }
    
    public class op_GPR extends Sym {
        op_GPR(String s) {
            super(AVRSymbol.get_GPR(s));
        }
        op_GPR(AVRSymbol.GPR sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }
    
    public class op_HGPR extends Sym {
        op_HGPR(String s) {
            super(AVRSymbol.get_HGPR(s));
        }
        op_HGPR(AVRSymbol.HGPR sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }
    
    public class op_MGPR extends Sym {
        op_MGPR(String s) {
            super(AVRSymbol.get_MGPR(s));
        }
        op_MGPR(AVRSymbol.MGPR sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }
    
    public class op_YZ extends Sym {
        op_YZ(String s) {
            super(AVRSymbol.get_YZ(s));
        }
        op_YZ(AVRSymbol.YZ sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }
    
    public class op_EGPR extends Sym {
        op_EGPR(String s) {
            super(AVRSymbol.get_EGPR(s));
        }
        op_EGPR(AVRSymbol.EGPR sym) {
            super(sym);
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }
    
    public class op_RDL extends Sym {
        op_RDL(String s) {
            super(AVRSymbol.get_RDL(s));
        }
        op_RDL(AVRSymbol.RDL sym) {
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
    
    public class SREL extends Int {
        public static final int low = -64;
        public static final int high = 63;
        SREL(int val) {
            super(AVRInstrBuilder.checkValue(val, low, high));
        }
        public void accept(AVROperandVisitor v) {
            v.visit(this);
        }
    }
    
    public class LREL extends Int {
        public static final int low = -1024;
        public static final int high = 1023;
        LREL(int val) {
            super(AVRInstrBuilder.checkValue(val, low, high));
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
    
    public class DADDR extends Int {
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
