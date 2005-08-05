package jintgen.arch.msp430;
import java.util.HashMap;
public class MSP430Operand {
    public static class SREG_B extends MSP430Operand {
        public final MSP430Symbol symbol;
        public static final HashMap set = new HashMap();
        static {
        }
        SREG_B(String s) {
            symbol = null;
        }
        SREG_B(MSP430Symbol sym) {
            symbol = null;
        }
    }
    
    public static class SREG_W extends MSP430Operand {
        public final MSP430Symbol symbol;
        public static final HashMap set = new HashMap();
        static {
        }
        SREG_W(String s) {
            symbol = null;
        }
        SREG_W(MSP430Symbol sym) {
            symbol = null;
        }
    }
    
    public static class AIREG_B extends MSP430Operand {
        public final MSP430Symbol symbol;
        public static final HashMap set = new HashMap();
        static {
        }
        AIREG_B(String s) {
            symbol = null;
        }
        AIREG_B(MSP430Symbol sym) {
            symbol = null;
        }
    }
    
    public static class AIREG_W extends MSP430Operand {
        public final MSP430Symbol symbol;
        public static final HashMap set = new HashMap();
        static {
        }
        AIREG_W(String s) {
            symbol = null;
        }
        AIREG_W(MSP430Symbol sym) {
            symbol = null;
        }
    }
    
    public static class INDREG_B extends MSP430Operand {
        public final MSP430Symbol symbol;
        public static final HashMap set = new HashMap();
        static {
        }
        INDREG_B(String s) {
            symbol = null;
        }
        INDREG_B(MSP430Symbol sym) {
            symbol = null;
        }
    }
    
    public static class INDREG_W extends MSP430Operand {
        public final MSP430Symbol symbol;
        public static final HashMap set = new HashMap();
        static {
        }
        INDREG_W(String s) {
            symbol = null;
        }
        INDREG_W(MSP430Symbol sym) {
            symbol = null;
        }
    }
    
    public static class IMM8 extends MSP430Operand {
        public static final int low = 0;
        public static final int high = 255;
        public final int value;
        IMM8(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class IMM16 extends MSP430Operand {
        public static final int low = 0;
        public static final int high = 65535;
        public final int value;
        IMM16(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class INDX_B extends MSP430Operand {
        public final SREG_B reg;
        public final IMM8 index;
        INDX_B(SREG_B reg, IMM8 index) {
            this.reg = reg;
            this.index = index;
        }
    }
    
    public static class INDX_W extends MSP430Operand {
        public final SREG_W reg;
        public final IMM16 index;
        INDX_W(SREG_W reg, IMM16 index) {
            this.reg = reg;
            this.index = index;
        }
    }
    
    public static class SYMB_B extends MSP430Operand {
        public static final int low = 0;
        public static final int high = 255;
        public final int value;
        SYMB_B(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class SYMB_W extends MSP430Operand {
        public static final int low = 0;
        public static final int high = 65535;
        public final int value;
        SYMB_W(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class ABSO_B extends MSP430Operand {
        public static final int low = 0;
        public static final int high = 255;
        public final int value;
        ABSO_B(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class ABSO_W extends MSP430Operand {
        public static final int low = 0;
        public static final int high = 65535;
        public final int value;
        ABSO_W(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class IMMD_B extends MSP430Operand {
        public static final int low = 0;
        public static final int high = 255;
        public final int value;
        IMMD_B(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class IMMD_W extends MSP430Operand {
        public static final int low = 0;
        public static final int high = 65535;
        public final int value;
        IMMD_W(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class JUMP_W extends MSP430Operand {
        public static final int low = 0;
        public static final int high = 1023;
        public final int value;
        JUMP_W(int val) {
            value = checkValue(val, low, high);
        }
    }
    
    public static class DOUBLE_W_source_union extends MSP430Operand {
        public final MSP430Operand operand;
        DOUBLE_W_source_union(SREG_W o) { operand = o; }
        DOUBLE_W_source_union(INDX_W o) { operand = o; }
        DOUBLE_W_source_union(SYMB_W o) { operand = o; }
        DOUBLE_W_source_union(ABSO_W o) { operand = o; }
        DOUBLE_W_source_union(INDREG_W o) { operand = o; }
        DOUBLE_W_source_union(AIREG_W o) { operand = o; }
        DOUBLE_W_source_union(IMMD_W o) { operand = o; }
    }
    
    public static class DOUBLE_W_dest_union extends MSP430Operand {
        public final MSP430Operand operand;
        DOUBLE_W_dest_union(SREG_W o) { operand = o; }
        DOUBLE_W_dest_union(INDX_W o) { operand = o; }
        DOUBLE_W_dest_union(SYMB_W o) { operand = o; }
        DOUBLE_W_dest_union(ABSO_W o) { operand = o; }
    }
    
    public static class SINGLE_W_source_union extends MSP430Operand {
        public final MSP430Operand operand;
        SINGLE_W_source_union(SREG_W o) { operand = o; }
        SINGLE_W_source_union(INDX_W o) { operand = o; }
        SINGLE_W_source_union(SYMB_W o) { operand = o; }
        SINGLE_W_source_union(ABSO_W o) { operand = o; }
        SINGLE_W_source_union(INDREG_W o) { operand = o; }
        SINGLE_W_source_union(AIREG_W o) { operand = o; }
        SINGLE_W_source_union(IMMD_W o) { operand = o; }
    }
    
    public static class DOUBLE_B_source_union extends MSP430Operand {
        public final MSP430Operand operand;
        DOUBLE_B_source_union(SREG_B o) { operand = o; }
        DOUBLE_B_source_union(INDX_B o) { operand = o; }
        DOUBLE_B_source_union(SYMB_B o) { operand = o; }
        DOUBLE_B_source_union(ABSO_B o) { operand = o; }
        DOUBLE_B_source_union(INDREG_B o) { operand = o; }
        DOUBLE_B_source_union(AIREG_B o) { operand = o; }
        DOUBLE_B_source_union(IMMD_B o) { operand = o; }
    }
    
    public static class DOUBLE_B_dest_union extends MSP430Operand {
        public final MSP430Operand operand;
        DOUBLE_B_dest_union(SREG_B o) { operand = o; }
        DOUBLE_B_dest_union(INDX_B o) { operand = o; }
        DOUBLE_B_dest_union(SYMB_B o) { operand = o; }
        DOUBLE_B_dest_union(ABSO_B o) { operand = o; }
    }
    
    public static class SINGLE_B_source_union extends MSP430Operand {
        public final MSP430Operand operand;
        SINGLE_B_source_union(SREG_B o) { operand = o; }
        SINGLE_B_source_union(INDX_B o) { operand = o; }
        SINGLE_B_source_union(SYMB_B o) { operand = o; }
        SINGLE_B_source_union(ABSO_B o) { operand = o; }
        SINGLE_B_source_union(INDREG_B o) { operand = o; }
        SINGLE_B_source_union(AIREG_B o) { operand = o; }
        SINGLE_B_source_union(IMMD_B o) { operand = o; }
    }
    
    private static int checkValue(int val, int low, int high) {
        if ( val < low || val > high ) {
            throw new Error();
        }
        return val;
    }
}
