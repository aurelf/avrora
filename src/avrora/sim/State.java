package avrora.sim;

import avrora.Arithmetic;
import avrora.Avrora;
import avrora.core.Elem;
import avrora.core.Instr;
import avrora.core.Program;
import avrora.core.Register;
import avrora.util.StringUtil;
import avrora.util.Terminal;

/**
 * The <code>State</code> class represents the state of the simulator, including
 * the contents of registers and memory.
 * @author Ben L. Titzer
 */
public class State implements IORegisterConstants {

    /**
     * Private variables used to implement the state of the microprocessor.
     */
    private int pc;
    private long cycles;

    private final byte[] regs;
    private final IOReg[] ioregs;
    private byte[] sram;

    private final Elem[] program;
    private long postedInterrupts;

    private final int sram_start;
    private final int sram_max;

    private static final int SRAM_MINSIZE = 128;

    private boolean I, T, H, S, V, N, Z, C;


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
         * @return the value of the register as a byte
         */
        public byte read();

        /**
         * The <code>write()</code> method writes an 8-bit value to the IO register
         * as a byte. For special IO registers, this may cause some action like
         * device activity, masking/unmasking of interrupts, etc.
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
         * @param num the number of the bit to clear
         */
        public void clearBit(int num);

        /**
         * The <code>setBit()</code> method sets a single bit in the IO register.
         * @param num the number of the bit to clear
         */
        public void setBit(int num);
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
         * @return the value of the register as a byte
         */
        public byte read() {
            return value;
        }

        /**
         * The <code>write()</code> method writes an 8-bit value to the IO register
         * as a byte. For simple <code>RWIOReg</code> instances, this simply writes
         * the internally stored value.
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
         * @param num the number of the bit to clear
         */
        public void clearBit(int num) {
            value = Arithmetic.clearBit(value, num);
        }

        /**
         * The <code>setBit()</code> method sets a single bit in the IO register.
         * @param num the number of the bit to clear
         */
        public void setBit(int num) {
            value = Arithmetic.setBit(value, num);
        }
    }

    /**
     * The <code>RESERVED</code> field of the state class represents an instance
     * of the <code>IOReg</code> interface that will not allow any writes to
     * this register to occur. These reserved IO registers are specified in the
     * hardware manuals.
     */
    public static final IOReg RESERVED = new IOReg() {
        public byte read() { return 0; }
        public void write(byte val) { throw new Error("cannot write to reserved register"); }
        public boolean readBit(int num) { return false; }
        public void setBit(int bit) { throw new Error("cannot set bit in reserved register"); }
        public void clearBit(int bit) { throw new Error("cannot clear bit in reserved register"); }
    };

    private static final int SREG_I_MASK = 1 << SREG_I;
    private static final int SREG_T_MASK = 1 << SREG_T;
    private static final int SREG_H_MASK = 1 << SREG_H;
    private static final int SREG_S_MASK = 1 << SREG_S;
    private static final int SREG_V_MASK = 1 << SREG_V;
    private static final int SREG_N_MASK = 1 << SREG_N;
    private static final int SREG_Z_MASK = 1 << SREG_Z;
    private static final int SREG_C_MASK = 1 << SREG_C;

    private class SREG_reg implements IOReg {

        /**
         * The <code>read()</code> method reads the 8-bit value of the IO register
         * as a byte. For simple <code>RWIOReg</code> instances, this simply returns
         * the internally stored value.
         * @return the value of the register as a byte
         */
        public byte read() {
            int value = 0;
            if ( I ) value |= SREG_I_MASK;
            if ( T ) value |= SREG_T_MASK;
            if ( H ) value |= SREG_H_MASK;
            if ( S ) value |= SREG_S_MASK;
            if ( V ) value |= SREG_V_MASK;
            if ( N ) value |= SREG_N_MASK;
            if ( Z ) value |= SREG_Z_MASK;
            if ( C ) value |= SREG_C_MASK;
            return (byte)value;
        }

        /**
         * The <code>write()</code> method writes an 8-bit value to the IO register
         * as a byte. For simple <code>RWIOReg</code> instances, this simply writes
         * the internally stored value.
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
            switch ( num ) {
                case SREG_I: return I;
                case SREG_T: return T;
                case SREG_H: return H;
                case SREG_S: return S;
                case SREG_V: return V;
                case SREG_N: return N;
                case SREG_Z: return Z;
                case SREG_C: return C;
            }
            throw Avrora.failure("bit out of range: "+num);
        }

        /**
         * The <code>clearBit()</code> method clears a single bit in the IO register.
         * @param num the number of the bit to clear
         */
        public void clearBit(int num) {
            switch ( num ) {
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
                    throw Avrora.failure("bit out of range: "+num);
            }
        }

        /**
         * The <code>setBit()</code> method sets a single bit in the IO register.
         * @param num the number of the bit to clear
         */
        public void setBit(int num) {
            switch ( num ) {
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
                    throw Avrora.failure("bit out of range: "+num);
            }
        }
    }





    /**
     * The constructor for the <code>State</code> class builds the internal data
     * structures needed to store the complete state of the machine, including registers,
     * IO registers, the SRAM, and the flash. All IO registers are initialized to be
     * instances of <code>RWIOReg</code>. Reserved and special IO registers must be
     * inserted by the <code>getIOReg()</code> and <code>setIOReg()</code>
     * methods.
     *
     * @param p the program to construct the state for
     * @param flash_size the size of the flash (program) memory in bytes
     * @param ioreg_size the number of IO registers
     * @param sram_size the size of the SRAM in bytes
     */
    public State(Program p, int flash_size, int ioreg_size, int sram_size) {

        // if program will not fit onto hardware, error
        if ( p.program_end > flash_size)
            throw Avrora.failure("program will not fit into " + flash_size + " bytes");

        // allocate register values
        regs = new byte[NUM_REGS];

        // beginning address of SRAM array
        sram_start = ioreg_size + NUM_REGS;

        // maximum data address
        sram_max = NUM_REGS + ioreg_size + sram_size;

        // make array of IO registers
        ioregs = new IOReg[ioreg_size];

        // sram is lazily allocated, only allocate first SRAM_MINSIZE bytes
        sram = new byte[sram_size > SRAM_MINSIZE ? SRAM_MINSIZE : sram_size];

        // make a local copy of the program's instructions
        program = p.makeImpression();

        flag_delta = new boolean[8];
        reg_delta = new boolean[NUM_REGS];

        // initialize IO registers to default values
        initializeIORegs();
    }

    protected void initializeIORegs() {
        for ( int cntr = 0; cntr < ioregs.length; cntr++ )
            ioregs[cntr] = new RWIOReg();
        ioregs[SREG] = new SREG_reg();
    }

    /**
     * The <code>getPostedInterrupts()</code> method returns a mask that represents
     * all interrupts that are currently pending (meaning they are ready to be
     * fired in priority order as long as the I flag is on).
     * @return a mask representing the interrupts which are posted for processing
     */
    public long getPostedInterrupts() {
        return postedInterrupts;
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

    /**
     * The <code>unpostInterrupt()</code> method is generally only used within the
     * simulator which does pre-processing of interrupts before it posts them
     * into the internal <code>State</code> instance. This method causes the
     * specified interrupt number to be removed from the pending list of interrupts
     * Clients should not use this method directly.
     *
     * @param num the interrupt number to post
     */
    public void unpostInterrupt(int num) {
        postedInterrupts &= ~(1 << num);
    }

    /**
     * The <code>setPostedInterrupts()</code> method allows a full update to the
     * pending list of interrupts. This should only be used within the simulator.
     * @param mask the new pending interrupt mask
     */
    public void setPostedInterrupts(long mask) {
        postedInterrupts = mask;
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
     * @param reg the register to write the value to
     * @param val the value to write to the register
     */
    public void setRegisterByte(Register reg, byte val) {
        regs[reg.getNumber()] = val;
    }

    /**
     * The <code>setRegisterWord</code> method writes a word value to a general
     * purpose register pair. This is a destructive update and should only be
     * called from the appropriate places in the simulator. The specified register
     * and the next register in numerical order are updated with the low-order and
     * high-order byte of the value passed, respectively. The specified register should
     * be less than r31, since r32 (the next register) does not exist.
     * @param reg the low register of the pair to write
     * @param val the word value to write to the register pair
     */
    public void setRegisterWord(Register reg, int val) {
        byte low = Arithmetic.low(val);
        byte high = Arithmetic.high(val);
        setRegisterByte(reg, low);
        setRegisterByte(reg.nextRegister(), high);
    }

    /**
     * The <code>getSREG()</code> method reads the value of the status register.
     * The status register contains the I, T, H, S, V, N, Z, and C flags, in order
     * from highest-order to lowest-order.
     * @return the value of the status register as a byte.
     */
    public byte getSREG() {
        return ioregs[SREG].read();
    }

    /**
     * The <code>setSREG()</code> method writes the value of the status register.
     * This method should only be called from the appropriate places in the simulator.
     * @param val
     */
    public void setSREG(byte val) {
        ioregs[SREG].write(val);
    }

    /**
     * The <code>setSREG_bit()</code> updates the value of the specified bit in the
     * status register. It should only be called from the appropriate places in the
     * simulator.
     * @param bit the number of the bit to update
     * @param val the new value of the bit as a boolean
     */
    public void setSREG_bit(int bit, boolean val) {
        if ( val ) ioregs[SREG].setBit(bit);
        else ioregs[SREG].clearBit(bit);
    }

    /**
     * The <code>setFlag_I()</code> method updates the value of the I bit in the status
     * register. It should only be called from within the simulator.
     * @param val the new value of the flag
     */
    public void setFlag_I(boolean val) { I = val; }

    /**
     * The <code>setFlag_T()</code> method updates the value of the T bit in the status
     * register. It should only be called from within the simulator.
     * @param val the new value of the flag
     */
    public void setFlag_T(boolean val) { T = val; }

    /**
     * The <code>setFlag_H()</code> method updates the value of the H bit in the status
     * register. It should only be called from within the simulator.
     * @param val the new value of the flag
     */
    public void setFlag_H(boolean val) { H = val; }

    /**
     * The <code>setFlag_S()</code> method updates the value of the S bit in the status
     * register. It should only be called from within the simulator.
     * @param val the new value of the flag
     */
    public void setFlag_S(boolean val) { S = val; }

    /**
     * The <code>setFlag_V()</code> method updates the value of the V bit in the status
     * register. It should only be called from within the simulator.
     * @param val the new value of the flag
     */
    public void setFlag_V(boolean val) { V = val; }

    /**
     * The <code>setFlag_N()</code> method updates the value of the N bit in the status
     * register. It should only be called from within the simulator.
     * @param val the new value of the flag
     */
    public void setFlag_N(boolean val) { N = val; }

    /**
     * The <code>setFlag_Z()</code> method updates the value of the Z bit in the status
     * register. It should only be called from within the simulator.
     * @param val the new value of the flag
     */
    public void setFlag_Z(boolean val) { Z = val; }

    /**
     * The <code>setFlag_C()</code> method updates the value of the C bit in the status
     * register. It should only be called from within the simulator.
     * @param val the new value of the flag
     */
    public void setFlag_C(boolean val) { C = val; }

    /**
     * The <code>getFlag_I()</code> method returns the current value of the I bit
     * in the status register as a boolean.
     * @return the value of the flag
     */
    public boolean getFlag_I() { return I; }

    /**
     * The <code>getFlag_T()</code> method returns the current value of the T bit
     * in the status register as a boolean.
     * @return the value of the flag
     */
    public boolean getFlag_T() { return T; }

    /**
     * The <code>getFlag_H()</code> method returns the current value of the H bit
     * in the status register as a boolean.
     * @return the value of the flag
     */
    public boolean getFlag_H() { return H; }

    /**
     * The <code>getFlag_S()</code> method returns the current value of the S bit
     * in the status register as a boolean.
     * @return the value of the flag
     */
    public boolean getFlag_S() { return S; }

    /**
     * The <code>getFlag_V()</code> method returns the current value of the V bit
     * in the status register as a boolean.
     * @return the value of the flag
     */
    public boolean getFlag_V() { return V; }

    /**
     * The <code>getFlag_N()</code> method returns the current value of the N bit
     * in the status register as a boolean.
     * @return the value of the flag
     */
    public boolean getFlag_N() { return N; }

    /**
     * The <code>getFlag_Z()</code> method returns the current value of the Z bit
     * in the status register as a boolean.
     * @return the value of the flag
     */
    public boolean getFlag_Z() { return Z; }

    /**
     * The <code>getFlag_C()</code> method returns the current value of the C bit
     * in the status register as a boolean.
     * @return the value of the flag
     */
    public boolean getFlag_C() { return C; }

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
     * The <code>getSP()</code> method reads the current value of the stack pointer.
     * Since the stack pointer is stored in two IO registers, this method will cause the
     * invocation of the <code>.read()</code> method on each of the <code>IOReg</code>
     * objects that store these values.
     * @return the value of the stack pointer as a byte address
     */
    public int getSP() {
        return Arithmetic.uword(ioregs[SPL].read(), ioregs[SPH].read());
    }

    /**
     * The <code>setSP()</code> method updates the value of the stack pointer. Generally
     * the stack pointer is stored in two IO registers <code>SPL</code> and <code>SPH</code>.
     * This method should generally only be used within the simulator.
     *
     * @see IORegisterConstants
     * @param val
     */
    public void setSP(int val) {
        ioregs[SPL].write(Arithmetic.low(val));
        ioregs[SPH].write(Arithmetic.high(val));
    }

    /**
     * The <code>getPC()</code> retrieves the current program counter.
     * @return the program counter as a byte address
     */
    public int getPC() {
        return pc;
    }

    /**
     * The <code>setPC()</code> method updates the value of the program counter. It is
     * generally used only by the simulator. In general it is a good idea to keep the
     * program counter aligned on a 2-byte boundary. Clients of the <code>State</code> interface
     * should generally not use this method.
     * @param pc the new program counter as a byte address
     */
    public void setPC(int pc) {
        this.pc = pc;
    }

    /**
     * The <code>getCurrentInstr()</code> method returns a reference to the
     * <code>Instr</code> object representing the instruction at the address of
     * the current value of the program counter.
     * @return a reference to the <code>Instr</code> object representing the instruction
     * at the current program counter
     */
    public Instr getCurrentInstr() {
        return program[pc].asInstr(pc);
    }

    /**
     * The <code>getInstr()</code> can be used to retrieve a reference to the
     * <code>Instr</code> object representing the instruction at the specified program
     * address. Care should be taken that the address in program memory specified does
     * not contain data. This is because Avrora does have a functioning disassembler
     * and assumes that the <code>Instr</code> objects for each instruction in the
     * program are known a priori.
     * @param address the byte address from which to read the instruction
     * @return a reference to the <code>Instr</code> object representing the instruction
     * at that address in the program
     */
    public Instr getInstr(int address) {
        return program[address].asInstr(address);
    }

    /**
     * The <code>setInstr()</code> method is used internally to update the instructions
     * of the program by the simulator. This is generally for the purpose of replacing
     * an instruction with a <code>Simulator.ProbedInstr</code> instance that will fire
     * probes when it is visited. It is generally not recommended for clients of the
     * <code>State</code> interface to update instructions in the program memory.
     *
     * @param i the instruction to write
     * @param address the byte address in the program to write the instruction to
     */
    public void setInstr(Instr i, int address) {
        program[address] = i;
        for (int cntr = 1; cntr < i.getSize(); cntr++)
            program[address + cntr] = Elem.INSTR_MIDDLE;
    }

    /**
     * The <code>getDataByte()</code> method reads a byte value from the data memory
     * (SRAM) at the specified address.
     * @param address the byte address to read
     * @throws ArrayIndexOutOfBoundsException if the specified address is not the valid
     * memory range
     * @return the value of the data memory at the specified address
     */
    public byte getDataByte(int address) {
        if ( address < NUM_REGS ) return regs[address];
        if ( address < sram_start) return ioregs[address - NUM_REGS].read();
        try {
            return sram[address - sram_start];
        } catch ( ArrayIndexOutOfBoundsException e) {
            resize(address);
            return sram[address - sram_start];
        }
    }

    private void resize(int address) {
        // no hope if the address is simply invalid.
        if ( address >= sram_max ) return;

        // double size until address is contained in new array
        int size = sram.length;
        while ( size <= address ) size <<= 1;

        // make sure we don't allocate more than the hardware limit
        if ( size > sram_max - sram_start ) size = sram_max - sram_start;

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
     * @throws ArrayIndexOutOfBoundsException if the specified address is not the valid
     * program memory range
     * @return the byte value of the program memory at the specified address
     */
    public byte getProgramByte(int address) {
        return program[address].asData(address).value;
    }

    /**
     * The <code>setDataByte()</code> method writes a value to the data
     * memory (SRAM) of the state. This is generally meant for the simulator, related
     * classes, and device implementations to use, but could also be used by
     * debuggers and other tools.
     * @param val the value to write
     * @param address the byte address at which to write the value
     */
    public void setDataByte(byte val, int address) {
        if ( address < NUM_REGS ) regs[address] = val;
        else if ( address < sram_start) ioregs[address - NUM_REGS].write(val);
        else try {
            sram[address - sram_start] = val;
        } catch ( ArrayIndexOutOfBoundsException e) {
            resize(address);
            sram[address - sram_start] = val;
        }
    }

    /**
     * The <code>getIORegisterByte()</code> method reads the value of an IO register.
     * Invocation of this method causes an invocatiobn of the <code>.read()</code>
     * method on the corresponding internal <code>IOReg</code> object, and its value
     * returned.
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
     * @param ioreg the IO register number
     * @param reg the <code>IOReg<code> object to install
     */
    public void setIOReg(int ioreg, IOReg reg) {
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
    public IOReg getIOReg(int ioreg) {
        return ioregs[ioreg];
    }

    /**
     * The <code>setIORegisterByte()</code> method writes a value to the specified
     * IO register. This is generally only used internally to the simulator and
     * device implementations, and client interfaces should probably not call
     * this method.
     *
     * @param val the value to write to the IO register
     * @param ioreg the IO register number to which to write the value
     */
    public void setIORegisterByte(byte val, int ioreg) {
        ioregs[ioreg].write(val);
    }

    /**
     * The <code>getCycles()</code> method returns the clock cycle count recorded
     * so far in the simulation.
     * @return the number of clock cycles elapsed in the simulation
     */
    public long getCycles() {
        return cycles;
    }

    /**
     * The <code>consumeCycle()</code> method increments the cycle count of
     * the state by one. This is generally only used internally to the simulator
     * and tightly coupled classes, so clients of the state interface should not
     * call this method unless they are implementing careful timings (e.g. an
     * external RAM).
     *
     */
    public void consumeCycle() {
        cycles++;
    }

    /**
     * The <code>consumeCycles()</code> method increments the cycle count of
     * the state by the specified number of cycles. This is generally only used
     * internally to the simulator and tightly coupled classes, so clients of the
     * state interface should not call this method unless they are implementing
     * careful timings (e.g. an external RAM).
     *
     * @param num the number of cycles to advance the cycle counter
     */
    public void consumeCycles(long num) {
        cycles += num;
    }

    /**
     *  D E B U G G I N G   A N D   T R A C I N G   S U P P O R T
     * ----------------------------------------------------------------
     *
     * These utilities allow the state to be output to the console
     * in a stylized fashion for visual inspection.
     *
     */

    /**
     * Delta lists of things that have changed recently.
     * These things are manipulated in the subclass <code>TracingState</code>.
     */
    protected final boolean[] reg_delta;
    protected final boolean[] flag_delta;
    protected boolean sp_delta;
    protected boolean pc_delta;

    public void dump() {
        int sp = getSP();
        byte sreg = getSREG();

        Terminal.print("    ");
        printPair("PC", pc, pc_delta);
        printPair("CC", cycles, true);
        printPair("I", getBitAsString(sreg, SREG_I), flag_delta[SREG_I]);
        printPair("T", getBitAsString(sreg, SREG_T), flag_delta[SREG_T]);
        printPair("H", getBitAsString(sreg, SREG_H), flag_delta[SREG_H]);
        printPair("S", getBitAsString(sreg, SREG_S), flag_delta[SREG_S]);
        printPair("V", getBitAsString(sreg, SREG_V), flag_delta[SREG_V]);
        printPair("N", getBitAsString(sreg, SREG_N), flag_delta[SREG_N]);
        printPair("Z", getBitAsString(sreg, SREG_Z), flag_delta[SREG_Z]);
        printPair("C", getBitAsString(sreg, SREG_C), flag_delta[SREG_C]);

        Terminal.print("\n    ");

        for (int cntr = 0; cntr < NUM_REGS; cntr++) {
            String n = "R" + cntr + ((cntr < 10) ? " " : "");
            printPair(n, regs[cntr], reg_delta[cntr]);
            if (cntr % 8 == 7) Terminal.print("\n    ");
        }

        printWordRegister(Register.X);
        printWordRegister(Register.Y);
        printWordRegister(Register.Z);

        printPair("SP ", sp, sp_delta);

        int max = sp + 10;
        if ( max > sram.length + sram_start ) max = sram.length + sram_start;

        for (int cntr = sp; cntr < max; cntr++) {
            Terminal.print(StringUtil.toHex(getDataByte(cntr), 2) + " ");
        }

        Terminal.nextln();

        Terminal.printBrightGreen("           0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F");
        Terminal.nextln();

        for (int row = NUM_REGS; row < 128; row += 16) {
            Terminal.printBrightGreen("    " + StringUtil.toHex(row >> 4, 3) + "x");
            Terminal.print(": ");
            for (int cntr = 0; cntr < 16; cntr++)
                Terminal.print(StringUtil.toHex(getDataByte(row + cntr), 2) + " ");
            Terminal.nextln();
        }
    }

    private String getBitAsString(byte val, int bit) {
        return (Arithmetic.getBit(val, bit) ? "1" : "0");
    }

    private void printWordRegister(Register r) {
        Register Ra = r;
        Register Rb = r.nextRegister();
        byte low = getRegisterByte(Ra);
        byte high = getRegisterByte(Rb);
        boolean modified = reg_delta[Ra.getNumber()] || reg_delta[Rb.getNumber()];
        printPair(r.getName().toUpperCase(), Arithmetic.uword(low, high), modified);
    }

    protected final void printPair(String n, String str, boolean modified) {
        Terminal.printBrightGreen(n);
        Terminal.print(": ");
        if (modified)
            Terminal.printRed(str + " ");
        else
            Terminal.print(str + " ");
    }

    protected final void printPair(String n, byte val, boolean modified) {
        String str = StringUtil.toHex(val, 2);
        printPair(n, str, modified);
    }

    protected final void printPair(String n, int val, boolean modified) {
        String str = StringUtil.toHex(val, 4);
        printPair(n, str, modified);
    }

    protected final void printPair(String n, long val, boolean modified) {
        String str = StringUtil.toHex(val, 10);
        printPair(n, str, modified);
    }

    public void clearTracingState() {
    }
}
