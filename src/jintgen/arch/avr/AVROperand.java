package jintgen.arch.avr;
import java.util.HashMap;
public class AVROperand {
    public static class GPR extends AVROperand {
        public final AVRSymbol.GPR symbol;
        GPR(String s) {
            symbol = AVRSymbol.GPR.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class HGPR extends AVROperand {
        public final AVRSymbol.HGPR symbol;
        HGPR(String s) {
            symbol = AVRSymbol.HGPR.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class MGPR extends AVROperand {
        public final AVRSymbol.MGPR symbol;
        MGPR(String s) {
            symbol = AVRSymbol.MGPR.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class YZ extends AVROperand {
        public final AVRSymbol.YZ symbol;
        YZ(String s) {
            symbol = AVRSymbol.YZ.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class EGPR extends AVROperand {
        public final AVRSymbol.EGPR symbol;
        EGPR(String s) {
            symbol = AVRSymbol.EGPR.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class RDL extends AVROperand {
        public final AVRSymbol.RDL symbol;
        RDL(String s) {
            symbol = AVRSymbol.RDL.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class IMM3 extends AVROperand {
        public static final int low = 0;
        public static final int high = 7;
        public final int value;
        IMM3(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class IMM5 extends AVROperand {
        public static final int low = 0;
        public static final int high = 31;
        public final int value;
        IMM5(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class IMM6 extends AVROperand {
        public static final int low = 0;
        public static final int high = 63;
        public final int value;
        IMM6(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class IMM7 extends AVROperand {
        public static final int low = 0;
        public static final int high = 127;
        public final int value;
        IMM7(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class IMM8 extends AVROperand {
        public static final int low = 0;
        public static final int high = 255;
        public final int value;
        IMM8(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class SREL extends AVROperand {
        public static final int low = -64;
        public static final int high = 63;
        public final int value;
        SREL(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class LREL extends AVROperand {
        public static final int low = -1024;
        public static final int high = 1023;
        public final int value;
        LREL(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class PADDR extends AVROperand {
        public static final int low = 0;
        public static final int high = 65536;
        public final int value;
        PADDR(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class DADDR extends AVROperand {
        public static final int low = 0;
        public static final int high = 65536;
        public final int value;
        DADDR(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class R0_B extends AVROperand {
        public final AVRSymbol.R0 symbol;
        R0_B(String s) {
            symbol = AVRSymbol.R0.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class RZ_W extends AVROperand {
        public final AVRSymbol.RZ symbol;
        RZ_W(String s) {
            symbol = AVRSymbol.RZ.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class AI_RZ_W extends AVROperand {
        public final AVRSymbol.RZ symbol;
        AI_RZ_W(String s) {
            symbol = AVRSymbol.RZ.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class XYZ extends AVROperand {
        public final AVRSymbol.ADR symbol;
        XYZ(String s) {
            symbol = AVRSymbol.ADR.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class AI_XYZ extends AVROperand {
        public final AVRSymbol.ADR symbol;
        AI_XYZ(String s) {
            symbol = AVRSymbol.ADR.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class PD_XYZ extends AVROperand {
        public final AVRSymbol.ADR symbol;
        PD_XYZ(String s) {
            symbol = AVRSymbol.ADR.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class XLPM_source_union extends AVROperand {
        public final AVROperand operand;
        XLPM_source_union(RZ_W o) { operand = o; }
        XLPM_source_union(AI_RZ_W o) { operand = o; }
    }
    
    public static class XLPM_dest_union extends AVROperand {
        public final AVROperand operand;
        XLPM_dest_union(R0_B o) { operand = o; }
        XLPM_dest_union(GPR o) { operand = o; }
    }
    
    public static class LD_ST_rd_union extends AVROperand {
        public final AVROperand operand;
        LD_ST_rd_union(GPR o) { operand = o; }
    }
    
    public static class LD_ST_ar_union extends AVROperand {
        public final AVROperand operand;
        LD_ST_ar_union(XYZ o) { operand = o; }
        LD_ST_ar_union(AI_XYZ o) { operand = o; }
        LD_ST_ar_union(PD_XYZ o) { operand = o; }
    }
    
    private static int checkValue(int val, int low, int high) {
        if ( val < low || val > high ) {
            throw new Error();
        }
        return val;
    }
}
