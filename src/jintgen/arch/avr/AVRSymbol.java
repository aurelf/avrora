package jintgen.arch.avr;
import java.util.HashMap;
public class AVRSymbol {
    public final String symbol;
    public final int value;
    
    AVRSymbol(String sym, int v) { symbol = sym;  value = v; }
    public int getValue() { return value; }
    
    public static class GPR extends AVRSymbol {
        GPR(String sym, int v) { super(sym, v); }
        private static HashMap GPR_set = new HashMap();
        private static GPR newGPR(String n, int v) {
            GPR obj = new GPR(n, v);
            GPR_set.put(n, obj);
            return obj;
        }
        public static GPR get(String name) {
            return (GPR)GPR_set.get(name);
        }
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
        public static final GPR R16 = newGPR("r16", 16);
        public static final GPR R17 = newGPR("r17", 17);
        public static final GPR R18 = newGPR("r18", 18);
        public static final GPR R19 = newGPR("r19", 19);
        public static final GPR R20 = newGPR("r20", 20);
        public static final GPR R21 = newGPR("r21", 21);
        public static final GPR R22 = newGPR("r22", 22);
        public static final GPR R23 = newGPR("r23", 23);
        public static final GPR R24 = newGPR("r24", 24);
        public static final GPR R25 = newGPR("r25", 25);
        public static final GPR R26 = newGPR("r26", 26);
        public static final GPR R27 = newGPR("r27", 27);
        public static final GPR R28 = newGPR("r28", 28);
        public static final GPR R29 = newGPR("r29", 29);
        public static final GPR R30 = newGPR("r30", 30);
        public static final GPR R31 = newGPR("r31", 31);
    }
    
    public static class ADR extends AVRSymbol {
        ADR(String sym, int v) { super(sym, v); }
        private static HashMap ADR_set = new HashMap();
        private static ADR newADR(String n, int v) {
            ADR obj = new ADR(n, v);
            ADR_set.put(n, obj);
            return obj;
        }
        public static ADR get(String name) {
            return (ADR)ADR_set.get(name);
        }
        public static final ADR X = newADR("X", 26);
        public static final ADR Y = newADR("Y", 28);
        public static final ADR Z = newADR("Z", 30);
    }
    
}
