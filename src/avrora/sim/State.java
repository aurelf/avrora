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

package avrora.sim;

import avrora.util.Arithmetic;
import avrora.Avrora;
import avrora.core.Instr;
import avrora.core.Program;
import avrora.core.Register;
import avrora.util.StringUtil;
import avrora.util.Terminal;

/**
 * The <code>State</code> class represents the state of the simulator, including
 * the contents of registers and memory.
 *
 * @author Ben L. Titzer
 */
public interface State extends IORegisterConstants {


    /**
     * The <code>IOReg</code> interface models the behavior of an IO register.
     * Since some IO registers behave specially with regards to the devices they
     * control, their functionality can be implemented externally to the <code>
     * State</code> class.
     *
     * @author Ben L. Titzer
     */
    public interface IOReg {
        /**
         * The <code>read()</code> method reads the 8-bit value of the IO register
         * as a byte. For special IO registers, this may cause some action like
         * device activity, or the actual value of the register may need to be
         * fetched or computed.
         *
         * @return the value of the register as a byte
         */
        public byte read();

        /**
         * The <code>write()</code> method writes an 8-bit value to the IO register
         * as a byte. For special IO registers, this may cause some action like
         * device activity, masking/unmasking of interrupts, etc.
         *
         * @param val the value to write
         */
        public void write(byte val);

        /**
         * The <code>readBit()</code> method reads a single bit from the IO register.
         *
         * @param num the number of the bit to read
         * @return the value of the bit as a boolean
         */
        public boolean readBit(int num);

        /**
         * The <code>clearBit()</code> method clears a single bit in the IO register.
         *
         * @param num the number of the bit to clear
         */
        public void clearBit(int num);

        /**
         * The <code>setBit()</code> method sets a single bit in the IO register.
         *
         * @param num the number of the bit to clear
         */
        public void setBit(int num);

        public void writeBit(int num, boolean val);
    }

    /**
     * The <code>RWIOReg</code> class is an implementation of an IO register
     * that has the simple, default behavior of being able to read and write
     * just as a general purpose register or byte in SRAM.
     *
     * @author Ben L. Titzer
     */
    public static class RWIOReg implements IOReg {

        protected byte value;

        /**
         * The <code>read()</code> method reads the 8-bit value of the IO register
         * as a byte. For simple <code>RWIOReg</code> instances, this simply returns
         * the internally stored value.
         *
         * @return the value of the register as a byte
         */
        public byte read() {
            return value;
        }

        /**
         * The <code>write()</code> method writes an 8-bit value to the IO register
         * as a byte. For simple <code>RWIOReg</code> instances, this simply writes
         * the internally stored value.
         *
         * @param val the value to write
         */
        public void write(byte val) {
            value = val;
        }

        /**
         * The <code>readBit()</code> method reads a single bit from the IO register.
         *
         * @param num the number of the bit to read
         * @return the value of the bit as a boolean
         */
        public boolean readBit(int num) {
            return Arithmetic.getBit(value, num);
        }

        /**
         * The <code>clearBit()</code> method clears a single bit in the IO register.
         *
         * @param num the number of the bit to clear
         */
        public void clearBit(int num) {
            value = Arithmetic.clearBit(value, num);
        }

        /**
         * The <code>setBit()</code> method sets a single bit in the IO register.
         *
         * @param num the number of the bit to clear
         */
        public void setBit(int num) {
            value = Arithmetic.setBit(value, num);
        }

        public void writeBit(int num, boolean val) {
            if ( val ) setBit(num);
            else clearBit(num);
        }
    }

    /**
     * The <code>RESERVED</code> field of the state class represents an instance
     * of the <code>IOReg</code> interface that will not allow any writes to
     * this register to occur. These reserved IO registers are specified in the
     * hardware manuals.
     */
    public static final IOReg RESERVED = new IOReg() {
        public byte read() {
            return 0;
        }

        public void write(byte val) {
            throw new Error("cannot write to reserved register");
        }

        public boolean readBit(int num) {
            return false;
        }

        public void setBit(int bit) {
            throw new Error("cannot set bit in reserved register");
        }

        public void clearBit(int bit) {
            throw new Error("cannot clear bit in reserved register");
        }

        public void writeBit(int bit, boolean val) {
            throw new Error("cannot write bit in reserved register");
        }
    };


    /**
     * The <code>getPostedInterrupts()</code> method returns a mask that represents
     * all interrupts that are currently pending (meaning they are ready to be
     * fired in priority order as long as the I flag is on).
     *
     * @return a mask representing the interrupts which are posted for processing
     */
    public long getPostedInterrupts();


    /**
     * Read a general purpose register's current value as a byte.
     *
     * @param reg the register to read
     * @return the current value of the register
     */
    public byte getRegisterByte(Register reg);

    /**
     * Read a general purpose register's current value as an integer, without any sign
     * extension.
     *
     * @param reg the register to read
     * @return the current unsigned value of the register
     */
    public int getRegisterUnsigned(Register reg);

    /**
     * Read a general purpose register pair as an unsigned word. This method will
     * read the value of the specified register and the value of the next register
     * in numerical order and return the two values combined as an unsigned integer
     * The specified register should be less than r31, because r32 (the next register)
     * does not exist.
     *
     * @param reg the low register of the pair to read
     * @return the current unsigned word value of the register pair
     */
    public int getRegisterWord(Register reg);


    /**
     * The <code>getSREG()</code> method reads the value of the status register.
     * The status register contains the I, T, H, S, V, N, Z, and C flags, in order
     * from highest-order to lowest-order.
     *
     * @return the value of the status register as a byte.
     */
    public byte getSREG();


    /**
     * The <code>getFlag_I()</code> method returns the current value of the I bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_I();

    /**
     * The <code>getFlag_T()</code> method returns the current value of the T bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_T();

    /**
     * The <code>getFlag_H()</code> method returns the current value of the H bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_H();

    /**
     * The <code>getFlag_S()</code> method returns the current value of the S bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_S();

    /**
     * The <code>getFlag_V()</code> method returns the current value of the V bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_V();

    /**
     * The <code>getFlag_N()</code> method returns the current value of the N bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_N();

    /**
     * The <code>getFlag_Z()</code> method returns the current value of the Z bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_Z();

    /**
     * The <code>getFlag_C()</code> method returns the current value of the C bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_C();

    /**
     * The <code>getStackByte()</code> method reads a byte from the address
     * specified by SP+1. This method should not be called with an empty stack,
     * as it will cause an exception consistent with trying to read non-existent
     * memory.
     *
     * @return the value on the top of the stack
     */
    public byte getStackByte();

    /**
     * The <code>getSP()</code> method reads the current value of the stack pointer.
     * Since the stack pointer is stored in two IO registers, this method will cause the
     * invocation of the <code>.read()</code> method on each of the <code>IOReg</code>
     * objects that store these values.
     *
     * @return the value of the stack pointer as a byte address
     */
    public int getSP();


    /**
     * The <code>getPC()</code> retrieves the current program counter.
     *
     * @return the program counter as a byte address
     */
    public int getPC();


    /**
     * The <code>getInstr()</code> can be used to retrieve a reference to the
     * <code>Instr</code> object representing the instruction at the specified program
     * address. Care should be taken that the address in program memory specified does
     * not contain data. This is because Avrora does have a functioning disassembler
     * and assumes that the <code>Instr</code> objects for each instruction in the
     * program are known a priori.
     *
     * @param address the byte address from which to read the instruction
     * @return a reference to the <code>Instr</code> object representing the instruction
     *         at that address in the program
     */
    public Instr getInstr(int address);


    /**
     * The <code>getDataByte()</code> method reads a byte value from the data memory
     * (SRAM) at the specified address.
     *
     * @param address the byte address to read
     * @return the value of the data memory at the specified address
     * @throws ArrayIndexOutOfBoundsException if the specified address is not the valid
     *                                        memory range
     */
    public byte getDataByte(int address);

    /**
     * The <code>getProgramByte()</code> method reads a byte value from
     * the program (Flash) memory. The flash memory generally stores read-only
     * values and the instructions of the program. Care should be taken that
     * the program memory at the specified address does not contain an instruction.
     * This is because, in general, programs should not read instructions as
     * data, and secondly, because no assembler is present in Avrora and therefore
     * the actual byte value of an instruction may not be known.
     *
     * @param address the byte address at which to read
     * @return the byte value of the program memory at the specified address
     * @throws ArrayIndexOutOfBoundsException if the specified address is not the valid
     *                                        program memory range
     */
    public byte getProgramByte(int address);


    /**
     * The <code>getIORegisterByte()</code> method reads the value of an IO register.
     * Invocation of this method causes an invocatiobn of the <code>.read()</code>
     * method on the corresponding internal <code>IOReg</code> object, and its value
     * returned.
     *
     * @param ioreg the IO register number
     * @return the value of the IO register
     */
    public byte getIORegisterByte(int ioreg);


    /**
     * The <code>getIOReg()</code> method is used to retrieve a reference to
     * the actual <code>IOReg</code> instance stored internally in the state. This is
     * generally only used in the simulator and device implementations, and clients
     * should probably not call this memory directly.
     *
     * @param ioreg the IO register number to retrieve
     * @return a reference to the <code>IOReg</code> instance of the specified IO register
     */
    public IOReg getIOReg(int ioreg);

    /**
     * The <code>getCycles()</code> method returns the clock cycle count recorded
     * so far in the simulation.
     *
     * @return the number of clock cycles elapsed in the simulation
     */
    public long getCycles();


}
