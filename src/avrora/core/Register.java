package avrora.core;

import avrora.Operand;
import avrora.AVRErrorReporter;

import java.util.HashMap;
import java.util.HashSet;

/**
 * The <code>Register</code> class represents a register available on the AVR
 * instruction set. All registers in the instruction set architecture are
 * represented as objects that have a name and a number. Those objects are
 * singletons and are public static final fields of this class.<br><br>
 *
 * Additionally, the <code>Register</code> class IsExplored sets of registers
 * that are used in verifying the operand constraints of each individual
 * instruction as defined in the AVR instruction set reference. An example
 * of an operand constraint is that ldi (load immediate) takes as operands
 * one of the general purpose registers {r17...r31} and an immediate. Other
 * instructions take certain subsets of the instructions. Those register
 * sets are allocated once here and are exposed as static fields in this
 * class.
 *
 * @see Operand
 * @see Instr
 * @author Ben L. Titzer
 */
public class Register {

    private static final HashMap registers = initializeRegisterMap();

    public static final Register R0 = getRegisterByNumber(0);
    public static final Register R1 = getRegisterByNumber(1);
    public static final Register R2 = getRegisterByNumber(2);
    public static final Register R3 = getRegisterByNumber(3);
    public static final Register R4 = getRegisterByNumber(4);
    public static final Register R5 = getRegisterByNumber(5);
    public static final Register R6 = getRegisterByNumber(6);
    public static final Register R7 = getRegisterByNumber(7);
    public static final Register R8 = getRegisterByNumber(8);
    public static final Register R9 = getRegisterByNumber(9);
    public static final Register R10 = getRegisterByNumber(10);
    public static final Register R11 = getRegisterByNumber(11);
    public static final Register R12 = getRegisterByNumber(12);
    public static final Register R13 = getRegisterByNumber(13);
    public static final Register R14 = getRegisterByNumber(14);
    public static final Register R15 = getRegisterByNumber(15);
    public static final Register R16 = getRegisterByNumber(16);
    public static final Register R17 = getRegisterByNumber(17);
    public static final Register R18 = getRegisterByNumber(18);
    public static final Register R19 = getRegisterByNumber(19);
    public static final Register R20 = getRegisterByNumber(20);
    public static final Register R21 = getRegisterByNumber(21);
    public static final Register R22 = getRegisterByNumber(22);
    public static final Register R23 = getRegisterByNumber(23);
    public static final Register R24 = getRegisterByNumber(24);
    public static final Register R25 = getRegisterByNumber(25);
    public static final Register R26 = getRegisterByNumber(26);
    public static final Register R27 = getRegisterByNumber(27);
    public static final Register R28 = getRegisterByNumber(28);
    public static final Register R29 = getRegisterByNumber(29);
    public static final Register R30 = getRegisterByNumber(30);
    public static final Register R31 = getRegisterByNumber(31);

    public static final Register X = getRegisterByName("x");
    public static final Register Y = getRegisterByName("y");
    public static final Register Z = getRegisterByName("z");

    private static final Register[] REGS_0_31 = {
        R0, R1, R2, R3, R4, R5, R6, R7,
        R8, R9, R10, R11, R12, R13, R14, R15,
        R16, R17, R18, R19, R20, R21, R22, R23,
        R24, R25, R26, R27, R28, R29, R30, R31
    };
    private static final Register[] EREGS = {
        R0, R2, R4, R6, R8, R10, R12, R14,
        R16, R18, R20, R22, R24, R26, R28, R30,
    };
    private static final Register[] REGS_16_31 = {
        R16, R17, R18, R19, R20, R21, R22, R23,
        R24, R25, R26, R27, R28, R29, R30, R31
    };
    private static final Register[] REGS_16_23 = {
        R16, R17, R18, R19,
        R20, R21, R22, R23,
    };
    private static final Register[] REGS_XYZ = {
        X, Y, Z
    };
    private static final Register[] REGS_YZ = {
        Y, Z
    };
    private static final Register[] REGS_Z = {
        Z
    };
    private static final Register[] REGS_RDL = {
        R24, R26, R28, R30
    };
    public static final Set GPR_set = new Set("{r0, r1, ..., r31}", REGS_0_31);
    public static final Set HGPR_set = new Set("{r16, r17, ..., r31}", REGS_16_31);
    public static final Set MGPR_set = new Set("{r16, r17, ..., r23}", REGS_16_23);
    public static final Set EGPR_set = new Set("{r0, r2, ..., r30}", EREGS);
    public static final Set ADR_set = new Set("{x, y, z}", REGS_XYZ);
    public static final Set RDL_set = new Set("{r24, r26, r28, r30}", REGS_RDL);
    public static final Set YZ_set = new Set("{y, z}", REGS_YZ);
    public static final Set Z_set = new Set("{z}", REGS_Z);

    private static HashMap initializeRegisterMap() {
        HashMap map = new HashMap();

        for (int cntr = 0; cntr < 32; cntr++) {
            Register reg = new Register("r" + cntr, cntr, 8);
            map.put("r" + cntr, reg);
            map.put("R" + cntr, reg);
        }

        Register reg = new Register("x", 26, 16);
        map.put("x", reg);
        map.put("X", reg);

        reg = new Register("y", 28, 16);
        map.put("y", reg);
        map.put("Y", reg);

        reg = new Register("z", 30, 16);
        map.put("z", reg);
        map.put("Z", reg);

        return map;
    }

    public static Register getRegisterByName(String name) {
        return (Register) registers.get(name);
    }

    public static Register getRegisterByNumber(int num) {
        return getRegisterByName("r" + num);
    }


    private final String name;
    private final int number;
    private final int width;

    private Register(String nm, int num, int w) {
        name = nm;
        number = num;
        width = w;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public int getWidth() {
        return width;
    }

    public Register nextRegister() {
        return REGS_0_31[number + 1];
    }

    public static class Set {
        public final String contents;
        private final HashSet registers;

        Set(String n, Register[] regs) {
            contents = n;
            registers = new HashSet(2 * regs.length);
            for (int cntr = 0; cntr < regs.length; cntr++)
                registers.add(regs[cntr]);
        }

        public boolean contains(Register reg) {
            return registers.contains(reg);
        }

        public String toString() {
            return contents;
        }
    }

}
