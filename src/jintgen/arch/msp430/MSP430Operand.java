package jintgen.arch.msp430;
import java.util.HashMap;

/**
 * The <code>MSP430Operand</code> interface represents operands that are
 * allowed to instructions in this architecture. Inner classes of this
 * interface enumerate the possible operand types to instructions and
 * their constructors allow for dynamic checking of correctness
 * constraints as expressed in the instruction set description.
 */
public interface MSP430Operand {
    public interface DOUBLE_W_source_union extends MSP430Operand { }
    public interface DOUBLE_W_dest_union extends MSP430Operand { }
    public interface SINGLE_W_source_union extends MSP430Operand { }
    public interface DOUBLE_B_source_union extends MSP430Operand { }
    public interface DOUBLE_B_dest_union extends MSP430Operand { }
    public interface SINGLE_B_source_union extends MSP430Operand { }
    public class SREG implements MSP430Operand, DOUBLE_B_source_union, SINGLE_W_source_union, DOUBLE_W_source_union, DOUBLE_W_dest_union, SINGLE_B_source_union, DOUBLE_B_dest_union  {
        public final MSP430Symbol.SREG symbol;
        SREG(String s) {
            symbol = MSP430Symbol.SREG.get(s);
            if ( symbol == null ) throw new Error();
        }
        SREG(MSP430Symbol.SREG sym) {
            symbol = sym;
        }
    }
    
    public class AIREG_B implements MSP430Operand, DOUBLE_B_source_union, SINGLE_B_source_union  {
        public final MSP430Symbol.SREG symbol;
        AIREG_B(String s) {
            symbol = MSP430Symbol.SREG.get(s);
            if ( symbol == null ) throw new Error();
        }
        AIREG_B(MSP430Symbol.SREG sym) {
            symbol = sym;
        }
    }
    
    public class AIREG_W implements MSP430Operand, SINGLE_W_source_union, DOUBLE_W_source_union  {
        public final MSP430Symbol.SREG symbol;
        AIREG_W(String s) {
            symbol = MSP430Symbol.SREG.get(s);
            if ( symbol == null ) throw new Error();
        }
        AIREG_W(MSP430Symbol.SREG sym) {
            symbol = sym;
        }
    }
    
    public class IREG implements MSP430Operand, DOUBLE_B_source_union, SINGLE_W_source_union, DOUBLE_W_source_union, SINGLE_B_source_union  {
        public final MSP430Symbol.SREG symbol;
        IREG(String s) {
            symbol = MSP430Symbol.SREG.get(s);
            if ( symbol == null ) throw new Error();
        }
        IREG(MSP430Symbol.SREG sym) {
            symbol = sym;
        }
    }
    
    public class IMM implements MSP430Operand, DOUBLE_B_source_union, SINGLE_W_source_union, DOUBLE_W_source_union, SINGLE_B_source_union  {
        public static final int low = -32768;
        public static final int high = 65536;
        public final int value;
        IMM(int val) {
            value = MSP430InstrBuilder.checkValue(val, low, high);
        }
    }
    
    public class INDX implements MSP430Operand, DOUBLE_B_source_union, SINGLE_W_source_union, DOUBLE_W_source_union, DOUBLE_W_dest_union, SINGLE_B_source_union, DOUBLE_B_dest_union  {
        public final SREG reg;
        public final IMM index;
        INDX(SREG reg, IMM index) {
            this.reg = reg;
            this.index = index;
        }
    }
    
    public class SYM implements MSP430Operand, DOUBLE_B_source_union, SINGLE_W_source_union, DOUBLE_W_source_union, DOUBLE_W_dest_union, SINGLE_B_source_union, DOUBLE_B_dest_union  {
        public static final int low = 0;
        public static final int high = 65535;
        public final int value;
        SYM(int val) {
            value = MSP430InstrBuilder.checkValue(val, low, high);
        }
    }
    
    public class ABSO implements MSP430Operand, DOUBLE_B_source_union, SINGLE_W_source_union, DOUBLE_W_source_union, DOUBLE_W_dest_union, SINGLE_B_source_union, DOUBLE_B_dest_union  {
        public static final int low = 0;
        public static final int high = 65535;
        public final int value;
        ABSO(int val) {
            value = MSP430InstrBuilder.checkValue(val, low, high);
        }
    }
    
    public class JUMP implements MSP430Operand  {
        public static final int low = 0;
        public static final int high = 1023;
        public final int value;
        JUMP(int val) {
            value = MSP430InstrBuilder.checkValue(val, low, high);
        }
    }
    
}
