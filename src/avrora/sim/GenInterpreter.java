package avrora.sim;

import avrora.core.InstrVisitor;
import avrora.core.Program;
import avrora.core.Instr;
import avrora.core.Register;
import avrora.Avrora;
import avrora.sim.util.MulticastProbe;
import avrora.util.Arithmetic;

/**
 * @author Ben L. Titzer
 */
public abstract class GenInterpreter implements InstrVisitor, State {
    private int pc;
    private long cycles;

    private final byte[] regs;
    private final State.IOReg[] ioregs;
    private byte[] sram;

    private final Program.Impression impression;
    private long postedInterrupts;

    private final int sram_start;
    private final int sram_max;

    private static final int SRAM_MINSIZE = 128;

    private boolean I, T, H, S, V, N, Z, C;

    private IOReg SREG_reg;

    /**
     * The <code>nextPC</code> field is used internally in maintaining the correct
     * execution order of the instructions.
     */
    protected int nextPC;

    protected int cyclesConsumed;

    /**
     * The <code>shouldRun</code> flag is used internally in the main execution
     * runLoop to implement the correct semantics of <code>start()</code> and
     * <code>stop()</code> to the clients.
     */
    protected boolean shouldRun;

    /**
     * The <code>sleeping</code> flag is used internally in the simulator when the
     * microcontroller enters the sleep mode.
     */
    protected boolean sleeping;

    /**
     * The <code>justReturnedFromInterrupt</code> field is used internally in
     * maintaining the invariant stated in the hardware manual that at least one
     * instruction following a return from an interrupt is executed before another
     * interrupt can be processed.
     */
    protected boolean justReturnedFromInterrupt;

    protected final Simulator simulator;

    /**
     * The constructor for the <code>Interpreter</code> class builds the internal data
     * structures needed to store the complete state of the machine, including registers,
     * IO registers, the SRAM, and the flash. All IO registers are initialized to be
     * instances of <code>RWIOReg</code>. Reserved and special IO registers must be
     * inserted by the <code>getIOReg()</code> and <code>setIOReg()</code>
     * methods.
     *
     * @param p          the program to construct the state for
     * @param flash_size the size of the flash (program) memory in bytes
     * @param ioreg_size the number of IO registers
     * @param sram_size  the size of the SRAM in bytes
     */
    protected GenInterpreter(Simulator s, Program p, int flash_size, int ioreg_size, int sram_size) {

        // initialize the reference to the simulator
        simulator = s;

        // if program will not fit onto hardware, error
        if (p.program_end > flash_size)
            throw Avrora.failure("program will not fit into " + flash_size + " bytes");

        // allocate register values
        regs = new byte[NUM_REGS];

        // beginning address of SRAM array
        sram_start = ioreg_size + NUM_REGS;

        // maximum data address
        sram_max = NUM_REGS + ioreg_size + sram_size;

        // make array of IO registers
        ioregs = new State.IOReg[ioreg_size];

        // sram is lazily allocated, only allocate first SRAM_MINSIZE bytes
        sram = new byte[sram_size > SRAM_MINSIZE ? SRAM_MINSIZE : sram_size];

        // make a local copy of the program's instructions
        impression = p.makeNewImpression(flash_size);

        // initialize IO registers to default values
        initializeIORegs();
    }

    protected void runLoop() {

        nextPC = pc;
        cyclesConsumed = 0;

        while (shouldRun) {

            if (justReturnedFromInterrupt) {
                // don't process the interrupt if we just returned from
                // an interrupt handler, because the hardware manual says
                // that at least one instruction is executed after
                // returning from an interrupt.
                justReturnedFromInterrupt = false;
            } else if ( I ) {

                // check if there are any pending (posted) interrupts
                if (postedInterrupts != 0) {
                    // the lowest set bit is the highest priority posted interrupt
                    int lowestbit = Arithmetic.lowestBit(postedInterrupts);

                    // fire the interrupt (update flag register(s) state)
                    simulator.triggerInterrupt(lowestbit);

                    // store the return address
                    pushPC(nextPC);

                    // set PC to interrupt handler
                    nextPC = simulator.getInterruptVectorAddress(lowestbit);
                    pc = nextPC;

                    // disable interrupts
                    I = false;

                    // process any timed events
                    advanceCycles(4);
                    sleeping = false;
                }
            }

            if ( sleeping ) {
                long delta = simulator.eventQueue.getHeadDelta();
                if ( delta <= 0 ) delta = 1;
                advanceCycles(delta);
            } else {
                // get the current instruction
                int curPC = nextPC; // at this point pc == nextPC
                Instr i = impression.readInstr(nextPC);

                // visit the actual instruction (or probe)
                simulator.activeProbe.fireBefore(i, curPC, this);
                i.accept(this);
                pc = nextPC;
                advanceCycles(cyclesConsumed);
                simulator.activeProbe.fireAfter(i, curPC, this);
            }
        }
    }

    protected void insertProbe(Simulator.Probe p, int addr) {
        Simulator.ProbedInstr pi = getProbedInstr(addr);
        if (pi != null)
            pi.add(p);
        else {
            // TODO: fix me
            // pi = new Simulator.ProbedInstr(impression.readInstr(addr), addr, p);
            // impression.writeInstr(pi, addr);
        }
    }

    protected void removeProbe(Simulator.Probe p, int addr) {
        Simulator.ProbedInstr pi = getProbedInstr(addr);
        if (pi != null) {
            pi.remove(p);
            if (pi.isEmpty())
                impression.writeInstr(pi.instr, pi.address);
        }
    }

    protected void insertBreakPoint(int addr) {
        Simulator.ProbedInstr pi = getProbedInstr(addr);
        if (pi != null)
            pi.setBreakPoint();
        else {
            // TODO: fix me
            // pi = new Simulator.ProbedInstr(impression.readInstr(addr), addr, null);
            // impression.writeInstr(pi, addr);
            // pi.setBreakPoint();
        }
    }

    protected void removeBreakPoint(int addr) {
        Simulator.ProbedInstr pi = getProbedInstr(addr);
        if (pi != null) pi.unsetBreakPoint();
    }

    private Simulator.ProbedInstr getProbedInstr(int addr) {
        Instr i = impression.readInstr(addr);
        if (i instanceof Simulator.ProbedInstr)
            return ((Simulator.ProbedInstr) i);
        else
            return null;
    }


    protected void initializeIORegs() {
        for (int cntr = 0; cntr < ioregs.length; cntr++)
            ioregs[cntr] = new State.RWIOReg();
        SREG_reg = ioregs[SREG] = new SREG_reg();
    }

//--BEGIN INTERPRETER GENERATOR--

//--END INTERPRETER GENERATOR--

    //
    //  U T I L I T I E S
    // ------------------------------------------------------------
    //
    //  These are utility functions for expressing instructions
    //  more concisely. They are private and can be inlined by
    //  the JIT compiler or javac -O.
    //
    //

    private void pushPC(int npc) {
        npc = npc / 2;
        pushByte(Arithmetic.low(npc));
        pushByte(Arithmetic.high(npc));
    }

    private int popPC() {
        byte high = popByte();
        byte low = popByte();
        return Arithmetic.uword(low, high) * 2;
    }

    private void unimplemented(Instr i) {
        throw Avrora.failure("unimplemented instruction: " + i.getVariant());
    }

    private boolean xor(boolean a, boolean b) {
        return (a && !b) || (b && !a);
    }

    private byte low(int val) {
        return (byte)val;
    }

    private byte high(int val) {
        return (byte)(val >> 8);
    }

    private void advanceCycles(long delta) {
        cycles += delta;
        simulator.eventQueue.advance(delta);
        cyclesConsumed = 0;
    }

    private static final int SREG_I_MASK = 1 << SREG_I;
    private static final int SREG_T_MASK = 1 << SREG_T;
    private static final int SREG_H_MASK = 1 << SREG_H;
    private static final int SREG_S_MASK = 1 << SREG_S;
    private static final int SREG_V_MASK = 1 << SREG_V;
    private static final int SREG_N_MASK = 1 << SREG_N;
    private static final int SREG_Z_MASK = 1 << SREG_Z;
    private static final int SREG_C_MASK = 1 << SREG_C;

    private class SREG_reg implements State.IOReg {

        /**
         * The <code>read()</code> method reads the 8-bit value of the IO register
         * as a byte. For simple <code>RWIOReg</code> instances, this simply returns
         * the internally stored value.
         *
         * @return the value of the register as a byte
         */
        public byte read() {
            int value = 0;
            if (I) value |= SREG_I_MASK;
            if (T) value |= SREG_T_MASK;
            if (H) value |= SREG_H_MASK;
            if (S) value |= SREG_S_MASK;
            if (V) value |= SREG_V_MASK;
            if (N) value |= SREG_N_MASK;
            if (Z) value |= SREG_Z_MASK;
            if (C) value |= SREG_C_MASK;
            return (byte) value;
        }

        /**
         * The <code>write()</code> method writes an 8-bit value to the IO register
         * as a byte. For simple <code>RWIOReg</code> instances, this simply writes
         * the internally stored value.
         *
         * @param val the value to write
         */
        public void write(byte val) {
            I = (val & SREG_I_MASK) != 0;
            T = (val & SREG_T_MASK) != 0;
            H = (val & SREG_H_MASK) != 0;
            S = (val & SREG_S_MASK) != 0;
            V = (val & SREG_V_MASK) != 0;
            N = (val & SREG_N_MASK) != 0;
            Z = (val & SREG_Z_MASK) != 0;
            C = (val & SREG_C_MASK) != 0;
        }

        /**
         * The <code>readBit()</code> method reads a single bit from the IO register.
         *
         * @param num the number of the bit to read
         * @return the value of the bit as a boolean
         */
        public boolean readBit(int num) {
            switch (num) {
                case SREG_I:
                    return I;
                case SREG_T:
                    return T;
                case SREG_H:
                    return H;
                case SREG_S:
                    return S;
                case SREG_V:
                    return V;
                case SREG_N:
                    return N;
                case SREG_Z:
                    return Z;
                case SREG_C:
                    return C;
            }
            throw Avrora.failure("bit out of range: " + num);
        }

        /**
         * The <code>clearBit()</code> method clears a single bit in the IO register.
         *
         * @param num the number of the bit to clear
         */
        public void clearBit(int num) {
            switch (num) {
                case SREG_I:
                    I = false;
                    break;
                case SREG_T:
                    T = false;
                    break;
                case SREG_H:
                    H = false;
                    break;
                case SREG_S:
                    S = false;
                    break;
                case SREG_V:
                    V = false;
                    break;
                case SREG_N:
                    N = false;
                    break;
                case SREG_Z:
                    Z = false;
                    break;
                case SREG_C:
                    C = false;
                    break;
                default:
                    throw Avrora.failure("bit out of range: " + num);
            }
        }

        /**
         * The <code>setBit()</code> method sets a single bit in the IO register.
         *
         * @param num the number of the bit to clear
         */
        public void setBit(int num) {
            switch (num) {
                case SREG_I:
                    I = true;
                    break;
                case SREG_T:
                    T = true;
                    break;
                case SREG_H:
                    H = true;
                    break;
                case SREG_S:
                    S = true;
                    break;
                case SREG_V:
                    V = true;
                    break;
                case SREG_N:
                    N = true;
                    break;
                case SREG_Z:
                    Z = true;
                    break;
                case SREG_C:
                    C = true;
                    break;
                default:
                    throw Avrora.failure("bit out of range: " + num);
            }
        }

        public void writeBit(int num, boolean value) {
            if ( value ) setBit(num);
            else clearBit(num);
        }
    }

    /**
     * Read a general purpose register's current value as a byte.
     *
     * @param reg the register to read
     * @return the current value of the register
     */
    public byte getRegisterByte(Register reg) {
        return regs[reg.getNumber()];
    }

    /**
     * Read a general purpose register's current value as an integer, without any sign
     * extension.
     *
     * @param reg the register to read
     * @return the current unsigned value of the register
     */
    public int getRegisterUnsigned(Register reg) {
        return regs[reg.getNumber()] & 0xff;
    }

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
    public int getRegisterWord(Register reg) {
        byte low = getRegisterByte(reg);
        byte high = getRegisterByte(reg.nextRegister());
        return Arithmetic.uword(low, high);
    }

    /**
     * The <code>setRegisterByte()</code> method writes a value to a general purpose
     * register. This is a destructive update and should only be called from the
     * appropriate places in the simulator.
     *
     * @param reg the register to write the value to
     * @param val the value to write to the register
     */
    protected void setRegisterByte(Register reg, byte val) {
        regs[reg.getNumber()] = val;
    }

    /**
     * The <code>setRegisterWord</code> method writes a word value to a general
     * purpose register pair. This is a destructive update and should only be
     * called from the appropriate places in the simulator. The specified register
     * and the next register in numerical order are updated with the low-order and
     * high-order byte of the value passed, respectively. The specified register should
     * be less than r31, since r32 (the next register) does not exist.
     *
     * @param reg the low register of the pair to write
     * @param val the word value to write to the register pair
     */
    protected void setRegisterWord(Register reg, int val) {
        byte low = Arithmetic.low(val);
        byte high = Arithmetic.high(val);
        setRegisterByte(reg, low);
        setRegisterByte(reg.nextRegister(), high);
    }

    /**
     * The <code>getSREG()</code> method reads the value of the status register.
     * The status register contains the I, T, H, S, V, N, Z, and C flags, in order
     * from highest-order to lowest-order.
     *
     * @return the value of the status register as a byte.
     */
    public byte getSREG() {
        return ioregs[SREG].read();
    }

    /**
     * The <code>setSREG()</code> method writes the value of the status register.
     * This method should only be called from the appropriate places in the simulator.
     *
     * @param val
     */
    protected void setSREG(byte val) {
        ioregs[SREG].write(val);
    }

    /**
     * The <code>getDataByte()</code> method reads a byte value from the data memory
     * (SRAM) at the specified address.
     *
     * @param address the byte address to read
     * @return the value of the data memory at the specified address
     * @throws ArrayIndexOutOfBoundsException if the specified address is not the valid
     *                                        memory range
     */
    public byte getDataByte(int address) {
        if (address < NUM_REGS) return regs[address];
        if (address < sram_start) return ioregs[address - NUM_REGS].read();
        try {
            return sram[address - sram_start];
        } catch (ArrayIndexOutOfBoundsException e) {
            resize(address);
            return sram[address - sram_start];
        }
    }

    private void resize(int address) {
        // no hope if the address is simply invalid.
        if (address >= sram_max) return;

        // double size until address is contained in new array
        int size = sram.length;
        while (size <= address) size <<= 1;

        // make sure we don't allocate more than the hardware limit
        if (size > sram_max - sram_start) size = sram_max - sram_start;

        // create new memory
        byte[] new_sram = new byte[size];

        // copy old memory
        System.arraycopy(sram, 0, new_sram, 0, sram.length);

        // update SRAM array reference to point to new memory
        sram = new_sram;
    }

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
    public byte getProgramByte(int address) {
        return impression.readProgramByte(address);
    }

    /**
     * The <code>setDataByte()</code> method writes a value to the data
     * memory (SRAM) of the state. This is generally meant for the simulator, related
     * classes, and device implementations to use, but could also be used by
     * debuggers and other tools.
     *
     * @param val     the value to write
     * @param address the byte address at which to write the value
     */
    public void setDataByte(byte val, int address) {
        if (address < NUM_REGS)
            regs[address] = val;
        else if (address < sram_start)
            ioregs[address - NUM_REGS].write(val);
        else
            try {
                sram[address - sram_start] = val;
            } catch (ArrayIndexOutOfBoundsException e) {
                resize(address);
                sram[address - sram_start] = val;
            }
    }

    /**
     * The <code>getInstrSize()</code> method reads the size of the instruction
     * at the given program address. This is needed in the interpreter to
     * compute the target of a skip instruction (an instruction that skips
     * over the instruction following it).
     * @param npc the program address of the instruction
     * @return the size in bytes of the instruction at the specified
     * program address
     */
    public int getInstrSize(int npc) {
        return simulator.program.readInstr(npc).getSize();
    }

    /**
     * The <code>getIORegisterByte()</code> method reads the value of an IO register.
     * Invocation of this method causes an invocatiobn of the <code>.read()</code>
     * method on the corresponding internal <code>IOReg</code> object, and its value
     * returned.
     *
     * @param ioreg the IO register number
     * @return the value of the IO register
     */
    public byte getIORegisterByte(int ioreg) {
        return ioregs[ioreg].read();
    }

    /**
     * The <code>setIOReg</code> method installs the specified <code>IOReg</code>
     * object to the specified IO register number. This method is generally only used
     * in the simulator and in device implementations to set up the state correctly
     * during initialization.
     *
     * @param ioreg the IO register number
     * @param reg   the <code>IOReg<code> object to install
     */
    public void setIOReg(int ioreg, State.IOReg reg) {
        ioregs[ioreg] = reg;
    }

    /**
     * The <code>getIOReg()</code> method is used to retrieve a reference to
     * the actual <code>IOReg</code> instance stored internally in the state. This is
     * generally only used in the simulator and device implementations, and clients
     * should probably not call this memory directly.
     *
     * @param ioreg the IO register number to retrieve
     * @return a reference to the <code>IOReg</code> instance of the specified IO register
     */
    public State.IOReg getIOReg(int ioreg) {
        return ioregs[ioreg];
    }

    /**
     * The <code>setIORegisterByte()</code> method writes a value to the specified
     * IO register. This is generally only used internally to the simulator and
     * device implementations, and client interfaces should probably not call
     * this method.
     *
     * @param val   the value to write to the IO register
     * @param ioreg the IO register number to which to write the value
     */
    public void setIORegisterByte(byte val, int ioreg) {
        ioregs[ioreg].write(val);
    }

    /**
     * The <code>popByte()</code> method pops a byte from the stack by reading
     * from the address pointed to by SP+1 and incrementing the stack pointer.
     * This method, like all of the other methods that change the state,
     * should probably only be used within the simulator. This method should not
     * be called with an empty stack, as it will cause an exception consistent
     * with trying to read non-existent memory.
     *
     * @return the value on the top of the stack
     */
    public byte popByte() {
        int address = getSP() + 1;
        setSP(address);
        return getDataByte(address);
    }

    /**
     * The <code>pushByte()</code> method pushes a byte onto the stack by writing
     * to the memory address pointed to by the stack pointer and decrementing the
     * stack pointer. This method, like all of the other methods that change the state,
     * should probably only be used within the simulator.
     *
     * @param val the value to push onto the stack
     */
    public void pushByte(byte val) {
        int address = getSP();
        setSP(address - 1);
        setDataByte(val, address);
    }

    /**
     * The <code>setSP()</code> method updates the value of the stack pointer. Generally
     * the stack pointer is stored in two IO registers <code>SPL</code> and <code>SPH</code>.
     * This method should generally only be used within the simulator.
     *
     * @param val
     * @see IORegisterConstants
     */
    protected void setSP(int val) {
        ioregs[SPL].write(Arithmetic.low(val));
        ioregs[SPH].write(Arithmetic.high(val));
    }

    /**
     * The <code>getCycles()</code> method returns the clock cycle count recorded
     * so far in the simulation.
     *
     * @return the number of clock cycles elapsed in the simulation
     */
    public long getCycles() {
        return cycles;
    }

    /**
     * The <code>getPostedInterrupts()</code> method returns a mask that represents
     * all interrupts that are currently pending (meaning they are ready to be
     * fired in priority order as long as the I flag is on).
     *
     * @return a mask representing the interrupts which are posted for processing
     */
    public long getPostedInterrupts() {
        return postedInterrupts;
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
     * The <code>getFlag_I()</code> method returns the current value of the I bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_I() { return I; }

    /**
     * The <code>getFlag_T()</code> method returns the current value of the T bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_T()  { return T; }

    /**
     * The <code>getFlag_H()</code> method returns the current value of the H bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_H()  { return H; }

    /**
     * The <code>getFlag_S()</code> method returns the current value of the S bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_S()  { return S; }

    /**
     * The <code>getFlag_V()</code> method returns the current value of the V bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_V() { return V; }

    /**
     * The <code>getFlag_N()</code> method returns the current value of the N bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_N()  { return N; }

    /**
     * The <code>getFlag_Z()</code> method returns the current value of the Z bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_Z()  { return Z; }

    /**
     * The <code>getFlag_C()</code> method returns the current value of the C bit
     * in the status register as a boolean.
     *
     * @return the value of the flag
     */
    public boolean getFlag_C() { return C; }


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
    public Instr getInstr(int address) {
        return impression.readInstr(address);
    }

    /**
     * The <code>getStackByte()</code> method reads a byte from the address
     * specified by SP+1. This method should not be called with an empty stack,
     * as it will cause an exception consistent with trying to read non-existent
     * memory.
     *
     * @return the value on the top of the stack
     */
    public byte getStackByte() {
        int address = getSP() + 1;
        return getDataByte(address);
    }

    /**
     * The <code>getSP()</code> method reads the current value of the stack pointer.
     * Since the stack pointer is stored in two IO registers, this method will cause the
     * invocation of the <code>.read()</code> method on each of the <code>IOReg</code>
     * objects that store these values.
     *
     * @return the value of the stack pointer as a byte address
     */
    public int getSP() {
        byte low = ioregs[SPL].read();
        byte high = ioregs[SPH].read();
        return Arithmetic.uword(low, high);
    }

    /**
     * The <code>unpostInterrupt()</code> method is generally only used within the
     * simulator which does pre-processing of interrupts before it posts them
     * into the internal <code>State</code> instance. This method causes the
     * specified interrupt number to be removed from the pending list of interrupts
     * Clients should not use this method directly.
     *
     * @param num the interrupt number to post
     */
    protected void unpostInterrupt(int num) {
        postedInterrupts &= ~(1 << num);
    }

    /**
     * The <code>postInterrupt()</code> method is generally only used within the
     * simulator which does pre-processing of interrupts before it posts them
     * into the internal <code>State</code> instance. This method causes the
     * specified interrupt number to be added to the pending list of interrupts
     * Clients should not use this method directly.
     *
     * @param num the interrupt number to post
     */
    public void postInterrupt(int num) {
        postedInterrupts |= 1 << num;
    }

}
