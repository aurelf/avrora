/**
 * Copyright (c) 2004-2005, Regents of the University of California
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

import avrora.arch.AbstractInstr;
import avrora.arch.avr.AVRProperties;
import avrora.arch.legacy.*;
import avrora.core.Program;
import avrora.sim.mcu.RegisterSet;
import avrora.sim.util.*;
import cck.util.Arithmetic;
import cck.util.Util;

/**
 * The <code>BaseInterpreter</code> class represents a base class of the legacy interpreter and the generated
 * interpreter(s) that stores the state of the executing program, e.g. registers and flags, etc.
 *
 * @author Ben L. Titzer
 */
public abstract class AtmelInterpreter extends Interpreter implements LegacyInstrVisitor {

    protected final int SREG; // location of the SREG IO register
    public final int RAMPZ; // location of the RAMPZ IO register
    protected int bootPC; // start up address
    protected int interruptBase; // base of interrupt vector table

    protected int pc;
    protected final ActiveRegister[] ioregs;
    public byte[] sram;
    protected MulticastWatch[] sram_watches;
    protected final int sram_start;
    protected final int sram_max;
    public boolean I;
    public boolean T;
    public boolean H;
    public boolean S;
    public boolean V;
    public boolean N;
    public boolean Z;
    public boolean C;
    public ActiveRegister SREG_reg;
    protected RWRegister SPL_reg;
    protected RWRegister SPH_reg;

    protected final CodeSegment flash;
    protected LegacyInstr[] shared_instr; // shared for performance reasons only

    protected final RegisterSet registers;

    protected final StateImpl state;

    /**
     * The <code>globalProbe</code> field stores a reference to a <code>MulticastProbe</code> that contains
     * all of the probes to be fired before and after the main execution runLoop--i.e. before and after every
     * instruction.
     */
    protected final MulticastProbe globalProbe;

    /**
     * The <code>exceptionWatch</code> stores a reference to a <code>MulticastExceptionWatch</code>
     * that contains all of the exception watches currently registered.
     */
    protected MulticastExceptionWatch exceptionWatch;

    /**
     * The <code>nextPC</code> field is used internally in maintaining the correct execution order of the
     * instructions.
     */
    public int nextPC;

    /**
     * The <code>cyclesConsumed</code> field stores the number of cycles consumed in doing a part of the
     * simulation (e.g. executing an instruction or processing an interrupt).
     */
    public int cyclesConsumed;

    /**
     * The <code>delayCycles</code> field tracks the number of cycles that the microcontroller is delayed.
     * Delay is needed because some devices pause execution of the program for some number of cycles, and also
     * to implement random delay at the beginning of startup in multiple node scenarios to prevent artificial
     * cycle-level synchronization.
     */
    protected long delayCycles;

    /**
     * The <code>shouldRun</code> flag is used internally in the main execution runLoop to implement the
     * correct semantics of <code>start()</code> and <code>stop()</code> to the clients.
     */
    protected boolean shouldRun;

    /**
     * The <code>sleeping</code> flag is used internally in the simulator when the microcontroller enters the
     * sleep mode.
     */
    protected boolean sleeping;

    /**
     * The <code>justReturnedFromInterrupt</code> field is used internally in maintaining the invariant stated
     * in the hardware manual that at least one instruction following a return from an interrupt is executed
     * before another interrupt can be processed.
     */
    public boolean justReturnedFromInterrupt;

    public class StateImpl implements LegacyState {

        /**
         * The <code>getSimulator()</code> method returns the simulator associated with this state
         * instance.
         * @return a reference to the simulator associated with this state instance.
         */
        public Simulator getSimulator() {
            return simulator;
        }

        /**
         * The <code>getInterruptTable()</code> method gets a reference to the interrupt table,
         * which contains information about each interrupt, such as whether it is enabled, posted,
         * pending, etc.
         * @return a reference to the <code>InterruptTable</code> instance
         */
        public InterruptTable getInterruptTable() {
            return interrupts;
        }

        /**
         * Read a general purpose register's current value as a byte.
         *
         * @param reg the register to read
         * @return the current value of the register
         */
        public byte getRegisterByte(LegacyRegister reg) {
            return sram[reg.getNumber()];
        }

        /**
         * Read a general purpose register's current value as an integer, without any sign extension.
         *
         * @param reg the register to read
         * @return the current unsigned value of the register
         */
        public int getRegisterUnsigned(LegacyRegister reg) {
            return sram[reg.getNumber()] & 0xff;
        }

        /**
         * Read a general purpose register pair as an unsigned word. This method will read the value of the
         * specified register and the value of the next register in numerical order and return the two values
         * combined as an unsigned integer The specified register should be less than r31, because r32 (the next
         * register) does not exist.
         *
         * @param reg the low register of the pair to read
         * @return the current unsigned word value of the register pair
         */
        public int getRegisterWord(LegacyRegister reg)  {
            int number = reg.getNumber();
            return Arithmetic.uword(sram[number], sram[number+1]);
        }


        /**
         * The <code>getSREG()</code> method reads the value of the status register. The status register contains
         * the I, T, H, S, V, N, Z, and C flags, in order from highest-order to lowest-order.
         *
         * @return the value of the status register as a byte.
         */
        public byte getSREG() {
            return SREG_reg.read();
        }


        /**
         * The <code>getFlag_I()</code> method returns the current value of the I bit in the status register as a
         * boolean.
         *
         * @return the value of the flag
         */
        public boolean getFlag_I() {
            return I;
        }

        /**
         * The <code>getFlag_T()</code> method returns the current value of the T bit in the status register as a
         * boolean.
         *
         * @return the value of the flag
         */
        public boolean getFlag_T()  {
            return T;
        }

        /**
         * The <code>getFlag_H()</code> method returns the current value of the H bit in the status register as a
         * boolean.
         *
         * @return the value of the flag
         */
        public boolean getFlag_H()  {
            return H;
        }

        /**
         * The <code>getFlag_S()</code> method returns the current value of the S bit in the status register as a
         * boolean.
         *
         * @return the value of the flag
         */
        public boolean getFlag_S() {
            return S;
        }

        /**
         * The <code>getFlag_V()</code> method returns the current value of the V bit in the status register as a
         * boolean.
         *
         * @return the value of the flag
         */
        public boolean getFlag_V() {
            return V;
        }

        /**
         * The <code>getFlag_N()</code> method returns the current value of the N bit in the status register as a
         * boolean.
         *
         * @return the value of the flag
         */
        public boolean getFlag_N() {
            return N;
        }

        /**
         * The <code>getFlag_Z()</code> method returns the current value of the Z bit in the status register as a
         * boolean.
         *
         * @return the value of the flag
         */
        public boolean getFlag_Z() {
            return Z;
        }

        /**
         * The <code>getFlag_C()</code> method returns the current value of the C bit in the status register as a
         * boolean.
         *
         * @return the value of the flag
         */
        public boolean getFlag_C() {
            return C;
        }

        /**
         * The <code>getStackByte()</code> method reads a byte from the address specified by SP+1. This method
         * should not be called with an empty stack, as it will cause an exception consistent with trying to read
         * non-existent memory.
         *
         * @return the value on the top of the stack
         */
        public byte getStackByte() {
            int address = getSP();
            return getDataByte(address);
        }

        /**
         * The <code>getSP()</code> method reads the current value of the stack pointer. Since the stack pointer
         * is stored in two IO registers, this method will cause the invocation of the <code>.read()</code> method
         * on each of the <code>IOReg</code> objects that store these values.
         *
         * @return the value of the stack pointer as a byte address
         */
        public int getSP() {
            byte low = SPL_reg.value;
            byte high = SPH_reg.value;
            return Arithmetic.uword(low, high);
        }

        /**
         * The <code>getPC()</code> retrieves the current program counter.
         *
         * @return the program counter as a byte address
         */
        public int getPC() {
            return pc;
        }

        /**
         * The <code>getInstr()</code> can be used to retrieve a reference to the <code>LegacyInstr</code> object
         * representing the instruction at the specified program address. Care should be taken that the address in
         * program memory specified does not contain data. This is because Avrora does have a functioning
         * disassembler and assumes that the <code>LegacyInstr</code> objects for each instruction in the program are
         * known a priori.
         *
         * @param address the byte address from which to read the instruction
         * @return a reference to the <code>LegacyInstr</code> object representing the instruction at that address in
         *         the program
         */
        public AbstractInstr getInstr(int address) {
            return flash.readInstr(address);
        }

        /**
         * The <code>getDataByte()</code> method reads a byte value from the data memory (SRAM) at the specified
         * address.
         *
         * @param address the byte address to read
         * @return the value of the data memory at the specified address
         * @throws ArrayIndexOutOfBoundsException if the specified address is not the valid memory range
         */
        public byte getDataByte(int address) {
            if (address >= sram_start)
                return sram[address];
            if (address >= NUM_REGS)
                return getAR(address - NUM_REGS).read();
            return sram[address];
        }

        /**
         * The <code>getProgramByte()</code> method reads a byte value from the program (Flash) memory. The flash
         * memory generally stores read-only values and the instructions of the program. Care should be taken that
         * the program memory at the specified address does not contain an instruction. This is because, in
         * general, programs should not read instructions as data, and secondly, because no assembler is present
         * in Avrora and therefore the actual byte value of an instruction may not be known.
         *
         * @param address the byte address at which to read
         * @return the byte value of the program memory at the specified address
         * @throws ArrayIndexOutOfBoundsException if the specified address is not the valid program memory range
         */
        public byte getProgramByte(int address) {
            return flash.get(address);
        }

        /**
         * The <code>getIORegisterByte()</code> method reads the value of an IO register. Invocation of this
         * method causes an invocatiobn of the <code>.read()</code> method on the corresponding internal
         * <code>IOReg</code> object, and its value returned.
         *
         * @param ioreg the IO register number
         * @return the value of the IO register
         */
        public byte getIORegisterByte(int ioreg) {
            return getAR(ioreg).read();
        }

        /**
         * The <code>getIOReg()</code> method is used to retrieve a reference to the actual <code>IOReg</code>
         * instance stored internally in the state. This is generally only used in the simulator and device
         * implementations, and clients should probably not call this memory directly.
         *
         * @param ioreg the IO register number to retrieve
         * @return a reference to the <code>ActiveRegister</code> instance of the specified IO register
         */
        public ActiveRegister getIOReg(int ioreg) {
            return getAR(ioreg);
        }

        private ActiveRegister getAR(int ioreg) {
            ActiveRegister ar = ioregs[ioreg];
            if ( ar instanceof ProbedActiveRegister ) {
                ar = ((ProbedActiveRegister)ar).ioreg;
            }
            return ar;
        }

        /**
         * The <code>getCycles()</code> method returns the clock cycle count recorded so far in the simulation.
         *
         * @return the number of clock cycles elapsed in the simulation
         */
        public long getCycles() {
            return clock.getCount();
        }

        /**
         * The <code>getSleepMode()</code> method returns an integer code describing which sleep mode the
         * microcontroller is currently in.
         *
         * @return an integer code representing the current sleep mode
         */
        public int getSleepMode() {
            throw Util.unimplemented();
        }

    }

    /**
     * The constructor for the <code>BaseInterpreter</code> class initializes the node's flash,
     * SRAM, general purpose registers, IO registers, and loads the program onto the flash. It
     * uses the <code>MicrocontrollerProperties</code> instance to configure the interpreter
     * such as the size of flash, SRAM, and location of IO registers.
     * @param simulator the simulator instance for this interpreter
     * @param p the program to load onto this interpreter instance
     * @param pr the properties of the microcontroller being simulated
     */
    public AtmelInterpreter(Simulator simulator, Program p, AVRProperties pr) {
        super(simulator);
        // this class and its methods are performance critical
        // observed speedup with this call on Hotspot
        Compiler.compileClass(this.getClass());

        state = new StateImpl();

        globalProbe = new MulticastProbe();
        exceptionWatch = new MulticastExceptionWatch();

        SREG = pr.getIOReg("SREG");

        // only look for the RAMPZ register if the flash is more than 64kb
        if ( pr.hasIOReg("RAMPZ") )
            RAMPZ = pr.getIOReg("RAMPZ");
        else
            RAMPZ = -1;

        // if program will not fit onto hardware, error
        if (p.program_end > pr.flash_size)
            throw Util.failure("program will not fit into " + pr.flash_size + " bytes");

        // beginning address of SRAM array
        sram_start = pr.ioreg_size + LegacyState.NUM_REGS;

        // maximum data address
        sram_max = LegacyState.NUM_REGS + pr.ioreg_size + pr.sram_size;

        // allocate SRAM
        sram = new byte[sram_max];

        // initialize IO registers to default values
        registers = simulator.getMicrocontroller().getRegisterSet();

        // for performance, we share a refernce to the ActiveRegister[] array
        ioregs = registers.share();
        SREG_reg = ioregs[SREG] = new SREG_reg();

        // allocate FLASH
        ErrorReporter reporter = new ErrorReporter();
        flash = pr.codeSegmentFactory.newCodeSegment("flash", this, reporter, p);
        reporter.segment = flash;
        // for performance, we share a reference to the LegacyInstr[] array representing flash
        // TODO: implement share() method
        shared_instr = flash.shareCode(null);

        // initialize the interrupt table
        interrupts = new InterruptTable(this, pr.num_interrupts);

        SPL_reg = (RWRegister) ioregs[pr.getIOReg("SPL")];
        SPH_reg = (RWRegister) ioregs[pr.getIOReg("SPH")];
    }

    public void start() {
        shouldRun = true;
        runLoop();
    }

    public void stop() {
        shouldRun = false;
        innerLoop = false;
    }

    public State getState() {
        return state;
    }

    protected abstract void runLoop();

    /**
     * The <code>getInterruptVectorAddress()</code> method computes the location in memory to jump to for the
     * given interrupt number. On the Atmega128, the starting point is the beginning of memory and each
     * interrupt vector slot is 4 bytes. On older architectures, this is not the case, therefore this method
     * has to be implemented according to the specific device being simulated.
     *
     * @param inum the interrupt number
     * @return the byte address that represents the address in the program to jump to when this interrupt is
     *         fired
     */
    protected int getInterruptVectorAddress(int inum) {
        return interruptBase + (inum - 1) * 4;
    }

    /**
     * The <code>setPosted()<code> method is used by external devices to post and unpost interrupts.
     * @param inum the interrupt number to post or unpost
     * @param post true if the interrupt should be posted; false if the interrupt should be unposted
     */
    public void setPosted(int inum, boolean post) {
        if ( post ) interrupts.post(inum);
        else interrupts.unpost(inum);
    }

    /**
     * The <code>setEnabled()</code> method is used by external devices (and mask registers) to enable
     * and disable interrupts.
     * @param inum the interrupt number to enable or disable
     * @param enabled true if the interrupt should be enabled; false if the interrupt should be disabled
     */
    public void setEnabled(int inum, boolean enabled) {
        if ( enabled ) {
            innerLoop = false;
            interrupts.enable(inum);
        } else interrupts.disable(inum);
    }

    /**
     * The <code>insertProbe()</code> method is used internally to insert a probe on a particular instruction.
     * @param p the probe to insert on an instruction
     * @param addr the address of the instruction on which to insert the probe
     */
    protected void insertProbe(Simulator.Probe p, int addr) {
        flash.insertProbe(addr, p);
    }

    /**
     * The <code>insertExceptionWatch()</code> method registers an </code>ExceptionWatch</code> to listen for
     * exceptional conditions in the machine.
     *
     * @param watch The <code>ExceptionWatch</code> instance to add.
     */
    protected void insertExceptionWatch(Simulator.ExceptionWatch watch) {
        exceptionWatch.add(watch);
    }

    /**
     * The <code>insertProbe()</code> method allows a probe to be inserted that is executed before and after
     * every instruction that is executed by the simulator
     *
     * @param p the probe to insert
     */
    protected void insertProbe(Simulator.Probe p) {
        innerLoop = false;
        globalProbe.add(p);
    }

    /**
     * The <code>removeProbe()</code> method is used internally to remove a probe from a particular instruction.
     * @param p the probe to remove from an instruction
     * @param addr the address of the instruction from which to remove the probe
     */
    protected void removeProbe(Simulator.Probe p, int addr) {
        flash.removeProbe(addr, p);
    }

    /**
     * The <code>removeProbe()</code> method removes a probe from the global probe table (the probes executed
     * before and after every instruction). The comparison used is reference equality, not
     * <code>.equals()</code>.
     *
     * @param b the probe to remove
     */
    public void removeProbe(Simulator.Probe b) {
        innerLoop = false;
        globalProbe.remove(b);
    }

    /**
     * The <code>insertWatch()</code> method is used internally to insert a watch on a particular memory location.
     * @param p the watch to insert on a memory location
     * @param data_addr the address of the memory location on which to insert the watch
     */
    protected void insertWatch(Simulator.Watch p, int data_addr) {
        if (sram_watches == null)
            sram_watches = new MulticastWatch[sram.length];

        // add the probe to the multicast probe present at the location (if there is one)
        MulticastWatch w = sram_watches[data_addr];
        if (w == null) w = sram_watches[data_addr] = new MulticastWatch();
        w.add(p);
    }

    /**
     * The <code>removeWatch()</code> method is used internally to remove a watch from a particular memory location.
     * @param p the watch to remove from the memory location
     * @param data_addr the address of the memory location from which to remove the watch
     */
    protected void removeWatch(Simulator.Watch p, int data_addr) {
        if (sram_watches == null)
            return;

        // remove the probe from the multicast probe present at the location (if there is one)
        MulticastWatch w = sram_watches[data_addr];
        if (w == null) return;
        w.remove(p);
    }

    /**
     * The <code>insertIORWatch()</code> method is used internally to insert a watch on an IO register.
     * @param p the watch to add to the IO register
     * @param ioreg_num the number of the IO register for which to insert the watch
     */
    protected void insertIORWatch(Simulator.IORWatch p, int ioreg_num) {
        ActiveRegister ar = ioregs[ioreg_num];
        ProbedActiveRegister par;
        if (ar instanceof ProbedActiveRegister) {
            par = (ProbedActiveRegister) ar;
        } else {
            par = new ProbedActiveRegister(this, ar, ioreg_num);
            ioregs[ioreg_num] = par;
        }
        par.add(p);
    }

    /**
     * The <code>removeIORWatch()</code> method is used internally to remove a watch on an IO register.
     * @param p the watch to remove from the IO register
     * @param ioreg_num the number of the IO register for which to remove the watch
     */
    protected void removeIORWatch(Simulator.IORWatch p, int ioreg_num) {
        ActiveRegister ar = ioregs[ioreg_num];
        ProbedActiveRegister par;
        if (ar instanceof ProbedActiveRegister) {
            par = (ProbedActiveRegister) ar;
            par.remove(p);
            if (par.isEmpty())
                ioregs[ioreg_num] = par.ioreg;
        }
    }

    /**
     * The <code>advanceClock()</code> method advances the clock by the specified number of cycles. It SHOULD NOT
     * be used externally. It also clears the <code>cyclesConsumed</code> variable that is used to track the
     * number of cycles consumed by a single instruction.
     * @param delta the number of cycles to advance the clock
     */
    protected void advanceClock(long delta) {
        clock.advance(delta);
        cyclesConsumed = 0;
    }

    /**
     * The <code>delay()</code> method is used to add some delay cycles before the next instruction is executed.
     * This is necessary because some devices such as the EEPROM actually delay execution of instructions while
     * they are working
     * @param cycles the number of cycles to delay the execution
     */
    protected void delay(long cycles) {
        innerLoop = false;
        delayCycles += cycles;
    }

    /**
     * The <code>storeProgramMemory()</code> method is called when the program executes the SPM instruction
     * which stores to the program memory.
     */
    protected void storeProgramMemory() {
        flash.update();
    }

    /**
     * Read a general purpose register's current value as a byte.
     *
     * @param reg the register to read
     * @return the current value of the register
     */
    public byte getRegisterByte(LegacyRegister reg) {
        return sram[reg.getNumber()];
    }

    public byte getRegisterByte(int reg) {
        return sram[reg];
    }

    /**
     * Read a general purpose register's current value as an integer, without any sign extension.
     *
     * @param reg the register to read
     * @return the current unsigned value of the register
     */
    public int getRegisterUnsigned(LegacyRegister reg) {
        return sram[reg.getNumber()] & 0xff;
    }

    /**
     * The <code>getRegisterUnsigned()</code> method reads a register's value (without sign extension)
     * @param reg the index into the register file
     * @return the value of the register as an unsigned integer
     */
    public int getRegisterUnsigned(int reg) {
        return sram[reg] & 0xff;
    }

    /**
     * Read a general purpose register pair as an unsigned word. This method will read the value of the
     * specified register and the value of the next register in numerical order and return the two values
     * combined as an unsigned integer The specified register should be less than r31, because r32 (the next
     * register) does not exist.
     *
     * @param reg the low register of the pair to read
     * @return the current unsigned word value of the register pair
     */
    public int getRegisterWord(LegacyRegister reg) {
        byte low = getRegisterByte(reg);
        byte high = getRegisterByte(reg.nextRegister());
        return Arithmetic.uword(low, high);
    }

    /**
     * Read a general purpose register pair as an unsigned word. This method will read the value of the
     * specified register and the value of the next register in numerical order and return the two values
     * combined as an unsigned integer The specified register should be less than r31, because r32 (the next
     * register) does not exist.
     *
     * @param reg the low register of the pair to read
     * @return the current unsigned word value of the register pair
     */
    public int getRegisterWord(int reg) {
        byte low = getRegisterByte(reg);
        byte high = getRegisterByte(reg + 1);
        return Arithmetic.uword(low, high);
    }

    /**
     * The <code>getSREG()</code> method reads the value of the status register. The status register contains
     * the I, T, H, S, V, N, Z, and C flags, in order from highest-order to lowest-order.
     *
     * @return the value of the status register as a byte.
     */
    public byte getSREG() {
        return SREG_reg.read();
    }

    /**
     * The <code>getDataByte()</code> method reads a byte value from the data memory (SRAM) at the specified
     * address.
     *
     * @param address the byte address to read
     * @return the value of the data memory at the specified address
     * @throws java.lang.ArrayIndexOutOfBoundsException
     *          if the specified address is not the valid memory range
     */
    public byte getDataByte(int address) {
        Simulator.Watch p;
        try {
            // FAST PATH 1: no watches
            if (sram_watches == null)
                return getSRAM(address);

            // FAST PATH 2: no watches for this address
            p = sram_watches[address];
            if (p == null)
                return getSRAM(address);
        } catch (ArrayIndexOutOfBoundsException e) {
            // ERROR: program tried to read memory that is not present
            SimUtil.readError(simulator, "sram", address);
            return 0;
        }

        // SLOW PATH: consult with memory watches
        p.fireBeforeRead(state, address);
        byte val = getSRAM(address);
        p.fireAfterRead(state, address, val);

        return val;
    }

    /**
     * The <code>ErrorReporter</code> class is used to report errors accessing segments.
     * @see Segment.ErrorReporter
     */
    protected class ErrorReporter implements Segment.ErrorReporter {
        Segment segment;

        public byte readError(int address) {
            SimUtil.readError(simulator, segment.name, address);
            exceptionWatch.invalidRead(segment.name, address);
            return segment.value;
        }

        public void writeError(int address, byte value) {
            SimUtil.writeError(simulator, segment.name, address, value);
            exceptionWatch.invalidWrite(segment.name, address, value);
        }
    }

    private byte getSRAM(int address) {
        if (address >= sram_start)
            return sram[address];
        if (address >= LegacyState.NUM_REGS)
            return ioregs[address - LegacyState.NUM_REGS].read();
        return sram[address];

    }

    /**
     * The <code>getInstrSize()</code> method reads the size of the instruction at the given program address.
     * This is needed in the interpreter to compute the target of a skip instruction (an instruction that
     * skips over the instruction following it).
     *
     * @param npc the program address of the instruction
     * @return the size in bytes of the instruction at the specified program address
     */
    public int getInstrSize(int npc) {
        return getInstr(npc).getSize();
    }

    /**
     * The <code>getIORegisterByte()</code> method reads the value of an IO register. Invocation of this
     * method causes an invocatiobn of the <code>.read()</code> method on the corresponding internal
     * <code>IOReg</code> object, and its value returned.
     *
     * @param ioreg the IO register number
     * @return the value of the IO register
     */
    public byte getIORegisterByte(int ioreg) {
        return ioregs[ioreg].read();
    }

    /**
     * The <code>getProgramByte()</code> method reads a byte value from the program (Flash) memory. The flash
     * memory generally stores read-only values and the instructions of the program. Care should be taken that
     * the program memory at the specified address does not contain an instruction. This is because, in
     * general, programs should not read instructions as data, and secondly, because no assembler is present
     * in Avrora and therefore the actual byte value of an instruction may not be known.
     *
     * @param address the byte address at which to read
     * @return the byte value of the program memory at the specified address
     * @throws avrora.sim.InterpreterError.AddressOutOfBoundsException if the specified address is not the valid program memory range
     */
    public byte getFlashByte(int address) {
        return flash.read(address);
    }

    /**
     * The <code>getIOReg()</code> method is used to retrieve a reference to the actual <code>IOReg</code>
     * instance stored internally in the state. This is generally only used in the simulator and device
     * implementations, and clients should probably not call this memory directly.
     *
     * @param ioreg the IO register number to retrieve
     * @return a reference to the <code>IOReg</code> instance of the specified IO register
     */
    public ActiveRegister getIOReg(int ioreg) {
        return ioregs[ioreg];
    }

    /**
     * The <code>writeRegisterByte()</code> method writes a value to a general purpose register. This is a
     * destructive update and should only be called from the appropriate places in the simulator.
     *
     * @param reg the register to write the value to
     * @param val the value to write to the register
     */
    protected void writeRegisterByte(LegacyRegister reg, byte val) {
        sram[reg.getNumber()] = val;
    }

    /**
     * The <code>writeRegisterWord</code> method writes a word value to a general purpose register pair. This is
     * a destructive update and should only be called from the appropriate places in the simulator. The
     * specified register and the next register in numerical order are updated with the low-order and
     * high-order byte of the value passed, respectively. The specified register should be less than r31,
     * since r32 (the next register) does not exist.
     *
     * @param reg the low register of the pair to write
     * @param val the word value to write to the register pair
     */
    protected void writeRegisterWord(LegacyRegister reg, int val) {
        byte low = Arithmetic.low(val);
        byte high = Arithmetic.high(val);
        writeRegisterByte(reg, low);
        writeRegisterByte(reg.nextRegister(), high);
    }

    /**
     * The <code>writeRegisterByte()</code> method writes a value to a general purpose register. This is a
     * destructive update and should only be called from the appropriate places in the simulator.
     *
     * @param reg the register to write the value to
     * @param val the value to write to the register
     */
    public void writeRegisterByte(int reg, byte val) {
        sram[reg] = val;
    }

    /**
     * The <code>writeRegisterWord</code> method writes a word value to a general purpose register pair. This is
     * a destructive update and should only be called from the appropriate places in the simulator. The
     * specified register and the next register in numerical order are updated with the low-order and
     * high-order byte of the value passed, respectively. The specified register should be less than r31,
     * since r32 (the next register) does not exist.
     *
     * @param reg the low register of the pair to write
     * @param val the word value to write to the register pair
     */
    public void writeRegisterWord(int reg, int val) {
        byte low = Arithmetic.low(val);
        byte high = Arithmetic.high(val);
        writeRegisterByte(reg, low);
        writeRegisterByte(reg + 1, high);
    }

    /**
     * The <code>writeSREG()</code> method writes the value of the status register. This method should only be
     * called from the appropriate places in the simulator.
     *
     * @param val
     */
    protected void writeSREG(byte val) {
        SREG_reg.write(val);
    }

    /**
     * The <code>writeDataByte()</code> method writes a value to the data memory (SRAM) of the state. This is
     * generally meant for the simulator, related classes, and device implementations to use, but could also
     * be used by debuggers and other tools.
     *
     * @param address the byte address at which to write the value
     * @param val     the value to write
     */
    public void writeDataByte(int address, byte val) {
        MulticastWatch p;

        try {
            // FAST PATH 1: no watches
            if (sram_watches == null) {
                setSRAM(address, val);
                return;
            }

            // FAST PATH 2: no watches for this address
            p = sram_watches[address];
            if (p == null) {
                setSRAM(address, val);
                return;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            SimUtil.writeError(simulator, "sram", address, val);
            return;
        }

        // SLOW PATH: consult with memory watches
        p.fireBeforeWrite(state, address, val);
        setSRAM(address, val);
        p.fireAfterWrite(state, address, val);
    }

    /**
     * The <code>writeFlashByte()</code> method updates the flash memory one byte at a time.
     * WARNING: this method should NOT BE CALLED UNLESS EXTREME CARE IS TAKEN. The program
     * cannot alter its own flash data except through the flash writing procedure supported
     * in <code>ReprogrammableCodeSegment</code>. This method is only meant for updating
     * node ID's that are stored in flash. DO NOT USE during execution!
     * @param address the address of the byte in flash
     * @param val the new value to write into the flash
     */
    public void writeFlashByte(int address, byte val) {
        flash.set(address, val);
    }

    private void setSRAM(int address, byte val) {
        if (address >= sram_start)
            sram[address] = val;
        else if (address >= LegacyState.NUM_REGS)
            ioregs[address - LegacyState.NUM_REGS].write(val);
        else
            sram[address] = val;
    }

    /**
     * The <code>installIOReg()</code> method installs the specified <code>IOReg</code> object to the specified IO
     * register number. This method is generally only used in the simulator and in device implementations to
     * set up the state correctly during initialization.
     *
     * @param ioreg the IO register number
     * @param reg   the <code>IOReg<code> object to install
     */
    public void installIOReg(int ioreg, ActiveRegister reg) {
        ioregs[ioreg] = reg;
    }

    /**
     * The <code>writeIORegisterByte()</code> method writes a value to the specified IO register. This is
     * generally only used internally to the simulator and device implementations, and client interfaces
     * should probably not call this method.
     *
     * @param ioreg the IO register number to which to write the value
     * @param val   the value to write to the IO register
     */
    public void writeIORegisterByte(int ioreg, byte val) {
        ioregs[ioreg].write(val);
    }

    /**
     * The <code>popByte()</code> method pops a byte from the stack by reading from the address pointed to by
     * SP+1 and incrementing the stack pointer. This method, like all of the other methods that change the
     * state, should probably only be used within the simulator. This method should not be called with an
     * empty stack, as it will cause an exception consistent with trying to read non-existent memory.
     *
     * @return the value on the top of the stack
     */
    public byte popByte() {
        int address = getSP() + 1;
        setSP(address);
        return getDataByte(address);
    }

    /**
     * The <code>pushByte()</code> method pushes a byte onto the stack by writing to the memory address
     * pointed to by the stack pointer and decrementing the stack pointer. This method, like all of the other
     * methods that change the state, should probably only be used within the simulator.
     *
     * @param val the value to push onto the stack
     */
    public void pushByte(byte val) {
        int address = getSP();
        setSP(address - 1);
        writeDataByte(address, val);
    }

    /**
     * The <code>setSP()</code> method updates the value of the stack pointer. Generally the stack pointer is
     * stored in two IO registers <code>SPL</code> and <code>SPH</code>. This method should generally only be
     * used within the simulator.
     *
     * @param val
     */
    public void setSP(int val) {
        SPL_reg.value = (Arithmetic.low(val));
        SPH_reg.value = (Arithmetic.high(val));
    }

    /**
     * This method sets the booting address of the interpreter. It should only be used before execution begins.
     *
     * @param npc the new PC to boot this interpreter from
     */
    public void setBootPC(int npc) {
        bootPC = npc;
    }

    /**
     * The <code>getInterruptBase()</code> method returns the base address of the interrupt table.
     * @return the base address of the interrupt table
     */
    public int getInterruptBase() {
        return interruptBase;
    }

    /**
     * The <code>setInterruptBase()</code> method sets the base of the interrupt table.
     * @param npc the new base of the interrupt table
     */
    public void setInterruptBase(int npc) {
        interruptBase = npc;
    }

    /**
     * The <code>getInstr()</code> can be used to retrieve a reference to the <code>LegacyInstr</code> object
     * representing the instruction at the specified program address. Care should be taken that the address in
     * program memory specified does not contain data. This is because Avrora does have a functioning
     * disassembler and assumes that the <code>LegacyInstr</code> objects for each instruction in the program are
     * known a priori.
     *
     * @param address the byte address from which to read the instruction
     * @return a reference to the <code>LegacyInstr</code> object representing the instruction at that address in
     *         the program; null if there is no instruction at the specified address
     */
    public LegacyInstr getInstr(int address) {
        return flash.readInstr(address);
    }

    /**
     * The <code>getSP()</code> method reads the current value of the stack pointer. Since the stack pointer
     * is stored in two IO registers, this method will cause the invocation of the <code>.read()</code> method
     * on each of the <code>IOReg</code> objects that store these values.
     *
     * @return the value of the stack pointer as a byte address
     */
    public int getSP() {
        byte low = SPL_reg.value;
        byte high = SPH_reg.value;
        return Arithmetic.uword(low, high);
    }

    /**
     * The <code>enableInterrupts()</code> method enables all of the interrupts.
     */
    public void enableInterrupts() {
        I = true;
        innerLoop = false;
        interrupts.enableAll();
    }

    /**
     * The <code>disableInterrupts()</code> method disables all of the interrupts.
     */
    public void disableInterrupts() {
        I = false;
        interrupts.disableAll();
    }

    /**
     * The <code>commit()</code> method is used internally to commit the results of the instructiobn just executed.
     * This should only be used internally.
     */
    protected void commit() {
        pc = nextPC;
        clock.advance(cyclesConsumed);
        cyclesConsumed = 0;
    }

    private class SREG_reg implements ActiveRegister {

        /**
         * The <code>read()</code> method reads the 8-bit value of the IO register as a byte. For simple
         * <code>RWIOReg</code> instances, this simply returns the internally stored value.
         *
         * @return the value of the register as a byte
         */
        public byte read() {
            int value = 0;
            if (I) value |= LegacyState.SREG_I_MASK;
            if (T) value |= LegacyState.SREG_T_MASK;
            if (H) value |= LegacyState.SREG_H_MASK;
            if (S) value |= LegacyState.SREG_S_MASK;
            if (V) value |= LegacyState.SREG_V_MASK;
            if (N) value |= LegacyState.SREG_N_MASK;
            if (Z) value |= LegacyState.SREG_Z_MASK;
            if (C) value |= LegacyState.SREG_C_MASK;
            return (byte) value;
        }

        /**
         * The <code>write()</code> method writes an 8-bit value to the IO register as a byte. For simple
         * <code>RWIOReg</code> instances, this simply writes the internally stored value.
         *
         * @param val the value to write
         */
        public void write(byte val) {
            I = (val & LegacyState.SREG_I_MASK) != 0;
            T = (val & LegacyState.SREG_T_MASK) != 0;
            H = (val & LegacyState.SREG_H_MASK) != 0;
            S = (val & LegacyState.SREG_S_MASK) != 0;
            V = (val & LegacyState.SREG_V_MASK) != 0;
            N = (val & LegacyState.SREG_N_MASK) != 0;
            Z = (val & LegacyState.SREG_Z_MASK) != 0;
            C = (val & LegacyState.SREG_C_MASK) != 0;
        }

        /**
         * The <code>readBit()</code> method reads a single bit from the IO register.
         *
         * @param num the number of the bit to read
         * @return the value of the bit as a boolean
         */
        public boolean readBit(int num) {
            switch (num) {
                case LegacyState.SREG_I:
                    return I;
                case LegacyState.SREG_T:
                    return T;
                case LegacyState.SREG_H:
                    return H;
                case LegacyState.SREG_S:
                    return S;
                case LegacyState.SREG_V:
                    return V;
                case LegacyState.SREG_N:
                    return N;
                case LegacyState.SREG_Z:
                    return Z;
                case LegacyState.SREG_C:
                    return C;
            }
            throw Util.failure("bit out of range: " + num);
        }

        public void writeBit(int num, boolean value) {
            switch (num) {
                case LegacyState.SREG_I:
                    if (value)
                        enableInterrupts();
                    else
                        disableInterrupts();
                    break;
                case LegacyState.SREG_T:
                    T = value;
                    break;
                case LegacyState.SREG_H:
                    H = value;
                    break;
                case LegacyState.SREG_S:
                    S = value;
                    break;
                case LegacyState.SREG_V:
                    V = value;
                    break;
                case LegacyState.SREG_N:
                    N = value;
                    break;
                case LegacyState.SREG_Z:
                    Z = value;
                    break;
                case LegacyState.SREG_C:
                    C = value;
                    break;
                default:
                    throw Util.failure("bit out of range: " + num);
            }
        }
    }
}
