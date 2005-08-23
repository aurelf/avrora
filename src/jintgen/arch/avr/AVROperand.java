package jintgen.arch.avr;
import java.util.HashMap;

/**
 * The <code>AVROperand</code> interface represents operands that are
 * allowed to instructions in this architecture. Inner classes of this
 * interface enumerate the possible operand types to instructions and
 * their constructors allow for dynamic checking of correctness
 * constraints as expressed in the instruction set description.
 */
public interface AVROperand {
    public interface XLPM_source_union extends AVROperand { }
    public interface XLPM_dest_union extends AVROperand { }
    public interface LD_ST_rd_union extends AVROperand { }
    public interface LD_ST_ar_union extends AVROperand { }
    public class GPR implements AVROperand, XLPM_dest_union, LD_ST_rd_union  {
        public final AVRSymbol.GPR symbol;
        GPR(String s) {
            symbol = AVRSymbol.GPR.get(s);
            if ( symbol == null ) throw new Error();
        }
        GPR(AVRSymbol.GPR sym) {
            symbol = sym;
        }
    }
    
    public class HGPR implements AVROperand  {
        public final AVRSymbol.HGPR symbol;
        HGPR(String s) {
            symbol = AVRSymbol.HGPR.get(s);
            if ( symbol == null ) throw new Error();
        }
        HGPR(AVRSymbol.HGPR sym) {
            symbol = sym;
        }
    }
    
    public class MGPR implements AVROperand  {
        public final AVRSymbol.MGPR symbol;
        MGPR(String s) {
            symbol = AVRSymbol.MGPR.get(s);
            if ( symbol == null ) throw new Error();
        }
        MGPR(AVRSymbol.MGPR sym) {
            symbol = sym;
        }
    }
    
    public class YZ implements AVROperand  {
        public final AVRSymbol.YZ symbol;
        YZ(String s) {
            symbol = AVRSymbol.YZ.get(s);
            if ( symbol == null ) throw new Error();
        }
        YZ(AVRSymbol.YZ sym) {
            symbol = sym;
        }
    }
    
    public class EGPR implements AVROperand  {
        public final AVRSymbol.EGPR symbol;
        EGPR(String s) {
            symbol = AVRSymbol.EGPR.get(s);
            if ( symbol == null ) throw new Error();
        }
        EGPR(AVRSymbol.EGPR sym) {
            symbol = sym;
        }
    }
    
    public class RDL implements AVROperand  {
        public final AVRSymbol.RDL symbol;
        RDL(String s) {
            symbol = AVRSymbol.RDL.get(s);
            if ( symbol == null ) throw new Error();
        }
        RDL(AVRSymbol.RDL sym) {
            symbol = sym;
        }
    }
    
    public class IMM3 implements AVROperand  {
        public static final int low = 0;
        public static final int high = 7;
        public final int value;
        IMM3(int val) {
            value = AVRInstrBuilder.checkValue(val, low, high);
        }
    }
    
    public class IMM5 implements AVROperand  {
        public static final int low = 0;
        public static final int high = 31;
        public final int value;
        IMM5(int val) {
            value = AVRInstrBuilder.checkValue(val, low, high);
        }
    }
    
    public class IMM6 implements AVROperand  {
        public static final int low = 0;
        public static final int high = 63;
        public final int value;
        IMM6(int val) {
            value = AVRInstrBuilder.checkValue(val, low, high);
        }
    }
    
    public class IMM7 implements AVROperand  {
        public static final int low = 0;
        public static final int high = 127;
        public final int value;
        IMM7(int val) {
            value = AVRInstrBuilder.checkValue(val, low, high);
        }
    }
    
    public class IMM8 implements AVROperand  {
        public static final int low = 0;
        public static final int high = 255;
        public final int value;
        IMM8(int val) {
            value = AVRInstrBuilder.checkValue(val, low, high);
        }
    }
    
    public class SREL implements AVROperand  {
        public static final int low = -64;
        public static final int high = 63;
        public final int value;
        SREL(int val) {
            value = AVRInstrBuilder.checkValue(val, low, high);
        }
    }
    
    public class LREL implements AVROperand  {
        public static final int low = -1024;
        public static final int high = 1023;
        public final int value;
        LREL(int val) {
            value = AVRInstrBuilder.checkValue(val, low, high);
        }
    }
    
    public class PADDR implements AVROperand  {
        public static final int low = 0;
        public static final int high = 65536;
        public final int value;
        PADDR(int val) {
            value = AVRInstrBuilder.checkValue(val, low, high);
        }
    }
    
    public class DADDR implements AVROperand  {
        public static final int low = 0;
        public static final int high = 65536;
        public final int value;
        DADDR(int val) {
            value = AVRInstrBuilder.checkValue(val, low, high);
        }
    }
    
    public class R0_B implements AVROperand, XLPM_dest_union  {
        public final AVRSymbol.R0 symbol;
        R0_B(String s) {
            symbol = AVRSymbol.R0.get(s);
            if ( symbol == null ) throw new Error();
        }
        R0_B(AVRSymbol.R0 sym) {
            symbol = sym;
        }
    }
    
    public class RZ_W implements AVROperand, XLPM_source_union  {
        public final AVRSymbol.RZ symbol;
        RZ_W(String s) {
            symbol = AVRSymbol.RZ.get(s);
            if ( symbol == null ) throw new Error();
        }
        RZ_W(AVRSymbol.RZ sym) {
            symbol = sym;
        }
    }
    
    public class AI_RZ_W implements AVROperand, XLPM_source_union  {
        public final AVRSymbol.RZ symbol;
        AI_RZ_W(String s) {
            symbol = AVRSymbol.RZ.get(s);
            if ( symbol == null ) throw new Error();
        }
        AI_RZ_W(AVRSymbol.RZ sym) {
            symbol = sym;
        }
    }
    
    public class XYZ implements AVROperand, LD_ST_ar_union  {
        public final AVRSymbol.ADR symbol;
        XYZ(String s) {
            symbol = AVRSymbol.ADR.get(s);
            if ( symbol == null ) throw new Error();
        }
        XYZ(AVRSymbol.ADR sym) {
            symbol = sym;
        }
    }
    
    public class AI_XYZ implements AVROperand, LD_ST_ar_union  {
        public final AVRSymbol.ADR symbol;
        AI_XYZ(String s) {
            symbol = AVRSymbol.ADR.get(s);
            if ( symbol == null ) throw new Error();
        }
        AI_XYZ(AVRSymbol.ADR sym) {
            symbol = sym;
        }
    }
    
    public class PD_XYZ implements AVROperand, LD_ST_ar_union  {
        public final AVRSymbol.ADR symbol;
        PD_XYZ(String s) {
            symbol = AVRSymbol.ADR.get(s);
            if ( symbol == null ) throw new Error();
        }
        PD_XYZ(AVRSymbol.ADR sym) {
            symbol = sym;
        }
    }
    
}
