package jintgen.arch.msp430;
import java.util.HashMap;
public class MSP430Symbol {
    public final String symbol;
    public final int value;
    
    MSP430Symbol(String sym, int v) { symbol = sym;  value = v; }
    public int getValue() { return value; }
    public int getEncodingValue() { return value; }
    
    public static class GPR extends MSP430Symbol {
        private static HashMap set = new HashMap();
        private static GPR newGPR(String n, int v) {
            GPR obj = new GPR(n, v);
            set.put(n, obj);
            return obj;
        }
        public static GPR get(String name) {
            return (GPR)set.get(name);
        }
        GPR(String sym, int v) { super(sym, v); }
        public static final GPR PC = newGPR("pc", 0);
        public static final GPR SP = newGPR("sp", 1);
        public static final GPR SR = newGPR("sr", 2);
        public static final GPR R0 = newGPR("r0", 0);
        public static final GPR R1 = newGPR("r1", 1);
        public static final GPR R2 = newGPR("r2", 2);
        public static final GPR R3 = newGPR("r3", 3);
        public static final GPR R4 = newGPR("r4", 4);
        public static final GPR R5 = newGPR("r5", 5);
        public static final GPR R6 = newGPR("r6", 6);
        public static final GPR R7 = newGPR("r7", 7);
        public static final GPR R8 = newGPR("r8", 8);
        public static final GPR R9 = newGPR("r9", 9);
        public static final GPR R10 = newGPR("r10", 10);
        public static final GPR R11 = newGPR("r11", 11);
        public static final GPR R12 = newGPR("r12", 12);
        public static final GPR R13 = newGPR("r13", 13);
        public static final GPR R14 = newGPR("r14", 14);
        public static final GPR R15 = newGPR("r15", 15);
    }
    
    public static class SREG extends GPR {
        public final int encoding;
        public int getEncodingValue() { return encoding; }
        private static HashMap set = new HashMap();
        private static SREG newSREG(String n, int v, int ev) {
            SREG obj = new SREG(n, v, ev);
            set.put(n, obj);
            return obj;
        }
        public static SREG get(String name) {
            return (SREG)set.get(name);
        }
        SREG(String sym, int v, int ev) { super(sym, v); encoding = ev; }
        public static final SREG R2 = newSREG("r2", 2, 2);
        public static final SREG R3 = newSREG("r3", 3, 3);
        public static final SREG R4 = newSREG("r4", 4, 4);
        public static final SREG R5 = newSREG("r5", 5, 5);
        public static final SREG R6 = newSREG("r6", 6, 6);
        public static final SREG R7 = newSREG("r7", 7, 7);
        public static final SREG R8 = newSREG("r8", 8, 8);
        public static final SREG R9 = newSREG("r9", 9, 9);
        public static final SREG R10 = newSREG("r10", 10, 10);
        public static final SREG R11 = newSREG("r11", 11, 11);
        public static final SREG R12 = newSREG("r12", 12, 12);
        public static final SREG R13 = newSREG("r13", 13, 13);
        public static final SREG R14 = newSREG("r14", 14, 14);
        public static final SREG R15 = newSREG("r15", 15, 15);
    }
    
}
