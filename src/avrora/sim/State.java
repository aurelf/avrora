package avrora.sim;

import avrora.core.*;
import avrora.Arithmetic;
import vpc.VPCBase;
import vpc.util.ColorTerminal;

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

    public static final int NUM_REGS = 32;
    private static final int SRAM_MINSIZE = 128;


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







    /**
     * The constructor for the <code>State</code> class builds the internal data
     * structures needed to store the complete state of the machine, including registers,
     * IO registers, the SRAM, and the flash. All IO registers are initialized to be
     * instances of <code>RWIOReg</code>. Reserved and special IO registers must be
     * inserted by the <code>getIORegister()</code> and <code>setIORegister()</code>
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
            throw VPCBase.failure("program will not fit into " + flash_size + " bytes");

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
    }

    public long getPostedInterrupts() {
        return postedInterrupts;
    }

    public void postInterrupt(int num) {
        postedInterrupts |= 1 << num;
    }

    public void unpostInterrupt(int num) {
        postedInterrupts &= ~(1 << num);
    }

    public void setPostedInterrupts(long mask) {
        postedInterrupts = mask;
    }

    public byte readRegister(Register reg) {
        return regs[reg.getNumber()];
    }

    public int readRegisterUnsigned(Register reg) {
        return regs[reg.getNumber()] & 0xff;
    }

    public int readRegisterWord(Register reg) {
        byte low = readRegister(reg);
        byte high = readRegister(reg.nextRegister());
        return Arithmetic.uword(low, high);
    }

    public void writeRegister(Register reg, byte val) {
        regs[reg.getNumber()] = val;
    }

    public void writeRegisterWord(Register reg, int val) {
        byte low = Arithmetic.low(val);
        byte high = Arithmetic.high(val);
        writeRegister(reg, low);
        writeRegister(reg.nextRegister(), high);
    }

    public byte readSREG() {
        return ioregs[SREG].read();
    }

    public byte readRAMPZ() {
        return ioregs[RAMPZ].read();
    }

    public void writeSREG(byte val) {
        ioregs[SREG].write(val);
    }

    public void setSREG_bit(int bit, boolean val) {
        if ( val ) ioregs[SREG].setBit(bit);
        else ioregs[SREG].clearBit(bit);
    }

    public void setFlag_I(boolean val) { setSREG_bit(SREG_I, val); }
    public void setFlag_T(boolean val) { setSREG_bit(SREG_T, val); }
    public void setFlag_H(boolean val) { setSREG_bit(SREG_H, val); }
    public void setFlag_S(boolean val) { setSREG_bit(SREG_S, val); }
    public void setFlag_V(boolean val) { setSREG_bit(SREG_V, val); }
    public void setFlag_N(boolean val) { setSREG_bit(SREG_N, val); }
    public void setFlag_Z(boolean val) { setSREG_bit(SREG_Z, val); }
    public void setFlag_C(boolean val) { setSREG_bit(SREG_C, val); }

    public boolean getFlag_I() { return ioregs[SREG].readBit(SREG_I); }
    public boolean getFlag_T() { return ioregs[SREG].readBit(SREG_T); }
    public boolean getFlag_H() { return ioregs[SREG].readBit(SREG_H); }
    public boolean getFlag_S() { return ioregs[SREG].readBit(SREG_S); }
    public boolean getFlag_V() { return ioregs[SREG].readBit(SREG_V); }
    public boolean getFlag_N() { return ioregs[SREG].readBit(SREG_N); }
    public boolean getFlag_Z() { return ioregs[SREG].readBit(SREG_Z); }
    public boolean getFlag_C() { return ioregs[SREG].readBit(SREG_C); }

    public byte readStackByte() {
        int address = readSP();
        return readDataByte(address);
    }

    public byte popByte() {
        int address = readSP() + 1;
        writeSP(address);
        return readDataByte(address);
    }

    public void pushByte(byte val) {
        int address = readSP();
        writeSP(address - 1);
        writeDataByte(val, address);
    }

    public int readSP() {
        return Arithmetic.uword(ioregs[SPL].read(), ioregs[SPH].read());
    }

    public void writeSP(int val) {
        ioregs[SPL].write(Arithmetic.low(val));
        ioregs[SPH].write(Arithmetic.high(val));
    }

    public int getPC() {
        return pc;
    }

    public void writePC(int pc) {
        this.pc = pc;
    }

    public Instr getCurrentInstr() {
        return program[pc].asInstr(pc);
    }

    public Instr readInstr(int address) {
        return program[address].asInstr(address);
    }

    public void writeInstr(Instr i, int address) {
        program[address] = i;
        for (int cntr = 1; cntr < i.getSize(); cntr++)
            program[address + cntr] = Elem.INSTR_MIDDLE;
    }

    public byte readDataByte(int address) {
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

    public byte readProgramByte(int address) {
        return program[address].asData(address).value;
    }

    public void writeDataByte(byte val, int address) {
        if ( address < NUM_REGS ) regs[address] = val;
        else if ( address < sram_start) ioregs[address - NUM_REGS].write(val);
        else try {
            sram[address - sram_start] = val;
        } catch ( ArrayIndexOutOfBoundsException e) {
            resize(address);
            sram[address - sram_start] = val;
        }
    }

    public byte readIORegister(int ioreg) {
        return ioregs[ioreg].read();
    }

    public void setIORegister(int ioreg, IOReg reg) {
        ioregs[ioreg] = reg;
    }

    public IOReg getIORegister(int ioreg) {
        return ioregs[ioreg];
    }

    public void writeIORegister(byte val, int ioreg) {
        ioregs[ioreg].write(val);
    }

    public long getCycles() {
        return cycles;
    }

    public void consumeCycle() {
        cycles++;
    }

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
     */
    protected final boolean[] reg_delta;
    protected final boolean[] flag_delta;
    protected boolean sp_delta;
    protected boolean pc_delta;

    public void dump() {
        int sp = readSP();
        byte sreg = readSREG();

        ColorTerminal.print("    ");
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

        ColorTerminal.print("\n    ");

        for (int cntr = 0; cntr < NUM_REGS; cntr++) {
            String n = "R" + cntr + ((cntr < 10) ? " " : "");
            printPair(n, regs[cntr], reg_delta[cntr]);
            if (cntr % 8 == 7) ColorTerminal.print("\n    ");
        }

        printWordRegister(Register.X);
        printWordRegister(Register.Y);
        printWordRegister(Register.Z);

        printPair("SP ", sp, sp_delta);

        int max = sp + 10;
        if ( max > sram.length + sram_start ) max = sram.length + sram_start;

        for (int cntr = sp; cntr < max; cntr++) {
            ColorTerminal.print(VPCBase.toPaddedUpperHex(readDataByte(cntr), 2) + " ");
        }

        ColorTerminal.nextln();

        ColorTerminal.printBrightGreen("           0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F");
        ColorTerminal.nextln();

        for (int row = NUM_REGS; row < 128; row += 16) {
            ColorTerminal.printBrightGreen("    " + VPCBase.toPaddedUpperHex(row >> 4, 3) + "x");
            ColorTerminal.print(": ");
            for (int cntr = 0; cntr < 16; cntr++)
                ColorTerminal.print(VPCBase.toPaddedUpperHex(readDataByte(row + cntr), 2) + " ");
            ColorTerminal.nextln();
        }
    }

    private String getBitAsString(byte val, int bit) {
        return (Arithmetic.getBit(val, bit) ? "1" : "0");
    }

    private void printWordRegister(Register r) {
        Register Ra = r;
        Register Rb = r.nextRegister();
        byte low = readRegister(Ra);
        byte high = readRegister(Rb);
        boolean modified = reg_delta[Ra.getNumber()] || reg_delta[Rb.getNumber()];
        printPair(r.getName().toUpperCase(), Arithmetic.uword(low, high), modified);
    }

    protected final void printPair(String n, String str, boolean modified) {
        ColorTerminal.printBrightGreen(n);
        ColorTerminal.print(": ");
        if (modified)
            ColorTerminal.printRed(str + " ");
        else
            ColorTerminal.print(str + " ");
    }

    protected final void printPair(String n, byte val, boolean modified) {
        String str = VPCBase.toPaddedUpperHex(val, 2);
        printPair(n, str, modified);
    }

    protected final void printPair(String n, int val, boolean modified) {
        String str = VPCBase.toPaddedUpperHex(val, 4);
        printPair(n, str, modified);
    }

    protected final void printPair(String n, long val, boolean modified) {
        String str = VPCBase.toPaddedUpperHex(val, 10);
        printPair(n, str, modified);
    }

    public void clearTracingState() {
    }
}
