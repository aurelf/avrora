/**
 * Copyright (c) 2004, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package avrora.core;

import java.util.HashMap;
import java.util.HashSet;

/**
 * The <code>Register</code> class represents a register available on the AVR
 * instruction set. All registers in the instruction set architecture are
 * represented as objects that have a name and a number. Those objects are
 * singletons and are public static final fields of this class.<br><br>
 * <p/>
 * Additionally, the <code>Register</code> class contains sets of registers
 * that are used in verifying the operand constraints of each individual
 * instruction as defined in the AVR instruction set reference. An example
 * of an operand constraint is that ldi (load immediate) takes as operands
 * one of the general purpose registers {r17...r31} and an immediate. Other
 * instructions take certain subsets of the instructions. Those register
 * sets are allocated once here and are exposed as static fields in this
 * class.
 *
 * @author Ben L. Titzer
 * @see Operand
 * @see Instr
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

    /**
     * The <code>hashCode()</code> computes the hash code of this register so that
     * registers can be inserted in hashmaps and hashsets. This implementation
     * of register simply uses the hash code of the name of the register as its
     * hash code.
     * @return an integer that represents the hash code of this register
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * The <code>toString()</code> method coverts this register to a string.
     * This implementation simply returns the name of the register.
     * @return a string representation of this register
     */
    public String toString() {
        return name;
    }

    /**
     * The <code>getName()</code> method returns the name of the instruction
     * as a string.
     * @return the name of the instruction
     */
    public String getName() {
        return name;
    }

    /**
     * The <code>getNumber()</code> method returns the "number" of this register,
     * meaning the offset into the register file.
     * @return the number of this register
     */
    public int getNumber() {
        return number;
    }

    /**
     * The <code>getWidth()</code> method returns the width of the register in
     * bits.
     * @return the number of bits in this register
     */
    public int getWidth() {
        return width;
    }

    /**
     * The <code>nextRegister()</code> method returns a reference to the register
     * that immediately follows this register in the register file. This is needed
     * when treating multiple registers as a single value, etc.
     * @return the register immediately following this register in the register file
     */
    public Register nextRegister() {
        return REGS_0_31[number + 1];
    }

    /**
     * The <code>Set</code> class represents a set of registers. This is used to
     * represent classes of registers that are used as operands to various instructions.
     * For example, an instruction might expect one of its operands to be a general
     * purpose register that has a number greater than 15; a set of those registers
     * can be constructed and then a membership test performed.
     *
     * In practice, the needed register sets are all allocated statically.
     */
    public static class Set {

        /**
         * The <code>contents</code> field stores a string that represents a summary
         * of the registers that are in this set. An example string for the even
         * registers would be <code>"{r0, r2, ..., r30}"</code>.
         */
        public final String contents;

        private final HashSet registers;

        /**
         * The constructor for the <code>Set</code> class takes a string that
         * represents the contents of the registers and an array of registers
         * that are members of the set. It then constructs an internal hash set
         * for fast membership tests.
         * @param n the string representing the contents of this set
         * @param regs an array of registers that are members of this set
         */
        Set(String n, Register[] regs) {
            contents = n;
            registers = new HashSet(2 * regs.length);
            for (int cntr = 0; cntr < regs.length; cntr++)
                registers.add(regs[cntr]);
        }

        /**
         * The <code>contains()</code> method tests for membership. Given a register,
         * it will return true if that register is a member of this set, and false
         * otherwise.
         * @param reg the register to test membership of
         * @return true if the specified register is a member of this set; false otherwise
         */
        public boolean contains(Register reg) {
            return registers.contains(reg);
        }

        /**
         * The <code>toString()</code> method converts this set to a string representation.
         * In this implementation, it simply returns the string representation of the
         * contents.
         * @return a string representation of this register set
         */
        public String toString() {
            return contents;
        }
    }

}
