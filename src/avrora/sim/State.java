package avrora.sim;

import avrora.sir.*;
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

    /**
     * Constants needed internally.
     */
    public static final int FLAG_I = 7;
    public static final int FLAG_T = 6;
    public static final int FLAG_H = 5;
    public static final int FLAG_S = 4;
    public static final int FLAG_V = 3;
    public static final int FLAG_N = 2;
    public static final int FLAG_Z = 1;
    public static final int FLAG_C = 0;

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
        public byte read();
        public void write(byte val);
        public boolean readBit(int num);
        public void clearBit(int num);
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

        public byte read() {
            return value;
        }

        public void write(byte val) {
            value = val;
        }

        public boolean readBit(int bit) {
            return Arithmetic.getBit(value, bit);
        }

        public void clearBit(int bit) {
            value = Arithmetic.clearBit(value, bit);
        }

        public void setBit(int bit) {
            value = Arithmetic.setBit(value, bit);
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

    private void setSREG_bit(int bit, boolean val) {
        if ( val ) ioregs[SREG].setBit(bit);
        else ioregs[SREG].clearBit(bit);
    }

    public void setFlag_I(boolean val) { setSREG_bit(FLAG_I, val); }
    public void setFlag_T(boolean val) { setSREG_bit(FLAG_T, val); }
    public void setFlag_H(boolean val) { setSREG_bit(FLAG_H, val); }
    public void setFlag_S(boolean val) { setSREG_bit(FLAG_S, val); }
    public void setFlag_V(boolean val) { setSREG_bit(FLAG_V, val); }
    public void setFlag_N(boolean val) { setSREG_bit(FLAG_N, val); }
    public void setFlag_Z(boolean val) { setSREG_bit(FLAG_Z, val); }
    public void setFlag_C(boolean val) { setSREG_bit(FLAG_C, val); }

    public boolean getFlag_I() { return ioregs[SREG].readBit(FLAG_I); }
    public boolean getFlag_T() { return ioregs[SREG].readBit(FLAG_T); }
    public boolean getFlag_H() { return ioregs[SREG].readBit(FLAG_H); }
    public boolean getFlag_S() { return ioregs[SREG].readBit(FLAG_S); }
    public boolean getFlag_V() { return ioregs[SREG].readBit(FLAG_V); }
    public boolean getFlag_N() { return ioregs[SREG].readBit(FLAG_N); }
    public boolean getFlag_Z() { return ioregs[SREG].readBit(FLAG_Z); }
    public boolean getFlag_C() { return ioregs[SREG].readBit(FLAG_C); }

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
        printPair("I", getBitAsString(sreg, FLAG_I), flag_delta[FLAG_I]);
        printPair("T", getBitAsString(sreg, FLAG_T), flag_delta[FLAG_T]);
        printPair("H", getBitAsString(sreg, FLAG_H), flag_delta[FLAG_H]);
        printPair("S", getBitAsString(sreg, FLAG_S), flag_delta[FLAG_S]);
        printPair("V", getBitAsString(sreg, FLAG_V), flag_delta[FLAG_V]);
        printPair("N", getBitAsString(sreg, FLAG_N), flag_delta[FLAG_N]);
        printPair("Z", getBitAsString(sreg, FLAG_Z), flag_delta[FLAG_Z]);
        printPair("C", getBitAsString(sreg, FLAG_C), flag_delta[FLAG_C]);

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
