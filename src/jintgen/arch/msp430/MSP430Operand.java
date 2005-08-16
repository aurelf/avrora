package jintgen.arch.msp430;
import java.util.HashMap;
public class MSP430Operand {
    public static class SREG extends MSP430Operand {
        public final MSP430Symbol.SREG symbol;
        SREG(String s) {
            symbol = MSP430Symbol.SREG.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class AIREG_B extends MSP430Operand {
        public final MSP430Symbol.SREG symbol;
        AIREG_B(String s) {
            symbol = MSP430Symbol.SREG.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class AIREG_W extends MSP430Operand {
        public final MSP430Symbol.SREG symbol;
        AIREG_W(String s) {
            symbol = MSP430Symbol.SREG.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class IREG extends MSP430Operand {
        public final MSP430Symbol.SREG symbol;
        IREG(String s) {
            symbol = MSP430Symbol.SREG.get(s);
            if ( symbol == null ) throw new Error();
        }
    }
    
    public static class IMM extends MSP430Operand {
        public static final int low = -32768;
        public static final int high = 65536;
        public final int value;
        IMM(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class INDX extends MSP430Operand {
        public final SREG reg;
        public final IMM index;
        INDX(SREG reg, IMM index) {
            this.reg = reg;
            this.index = index;
        }
    }
    
    public static class SYM extends MSP430Operand {
        public static final int low = 0;
        public static final int high = 65535;
        public final int value;
        SYM(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class ABSO extends MSP430Operand {
        public static final int low = 0;
        public static final int high = 65535;
        public final int value;
        ABSO(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class JUMP extends MSP430Operand {
        public static final int low = 0;
        public static final int high = 1023;
        public final int value;
        JUMP(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class DOUBLE_W_source_union extends MSP430Operand {
        public final MSP430Operand operand;
        DOUBLE_W_source_union(SREG o) { operand = o; }
        DOUBLE_W_source_union(INDX o) { operand = o; }
        DOUBLE_W_source_union(SYM o) { operand = o; }
        DOUBLE_W_source_union(ABSO o) { operand = o; }
        DOUBLE_W_source_union(IREG o) { operand = o; }
        DOUBLE_W_source_union(AIREG_W o) { operand = o; }
        DOUBLE_W_source_union(IMM o) { operand = o; }
    }
    
    public static class DOUBLE_W_dest_union extends MSP430Operand {
        public final MSP430Operand operand;
        DOUBLE_W_dest_union(SREG o) { operand = o; }
        DOUBLE_W_dest_union(INDX o) { operand = o; }
        DOUBLE_W_dest_union(SYM o) { operand = o; }
        DOUBLE_W_dest_union(ABSO o) { operand = o; }
    }
    
    public static class SINGLE_W_source_union extends MSP430Operand {
        public final MSP430Operand operand;
        SINGLE_W_source_union(SREG o) { operand = o; }
        SINGLE_W_source_union(INDX o) { operand = o; }
        SINGLE_W_source_union(SYM o) { operand = o; }
        SINGLE_W_source_union(ABSO o) { operand = o; }
        SINGLE_W_source_union(IREG o) { operand = o; }
        SINGLE_W_source_union(AIREG_W o) { operand = o; }
        SINGLE_W_source_union(IMM o) { operand = o; }
    }
    
    public static class DOUBLE_B_source_union extends MSP430Operand {
        public final MSP430Operand operand;
        DOUBLE_B_source_union(SREG o) { operand = o; }
        DOUBLE_B_source_union(INDX o) { operand = o; }
        DOUBLE_B_source_union(SYM o) { operand = o; }
        DOUBLE_B_source_union(ABSO o) { operand = o; }
        DOUBLE_B_source_union(IREG o) { operand = o; }
        DOUBLE_B_source_union(AIREG_B o) { operand = o; }
        DOUBLE_B_source_union(IMM o) { operand = o; }
    }
    
    public static class DOUBLE_B_dest_union extends MSP430Operand {
        public final MSP430Operand operand;
        DOUBLE_B_dest_union(SREG o) { operand = o; }
        DOUBLE_B_dest_union(INDX o) { operand = o; }
        DOUBLE_B_dest_union(SYM o) { operand = o; }
        DOUBLE_B_dest_union(ABSO o) { operand = o; }
    }
    
    public static class SINGLE_B_source_union extends MSP430Operand {
        public final MSP430Operand operand;
        SINGLE_B_source_union(SREG o) { operand = o; }
        SINGLE_B_source_union(INDX o) { operand = o; }
        SINGLE_B_source_union(SYM o) { operand = o; }
        SINGLE_B_source_union(ABSO o) { operand = o; }
        SINGLE_B_source_union(IREG o) { operand = o; }
        SINGLE_B_source_union(AIREG_B o) { operand = o; }
        SINGLE_B_source_union(IMM o) { operand = o; }
    }
    
    private static int checkValue(int val, int low, int high) {
        if ( val < low || val > high ) {
            throw new Error();
        }
        return val;
    }
}
