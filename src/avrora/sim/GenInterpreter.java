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

    public static final Register R0 = Register.R0;
    public static final Register R1 = Register.R1;
    public static final Register R2 = Register.R2;
    public static final Register R3 = Register.R3;
    public static final Register R4 = Register.R4;
    public static final Register R5 = Register.R5;
    public static final Register R6 = Register.R6;
    public static final Register R7 = Register.R7;
    public static final Register R8 = Register.R8;
    public static final Register R9 = Register.R9;
    public static final Register R10 = Register.R10;
    public static final Register R11 = Register.R11;
    public static final Register R12 = Register.R12;
    public static final Register R13 = Register.R13;
    public static final Register R14 = Register.R14;
    public static final Register R15 = Register.R15;
    public static final Register R16 = Register.R16;
    public static final Register R17 = Register.R17;
    public static final Register R18 = Register.R18;
    public static final Register R19 = Register.R19;
    public static final Register R20 = Register.R20;
    public static final Register R21 = Register.R21;
    public static final Register R22 = Register.R22;
    public static final Register R23 = Register.R23;
    public static final Register R24 = Register.R24;
    public static final Register R25 = Register.R25;
    public static final Register R26 = Register.R26;
    public static final Register R27 = Register.R27;
    public static final Register R28 = Register.R28;
    public static final Register R29 = Register.R29;
    public static final Register R30 = Register.R30;
    public static final Register R31 = Register.R31;

    public static final Register RX = Register.X;
    public static final Register RY = Register.Y;
    public static final Register RZ = Register.Z;

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
    public void visit(Instr.ADC i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        int tmp_1 = getRegisterUnsigned(i.r2);
        int tmp_2 = bit(C);
        int tmp_3 = tmp_0 + tmp_1 + tmp_2;
        int tmp_4 = (tmp_0 & 0x0000000F);
        int tmp_5 = (tmp_1 & 0x0000000F);
        boolean tmp_6 = ((tmp_0 & 128) != 0);
        boolean tmp_7 = ((tmp_1 & 128) != 0);
        boolean tmp_8 = ((tmp_3 & 128) != 0);
        H = ((tmp_4 + tmp_5 + tmp_2 & 16) != 0);
        C = ((tmp_3 & 256) != 0);
        N = ((tmp_3 & 128) != 0);
        Z = low(tmp_3) == 0;
        V = tmp_6 && tmp_7 && !tmp_8 || !tmp_6 && !tmp_7 && tmp_8;
        S = xor(N, V);
        byte tmp_9 = low(tmp_3);
        setRegisterByte(i.r1, tmp_9);
        cyclesConsumed += 1;
    }
    public void visit(Instr.ADD i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        int tmp_1 = getRegisterUnsigned(i.r2);
        int tmp_2 = 0;
        int tmp_3 = tmp_0 + tmp_1 + tmp_2;
        int tmp_4 = (tmp_0 & 0x0000000F);
        int tmp_5 = (tmp_1 & 0x0000000F);
        boolean tmp_6 = ((tmp_0 & 128) != 0);
        boolean tmp_7 = ((tmp_1 & 128) != 0);
        boolean tmp_8 = ((tmp_3 & 128) != 0);
        H = ((tmp_4 + tmp_5 + tmp_2 & 16) != 0);
        C = ((tmp_3 & 256) != 0);
        N = ((tmp_3 & 128) != 0);
        Z = low(tmp_3) == 0;
        V = tmp_6 && tmp_7 && !tmp_8 || !tmp_6 && !tmp_7 && tmp_8;
        S = xor(N, V);
        byte tmp_9 = low(tmp_3);
        setRegisterByte(i.r1, tmp_9);
        cyclesConsumed += 1;
    }
    public void visit(Instr.ADIW i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r1);
        int tmp_1 = tmp_0 + i.imm1;
        boolean tmp_2 = ((tmp_1 & 32768) != 0);
        boolean tmp_3 = ((tmp_0 & 128) != 0);
        C = !tmp_2 && tmp_3;
        N = tmp_2;
        V = !tmp_3 && tmp_2;
        Z = (tmp_1 & 0x0000FFFF) == 0;
        S = xor(N, V);
        setRegisterWord(i.r1, tmp_1);
        cyclesConsumed += 2;
    }
    public void visit(Instr.AND i)  {
        nextPC = pc + 2;
        int tmp_1 = getRegisterByte(i.r1);
        int tmp_2 = getRegisterByte(i.r2);
        int tmp_3 = tmp_1 & tmp_2;
        N = ((tmp_3 & 128) != 0);
        Z = low(tmp_3) == 0;
        V = false;
        S = xor(N, V);
        byte tmp_4 = low(tmp_3);
        int tmp_0 = tmp_4;
        cyclesConsumed += 1;
    }
    public void visit(Instr.ANDI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = tmp_0 & tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.ASR i)  {
        nextPC = pc + 2;
        byte tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = tmp_0;
        boolean tmp_2 = ((tmp_0 & 128) != 0);
        int tmp_3 = (tmp_1 & 255) >> 1;
        tmp_3 = Arithmetic.setBit(tmp_3, 7, tmp_2);
        C = ((tmp_1 & 1) != 0);
        N = tmp_2;
        Z = low(tmp_3) == 0;
        V = xor(N, C);
        S = xor(N, V);
        byte tmp_4 = low(tmp_3);
        setRegisterByte(i.r1, tmp_4);
        cyclesConsumed += 1;
    }
    public void visit(Instr.BCLR i)  {
        nextPC = pc + 2;
        getIOReg(SREG).writeBit(i.imm1, false);
        cyclesConsumed += 1;
    }
    public void visit(Instr.BLD i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, Arithmetic.setBit(getRegisterByte(i.r1), i.imm1, T));
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRBC i)  {
        nextPC = pc + 2;
        if ( !getIOReg(SREG).readBit(i.imm1) ) {
            int tmp_0 = i.imm2;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRBS i)  {
        nextPC = pc + 2;
        if ( getIOReg(SREG).readBit(i.imm1) ) {
            int tmp_0 = i.imm2;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRCC i)  {
        nextPC = pc + 2;
        if ( !C ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRCS i)  {
        nextPC = pc + 2;
        if ( C ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BREAK i)  {
        nextPC = pc + 2;
        stop();
        cyclesConsumed += 1;
    }
    public void visit(Instr.BREQ i)  {
        nextPC = pc + 2;
        if ( Z ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRGE i)  {
        nextPC = pc + 2;
        if ( !S ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRHC i)  {
        nextPC = pc + 2;
        if ( !H ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRHS i)  {
        nextPC = pc + 2;
        if ( H ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRID i)  {
        nextPC = pc + 2;
        if ( !I ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRIE i)  {
        nextPC = pc + 2;
        if ( I ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRLO i)  {
        nextPC = pc + 2;
        if ( C ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRLT i)  {
        nextPC = pc + 2;
        if ( S ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRMI i)  {
        nextPC = pc + 2;
        if ( N ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRNE i)  {
        nextPC = pc + 2;
        if ( !Z ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRPL i)  {
        nextPC = pc + 2;
        if ( !N ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRSH i)  {
        nextPC = pc + 2;
        if ( !C ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRTC i)  {
        nextPC = pc + 2;
        if ( !T ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRTS i)  {
        nextPC = pc + 2;
        if ( T ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRVC i)  {
        nextPC = pc + 2;
        if ( !V ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRVS i)  {
        nextPC = pc + 2;
        if ( V ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BSET i)  {
        nextPC = pc + 2;
        getIOReg(SREG).writeBit(i.imm1, true);
        cyclesConsumed += 1;
    }
    public void visit(Instr.BST i)  {
        nextPC = pc + 2;
        T = Arithmetic.getBit(getRegisterByte(i.r1), i.imm1);
        cyclesConsumed += 1;
    }
    public void visit(Instr.CALL i)  {
        nextPC = pc + 4;
        int tmp_0 = nextPC;
        tmp_0 = tmp_0 / 2;
        pushByte(low(tmp_0));
        pushByte(high(tmp_0));
        int tmp_1 = i.imm1;
        int tmp_2 = tmp_1 * 2;
        nextPC = tmp_2;
        cyclesConsumed += 4;
    }
    public void visit(Instr.CBI i)  {
        nextPC = pc + 1;
        getIOReg(i.imm1).writeBit(i.imm2, false);
        cyclesConsumed += 2;
    }
    public void visit(Instr.CBR i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = ~i.imm1;
        int tmp_2 = tmp_0 & tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLC i)  {
        nextPC = pc + 2;
        C = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLH i)  {
        nextPC = pc + 2;
        H = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLI i)  {
        nextPC = pc + 2;
        I = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLN i)  {
        nextPC = pc + 2;
        N = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLR i)  {
        nextPC = pc + 2;
        S = false;
        V = false;
        N = false;
        Z = true;
        setRegisterByte(i.r1, low(0));
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLS i)  {
        nextPC = pc + 2;
        S = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLT i)  {
        nextPC = pc + 2;
        T = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLV i)  {
        nextPC = pc + 2;
        V = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLZ i)  {
        nextPC = pc + 2;
        Z = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.COM i)  {
        nextPC = pc + 2;
        int tmp_0 = 255 - getRegisterByte(i.r1);
        C = true;
        N = ((tmp_0 & 128) != 0);
        Z = low(tmp_0) == 0;
        V = false;
        S = xor(N, V);
        setRegisterByte(i.r1, low(tmp_0));
        cyclesConsumed += 1;
    }
    public void visit(Instr.CP i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = 0;
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.CPC i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = bit(C);
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.CPI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = 0;
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.CPSE i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = tmp_0;
        int tmp_3 = tmp_1;
        int tmp_4 = 0;
        int tmp_5 = tmp_2 - tmp_3 - tmp_4;
        boolean tmp_6 = ((tmp_2 & 128) != 0);
        boolean tmp_7 = ((tmp_3 & 128) != 0);
        boolean tmp_8 = ((tmp_5 & 128) != 0);
        boolean tmp_9 = ((tmp_2 & 8) != 0);
        boolean tmp_10 = ((tmp_3 & 8) != 0);
        boolean tmp_11 = ((tmp_5 & 8) != 0);
        H = !tmp_9 && tmp_10 || tmp_10 && tmp_11 || tmp_11 && !tmp_9;
        C = !tmp_6 && tmp_7 || tmp_7 && tmp_8 || tmp_8 && !tmp_6;
        N = tmp_8;
        Z = low(tmp_5) == 0;
        V = tmp_6 && !tmp_7 && !tmp_8 || !tmp_6 && tmp_7 && tmp_8;
        S = xor(N, V);
        byte tmp_12 = low(tmp_5);
        if ( tmp_0 == tmp_1 ) {
            int tmp_13 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_13;
            if ( tmp_13 == 4 ) {
                cyclesConsumed = cyclesConsumed + 2;
            }
            else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.DEC i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        byte tmp_1 = low(tmp_0 - 1);
        N = ((tmp_1 & 128) != 0);
        Z = tmp_1 == 0;
        V = tmp_0 == 128;
        S = xor(N, V);
        setRegisterByte(i.r1, tmp_1);
        cyclesConsumed += 1;
    }
    public void visit(Instr.EICALL i)  {
        nextPC = pc + 2;
        cyclesConsumed += 4;
    }
    public void visit(Instr.EIJMP i)  {
        nextPC = pc + 2;
        cyclesConsumed += 2;
    }
    public void visit(Instr.ELPM i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(RZ);
        tmp_0 = (tmp_0 & 0xFF00FFFF) | ((getIORegisterByte(RAMPZ) & 0x000000FF) << 16);
        setRegisterByte(R0, getProgramByte(tmp_0));
        cyclesConsumed += 3;
    }
    public void visit(Instr.ELPMD i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(RZ);
        tmp_0 = (tmp_0 & 0xFF00FFFF) | ((getIORegisterByte(RAMPZ) & 0x000000FF) << 16);
        setRegisterByte(i.r1, getProgramByte(tmp_0));
        cyclesConsumed += 3;
    }
    public void visit(Instr.ELPMPI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(RZ);
        tmp_0 = (tmp_0 & 0xFF00FFFF) | ((getIORegisterByte(RAMPZ) & 0x000000FF) << 16);
        setRegisterByte(i.r1, getProgramByte(tmp_0));
        setRegisterWord(RZ, tmp_0 + 1);
        cyclesConsumed += 3;
    }
    public void visit(Instr.EOR i)  {
        nextPC = pc + 2;
        byte tmp_0 = low(getRegisterByte(i.r1) ^ getRegisterByte(i.r2));
        N = ((tmp_0 & 128) != 0);
        Z = tmp_0 == 0;
        V = false;
        S = xor(N, V);
        setRegisterByte(i.r1, tmp_0);
        cyclesConsumed += 1;
    }
    public void visit(Instr.FMUL i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1) * getRegisterUnsigned(i.r2) << 1;
        Z = (tmp_0 & 0x0000FFFF) == 0;
        C = ((tmp_0 & 65536) != 0);
        setRegisterByte(R0, low(tmp_0));
        setRegisterByte(R1, high(tmp_0));
        cyclesConsumed += 2;
    }
    public void visit(Instr.FMULS i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1) * getRegisterByte(i.r2) << 1;
        Z = (tmp_0 & 0x0000FFFF) == 0;
        C = ((tmp_0 & 65536) != 0);
        setRegisterByte(R0, low(tmp_0));
        setRegisterByte(R1, high(tmp_0));
        cyclesConsumed += 2;
    }
    public void visit(Instr.FMULSU i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1) * getRegisterUnsigned(i.r2) << 1;
        Z = (tmp_0 & 0x0000FFFF) == 0;
        C = ((tmp_0 & 65536) != 0);
        setRegisterByte(R0, low(tmp_0));
        setRegisterByte(R1, high(tmp_0));
        cyclesConsumed += 2;
    }
    public void visit(Instr.ICALL i)  {
        nextPC = pc + 2;
        int tmp_0 = nextPC;
        tmp_0 = tmp_0 / 2;
        pushByte(low(tmp_0));
        pushByte(high(tmp_0));
        int tmp_1 = getRegisterWord(R0);
        int tmp_2 = tmp_1 * 2;
        nextPC = tmp_2;
        cyclesConsumed += 3;
    }
    public void visit(Instr.IJMP i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(R0);
        int tmp_1 = tmp_0 * 2;
        nextPC = tmp_1;
        cyclesConsumed += 2;
    }
    public void visit(Instr.IN i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, getIORegisterByte(i.imm1));
        cyclesConsumed += 1;
    }
    public void visit(Instr.INC i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        byte tmp_1 = low(tmp_0 + 1);
        N = ((tmp_1 & 128) != 0);
        Z = tmp_1 == 0;
        V = tmp_0 == 127;
        S = xor(N, V);
        setRegisterByte(i.r1, tmp_1);
        cyclesConsumed += 1;
    }
    public void visit(Instr.JMP i)  {
        nextPC = pc + 4;
        int tmp_0 = i.imm1;
        int tmp_1 = tmp_0 * 2;
        nextPC = tmp_1;
        cyclesConsumed += 3;
    }
    public void visit(Instr.LD i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, getDataByte(getRegisterWord(i.r2)));
        cyclesConsumed += 2;
    }
    public void visit(Instr.LDD i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, getDataByte(getRegisterWord(i.r2) + i.imm1));
        cyclesConsumed += 2;
    }
    public void visit(Instr.LDI i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, low(i.imm1));
        cyclesConsumed += -1;
    }
    public void visit(Instr.LDPD i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r2) - 1;
        setRegisterByte(i.r1, getDataByte(tmp_0));
        setRegisterWord(i.r2, tmp_0);
        cyclesConsumed += 2;
    }
    public void visit(Instr.LDPI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r2);
        setRegisterByte(i.r1, getDataByte(tmp_0));
        setRegisterWord(i.r2, tmp_0 + 1);
        cyclesConsumed += 2;
    }
    public void visit(Instr.LDS i)  {
        nextPC = pc + 4;
        setRegisterByte(i.r1, getDataByte(i.imm1));
        cyclesConsumed += 2;
    }
    public void visit(Instr.LPM i)  {
        nextPC = pc + 2;
        setRegisterByte(R0, getProgramByte(getRegisterWord(RZ)));
        cyclesConsumed += 3;
    }
    public void visit(Instr.LPMD i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, getProgramByte(getRegisterWord(RZ)));
        cyclesConsumed += 3;
    }
    public void visit(Instr.LPMPI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(RZ);
        setRegisterByte(i.r1, getProgramByte(tmp_0));
        setRegisterWord(RZ, tmp_0 + 1);
        cyclesConsumed += 3;
    }
    public void visit(Instr.LSL i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        boolean tmp_1 = false;
        int tmp_2 = tmp_0 << 1;
        tmp_2 = Arithmetic.setBit(tmp_2, 0, tmp_1);
        H = ((tmp_2 & 16) != 0);
        C = ((tmp_2 & 256) != 0);
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = xor(N, C);
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.LSR i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        boolean tmp_1 = false;
        int tmp_2 = (tmp_0 & 255) >> 1;
        tmp_2 = Arithmetic.setBit(tmp_2, 7, tmp_1);
        C = ((tmp_0 & 1) != 0);
        N = tmp_1;
        Z = low(tmp_2) == 0;
        V = xor(N, C);
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.MOV i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, getRegisterByte(i.r2));
        cyclesConsumed += 1;
    }
    public void visit(Instr.MOVW i)  {
        nextPC = pc + 2;
        setRegisterWord(i.r1, getRegisterWord(i.r2));
        cyclesConsumed += 1;
    }
    public void visit(Instr.MUL i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1) * getRegisterUnsigned(i.r2);
        C = ((tmp_0 & 32768) != 0);
        Z = (tmp_0 & 0x0000FFFF) == 0;
        setRegisterWord(R0, tmp_0);
        cyclesConsumed += 2;
    }
    public void visit(Instr.MULS i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1) * getRegisterByte(i.r2);
        C = ((tmp_0 & 32768) != 0);
        Z = (tmp_0 & 0x0000FFFF) == 0;
        setRegisterWord(R0, tmp_0);
        cyclesConsumed += 2;
    }
    public void visit(Instr.MULSU i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1) * getRegisterUnsigned(i.r2);
        C = ((tmp_0 & 32768) != 0);
        Z = (tmp_0 & 0x0000FFFF) == 0;
        setRegisterWord(R0, tmp_0);
        cyclesConsumed += 2;
    }
    public void visit(Instr.NEG i)  {
        nextPC = pc + 2;
        int tmp_0 = 0;
        int tmp_1 = getRegisterByte(i.r1);
        int tmp_2 = 0;
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        setRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }
    public void visit(Instr.NOP i)  {
        nextPC = pc + 2;
        cyclesConsumed += 1;
    }
    public void visit(Instr.OR i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = tmp_0 | tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.ORI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = tmp_0 | tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.OUT i)  {
        nextPC = pc + 2;
        setIORegisterByte(i.imm1, getRegisterByte(i.r1));
        cyclesConsumed += 1;
    }
    public void visit(Instr.POP i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, popByte());
        cyclesConsumed += 2;
    }
    public void visit(Instr.PUSH i)  {
        nextPC = pc + 2;
        pushByte(getRegisterByte(i.r1));
        cyclesConsumed += 2;
    }
    public void visit(Instr.RCALL i)  {
        nextPC = pc + 2;
        int tmp_0 = nextPC;
        tmp_0 = tmp_0 / 2;
        pushByte(low(tmp_0));
        pushByte(high(tmp_0));
        int tmp_1 = i.imm1;
        int tmp_2 = tmp_1 * 2 + nextPC;
        nextPC = tmp_2;
        cyclesConsumed += 3;
    }
    public void visit(Instr.RET i)  {
        nextPC = pc + 2;
        byte tmp_0 = popByte();
        byte tmp_1 = popByte();
        int tmp_2 = uword(tmp_1, tmp_0) * 2;
        nextPC = tmp_2;
        cyclesConsumed += 4;
    }
    public void visit(Instr.RETI i)  {
        nextPC = pc + 2;
        byte tmp_0 = popByte();
        byte tmp_1 = popByte();
        int tmp_2 = uword(tmp_1, tmp_0) * 2;
        nextPC = tmp_2;
        I = true;
        justReturnedFromInterrupt = true;
        cyclesConsumed += 4;
    }
    public void visit(Instr.RJMP i)  {
        nextPC = pc + 2;
        int tmp_0 = i.imm1;
        int tmp_1 = tmp_0 * 2 + nextPC;
        nextPC = tmp_1;
        cyclesConsumed += 2;
    }
    public void visit(Instr.ROL i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        boolean tmp_1 = C;
        int tmp_2 = tmp_0 << 1;
        tmp_2 = Arithmetic.setBit(tmp_2, 0, tmp_1);
        H = ((tmp_2 & 16) != 0);
        C = ((tmp_2 & 256) != 0);
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = xor(N, C);
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.ROR i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        boolean tmp_1 = C;
        int tmp_2 = (tmp_0 & 255) >> 1;
        tmp_2 = Arithmetic.setBit(tmp_2, 7, tmp_1);
        C = ((tmp_0 & 1) != 0);
        N = tmp_1;
        Z = low(tmp_2) == 0;
        V = xor(N, C);
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.SBC i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = bit(C);
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0 && Z;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        setRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }
    public void visit(Instr.SBCI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = bit(C);
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0 && Z;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        setRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }
    public void visit(Instr.SBI i)  {
        nextPC = pc + 2;
        getIOReg(i.imm1).writeBit(i.imm2, true);
        cyclesConsumed += 2;
    }
    public void visit(Instr.SBIC i)  {
        nextPC = pc + 2;
        if ( !getIOReg(i.imm1).readBit(i.imm2) ) {
            int tmp_0 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_0;
            if ( tmp_0 == 4 ) {
                cyclesConsumed = cyclesConsumed + 2;
            }
            else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.SBIS i)  {
        nextPC = pc + 2;
        if ( getIOReg(i.imm1).readBit(i.imm2) ) {
            int tmp_0 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_0;
            if ( tmp_0 == 4 ) {
                cyclesConsumed = cyclesConsumed + 2;
            }
            else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.SBIW i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r1);
        int tmp_1 = tmp_0 - i.imm1;
        boolean tmp_2 = ((tmp_0 & 32768) != 0);
        boolean tmp_3 = ((tmp_1 & 32768) != 0);
        V = tmp_2 && !tmp_3;
        N = tmp_3;
        Z = (tmp_1 & 0x0000FFFF) == 0;
        C = tmp_3 && !tmp_2;
        S = xor(N, V);
        setRegisterWord(i.r1, tmp_1);
        cyclesConsumed += 2;
    }
    public void visit(Instr.SBR i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = tmp_0 | tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.SBRC i)  {
        nextPC = pc + 2;
        if ( !Arithmetic.getBit(getRegisterByte(i.r1), i.imm1) ) {
            int tmp_0 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_0;
            if ( tmp_0 == 4 ) {
                cyclesConsumed = cyclesConsumed + 2;
            }
            else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.SBRS i)  {
        nextPC = pc + 2;
        if ( Arithmetic.getBit(getRegisterByte(i.r1), i.imm1) ) {
            int tmp_0 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_0;
            if ( tmp_0 == 4 ) {
                cyclesConsumed = cyclesConsumed + 2;
            }
            else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.SEC i)  {
        nextPC = pc + 2;
        C = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SEH i)  {
        nextPC = pc + 2;
        H = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SEI i)  {
        nextPC = pc + 2;
        I = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SEN i)  {
        nextPC = pc + 2;
        N = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SER i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, low(255));
        cyclesConsumed += 1;
    }
    public void visit(Instr.SES i)  {
        nextPC = pc + 2;
        S = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SET i)  {
        nextPC = pc + 2;
        T = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SEV i)  {
        nextPC = pc + 2;
        V = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SEZ i)  {
        nextPC = pc + 2;
        Z = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SLEEP i)  {
        nextPC = pc + 2;
        sleeping = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SPM i)  {
        nextPC = pc + 2;
        cyclesConsumed += 1;
    }
    public void visit(Instr.ST i)  {
        nextPC = pc + 2;
        setDataByte(getRegisterWord(i.r1), getRegisterByte(i.r2));
        cyclesConsumed += 2;
    }
    public void visit(Instr.STD i)  {
        nextPC = pc + 2;
        setDataByte(getRegisterWord(i.r1) + i.imm1, getRegisterByte(i.r2));
        cyclesConsumed += 2;
    }
    public void visit(Instr.STPD i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r1) - 1;
        setDataByte(tmp_0, getRegisterByte(i.r2));
        setRegisterWord(i.r1, tmp_0);
        cyclesConsumed += 2;
    }
    public void visit(Instr.STPI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r1);
        setDataByte(tmp_0, getRegisterByte(i.r2));
        setRegisterWord(i.r1, tmp_0 + 1);
        cyclesConsumed += 2;
    }
    public void visit(Instr.STS i)  {
        nextPC = pc + 4;
        setDataByte(i.imm1, getRegisterByte(i.r1));
        cyclesConsumed += 2;
    }
    public void visit(Instr.SUB i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = 0;
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        setRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }
    public void visit(Instr.SUBI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = 0;
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        setRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }
    public void visit(Instr.SWAP i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        int tmp_1 = 0;
        tmp_1 = (tmp_1 & 0xFFFFFFF0) | ((((tmp_0 >> 4) & 0x0000000F) & 0x0000000F));
        tmp_1 = (tmp_1 & 0xFFFFFF0F) | (((tmp_0 & 0x0000000F) & 0x0000000F) << 4);
        setRegisterByte(i.r1, low(tmp_1));
        cyclesConsumed += 1;
    }
    public void visit(Instr.TST i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        V = false;
        Z = low(tmp_0) == 0;
        N = ((tmp_0 & 128) != 0);
        S = xor(N, V);
        cyclesConsumed += 1;
    }
    public void visit(Instr.WDR i)  {
        nextPC = pc + 2;
        cyclesConsumed += 1;
    }
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

    private byte bit(boolean val) {
        if ( val ) return 1;
        return 0;
    }

    private int uword(byte low, byte high) {
        return Arithmetic.uword(low, high);
    }

    private void stop() {
        shouldRun = false;
    }

    private void enterSleepMode() {
        sleeping = true;
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
     * @param address the byte address at which to write the value
     * @param val     the value to write
     */
    public void setDataByte(int address, byte val) {
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
     * @param ioreg the IO register number to which to write the value
     * @param val   the value to write to the IO register
     */
    public void setIORegisterByte(int ioreg, byte val) {
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
        setDataByte(address, val);
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
