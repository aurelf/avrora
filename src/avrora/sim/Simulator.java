package avrora.sim;

import avrora.sir.InstrVisitor;
import avrora.sir.Instr;
import avrora.sir.Program;
import avrora.sir.Register;
import avrora.Arithmetic;
import avrora.Operand;
import vpc.util.ColorTerminal;
import vpc.VPCBase;

/**
 * The <code>Simulator</code> class implements a full processor simulator
 * for the AVR instruction set. It is the base class of specific implementations
 * that implement processor-specific behavior.
 *
 * @author Ben L. Titzer
 */
public abstract class Simulator extends VPCBase implements InstrVisitor, IORegisterConstants {

    public static boolean TRACE = false;
    public static boolean TRACEREGS = false;

    protected final Program program;
    protected State state;

    protected Probe activeProbe;

    protected boolean shouldRun;
    protected boolean justReturnedFromInterrupt;
    protected int nextPC;
    protected Interrupt[] interrupts;

    public static int MAX_INTERRUPTS = 35;

    public Simulator(Program p) {
        program = p;
        interrupts = new Interrupt[MAX_INTERRUPTS];

        for ( int cntr = 0; cntr < MAX_INTERRUPTS; cntr++ )
            interrupts[cntr] = IGNORE;

        reset();
    }

    public interface Probe {

        public void fireBefore(Instr i, int address, State state);
        public void fireAfter(Instr i, int address, State state);
    }

    public interface MemoryProbe {

        public void fireBeforeRead(Instr i, int address, State state);
        public void fireBeforeWrite(Instr i, int address, State state, byte value);
        public void fireAfterRead(Instr i, int address, State state, byte value);
        public void fireAfterWrite(Instr i, int address, State state, byte value);
    }

    class BreakPointException extends RuntimeException {
        public final BreakPoint breakpoint;

        BreakPointException(BreakPoint bp) {
            super("breakpoint at " + bp.address + " reached");
            breakpoint = bp;
        }
    }

    class BreakPoint extends Instr {

        private final int address;
        private final Instr instr;
        private final Probe probe;

        BreakPoint(Instr i, int a, Probe p) {
            instr = i;
            address = a;
            probe = p;
        }

        public Instr build(int address, Operand[] ops) {
            return instr.build(address, ops);
        }

        public void accept(InstrVisitor v) {
            throw new BreakPointException(this);
        }

        public String getName() {
            return "breakpoint: " + instr.getName();
        }

        public String getVariant() {
            return "breakpoint: " + instr.getVariant();
        }

        public int getSize() {
            return instr.getSize();
        }

        public String getOperands() {
            return instr.getOperands();
        }

        public Instr asInstr(int address) {
            return instr;
        }
    }

    /**
     * The <code>Interrupt</code> interface represents the behavior of an interrupt
     * (i.e. how it manipulates the state of the processor) when it is posted and
     * when it is triggered (handler is executed by the processor). For example,
     * an external interrupt, when posted, sets a bit in an IO register, and if
     * the interrupt is not masked, will add it to the pending interrupts on
     * the processor. When the interrupt triggers, it remains flagged (the bit
     * in the IO register remains on). Some interrupts clear bits in IO registers
     * on triggered (e.g. timer interrupts). This interface allows both of these
     * behaviors to be implemented.
     */
    public interface Interrupt {
        public void post();
        public void fire();
    }

    public class MaskableInterrupt implements Interrupt {
        final int interruptNumber;

        final int maskRegister;
        final int flagRegister;
        final int bit;

        final boolean sticky;

        public MaskableInterrupt(int num, int mr, int fr, int b, boolean e) {
            interruptNumber = num;
            maskRegister = mr;
            flagRegister = fr;
            bit = b;
            sticky = e;
        }

        public void post() {
            int flag = 1 << bit;
            int nfr = state.readIORegister(flagRegister) | flag;
            state.writeIORegister((byte)nfr, flagRegister);
            int mask = state.readIORegister(maskRegister);
            if ( (mask & flag) != 0 )
                state.postInterrupt(interruptNumber);
        }

        public void fire() {
            if ( !sticky ) {
                int nfr = state.readIORegister(flagRegister) & ~(1 << bit);
                state.writeIORegister((byte)nfr, flagRegister);
                state.unpostInterrupt(interruptNumber);
            }
        }
    }

    private static final Interrupt IGNORE = new Interrupt() {
        public void post() { }
        public void fire() { }
    };



    public State getState() {
        return state;
    }

    public void start() {

        if (TRACE) {
            ColorTerminal.println("Simulator running...");
        }

        shouldRun = true;
        loop();
    }

    public void stop() {
        shouldRun = false;
    }

    public void reset() {
        if (TRACEREGS) {
            state = constructTracingState();
            TRACE = true;
        } else {
            state = constructState();
        }
    }

    protected abstract State constructState();
    protected abstract State constructTracingState();

    private void loop() {

        nextPC = state.getPC();

        while (shouldRun) {

            if ( justReturnedFromInterrupt ) {
                // don't process the interrupt if we just returned from
                // an interrupt handler, because the hardware manual says
                // that at least one instruction is executed after
                // returning from an interrupt.
                justReturnedFromInterrupt = false;
            } else if ( state.getFlag_I() ) {
                long interruptPostMask = state.getPostedInterrupts();

                // check if there are any pending (posted) interrupts
                if ( interruptPostMask != 0 ) {
                    // the lowest set bit is the highest priority posted interrupt
                    int lowbit = Arithmetic.lowestBit(interruptPostMask);

                    // fire the interrupt (update flag register(s) state)
                    interrupts[lowbit].fire();

                    // set PC to interrupt handler
                    nextPC = lowbit * 4;
                    state.writePC(nextPC);

                    // hardware manual says handling of interrupt requires 4 cycles
                    state.consumeCycles(4);

                    // disable interrupts
                    state.setFlag_I(false);
                }
            }

            // get the current instruction
            Instr i = state.getCurrentInstr();
            nextPC = nextPC + i.getSize();

            if (TRACE) {
                ColorTerminal.printBrightCyan(toPaddedUpperHex(state.getPC(), 4) + ": ");
                ColorTerminal.printBrightBlue(i.getVariant() + " ");
                ColorTerminal.print(i.getOperands());
                ColorTerminal.nextln();
            }

            try {
                // visit the actual instruction (or probe)
                if (activeProbe != null) {
                    int pc = state.getPC();
                    activeProbe.fireBefore(i, pc, state);
                    execute(i);
                    activeProbe.fireAfter(i, pc, state);
                } else {
                    execute(i);
                }

            } catch (BreakPointException bpe) {
                int pc = state.getPC();

                bpe.breakpoint.probe.fireBefore(bpe.breakpoint.instr, pc, state);

                if (!shouldRun) break;
                execute(bpe.breakpoint.instr);

                bpe.breakpoint.probe.fireAfter(bpe.breakpoint.instr, pc, state);

                if (activeProbe != null)
                    activeProbe.fireAfter(i, pc, state);
            }

            if (TRACEREGS) {
                state.dump();
                state.clearTracingState();
            }
        }
    }

    private void execute(Instr i) {
        i.accept(this);
        state.writePC(nextPC);
        state.consumeCycles(i.getCycles());

    }

    public void insertProbe(Probe p) {
        activeProbe = p;
    }

    public void setBreakPoint(Probe p, int prog_addr) {
        Instr i = state.readInstr(prog_addr);
        if (i instanceof BreakPoint) return;

        BreakPoint bp = new BreakPoint(i, prog_addr, p);
        state.writeInstr(bp, prog_addr);
    }

    public void setWatchPoint(MemoryProbe p, int data_addr) {
        // TODO: implement watchpoints
    }

    public void postInterrupt(int num) {
        interrupts[num].post();
    }

    protected void triggerInterrupt(int num) {
        interrupts[num].fire();
    }


    /**
     *  V I S I T   M E T H O D S
     * ------------------------------------------------------------------
     *
     * These methods implement the InstrVisitor interface and
     * accomplish the behavior of each instruction.
     *
     *
     */

    public void visit(Instr.ADC i) { // add two registers and carry flag
        int r1 = state.readRegisterUnsigned(i.r1);
        int r2 = state.readRegisterUnsigned(i.r2);
        int result = performAddition(r1, r2, state.getFlag_C() ? 1 : 0);
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.ADD i) { // add second register to first
        int r1 = state.readRegisterUnsigned(i.r1);
        int r2 = state.readRegisterUnsigned(i.r2);
        int result = performAddition(r1, r2, 0);
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.ADIW i) { // add immediate to word register
        int r1 = readRegisterWord(i.r1);
        int r2 = i.imm1;
        int result = r1 + r2;
        boolean R15 = Arithmetic.getBit(result, 15);
        boolean Rdh7 = Arithmetic.getBit(r1, 15);

        state.setFlag_C(!R15 && Rdh7);
        state.setFlag_N(R15);
        state.setFlag_V(!Rdh7 && R15);
        state.setFlag_Z((result & 0xffff) == 0);
        state.setFlag_S(xor(state.getFlag_N(), state.getFlag_Z()));

        writeRegisterWord(i.r1, result);
    }

    public void visit(Instr.AND i) { // and first register with second
        int r1 = state.readRegister(i.r1);
        int r2 = state.readRegister(i.r2);
        int result = performAnd(r1, r2);
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.ANDI i) { // and register with immediate
        int r1 = state.readRegister(i.r1);
        int r2 = i.imm1;
        int result = performAnd(r1, r2);
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.ASR i) { // arithmetic shift right by one bit
        int r1 = state.readRegister(i.r1);
        int result = performRightShift(r1, (r1 & 0x80) != 0);
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.BCLR i) { // clear bit in SREG
        byte psr = state.readSREG();
        psr = Arithmetic.clearBit(psr, i.imm1);
        state.writeSREG(psr);
    }

    public void visit(Instr.BLD i) { // load bit from T flag into register
        boolean T = state.getFlag_T();
        byte val = state.readRegister(i.r1);
        if (T)
            val = Arithmetic.setBit(val, i.imm1);
        else
            val = Arithmetic.clearBit(val, i.imm1);
        state.writeRegister(i.r1, val);
    }

    public void visit(Instr.BRBC i) { // branch if bit in SREG is clear
        byte val = state.readSREG();
        boolean f = Arithmetic.getBit(val, i.imm1);
        if (!f)
            relativeBranch(i.imm2);
    }

    public void visit(Instr.BRBS i) { // branch if bit in SREG is set
        byte val = state.readSREG();
        boolean f = Arithmetic.getBit(val, i.imm1);
        if (f)
            relativeBranch(i.imm2);
    }

    public void visit(Instr.BRCC i) { // branch if C (carry) flag is clear
        if (!state.getFlag_C())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRCS i) { // branch if C (carry) flag is set
        if (state.getFlag_C())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BREAK i) {
        stop();
    }

    public void visit(Instr.BREQ i) { // branch if equal
        if (state.getFlag_Z())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRGE i) { // branch if greater or equal (signed)
        if (!xor(state.getFlag_N(), state.getFlag_V()))
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRHC i) { // branch if H (half carry) flag is clear
        if (!state.getFlag_H())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRHS i) { // branch if H (half carry) flag is set
        if (state.getFlag_H())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRID i) { // branch if interrupts are disabled
        if (!state.getFlag_I())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRIE i) { // branch if interrupts are enabled
        if (state.getFlag_I())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRLO i) { // branch if lower
        if (state.getFlag_C())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRLT i) { // branch if less than zero, signed
        if (xor(state.getFlag_N(), state.getFlag_V()))
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRMI i) { // branch if minus
        if (state.getFlag_N())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRNE i) { // branch if not equal
        if (!state.getFlag_Z())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRPL i) { // branch if plus
        if (!state.getFlag_N())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRSH i) { // branch if same or higher
        if (!state.getFlag_C())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRTC i) { // branch if T flag clear
        if (!state.getFlag_T())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRTS i) { // branch if T flag set
        if (state.getFlag_T())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRVC i) { // branch if V flag clear
        if (!state.getFlag_V())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRVS i) { // branch if V flag set
        if (state.getFlag_V())
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BSET i) { // set flag in SREG
        byte val = state.readSREG();
        val = Arithmetic.setBit(val, i.imm1);
        state.writeSREG(val);
    }

    public void visit(Instr.BST i) { // store bit in register to T flag
        byte val = state.readRegister(i.r1);
        boolean T = Arithmetic.getBit(val, i.imm1);
        state.setFlag_T(T);
    }

    public void visit(Instr.CALL i) { // call an absolute address
        pushPC(nextPC);
        nextPC = absolute(i.imm1);
    }

    public void visit(Instr.CBI i) { // clear bit in IO register
        state.getIORegister(i.imm1).clearBit(i.imm2);
    }

    public void visit(Instr.CBR i) { // clear bits in register
        int r1 = state.readRegister(i.r1);
        int r2 = i.imm1;
        int result = performAnd(r1, ~r2);
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.CLC i) { // clear C flag
        state.setFlag_C(false);
    }

    public void visit(Instr.CLH i) { // clear H flag
        state.setFlag_H(false);
    }

    public void visit(Instr.CLI i) { // clear I (interrupts) flag
        state.setFlag_I(false);
    }

    public void visit(Instr.CLN i) { // clear N flag
        state.setFlag_N(false);
    }

    public void visit(Instr.CLR i) { // clear register (set to zero)
        state.setFlag_S(false);
        state.setFlag_V(false);
        state.setFlag_N(false);
        state.setFlag_Z(true);
        state.writeRegister(i.r1, (byte) 0);
    }

    public void visit(Instr.CLS i) { // clear S flag
        state.setFlag_S(false);
    }

    public void visit(Instr.CLT i) { // clear T flag
        state.setFlag_T(false);
    }

    public void visit(Instr.CLV i) { // clear V flag
        state.setFlag_V(false);
    }

    public void visit(Instr.CLZ i) { // clear Z flag
        state.setFlag_Z(false);
    }

    public void visit(Instr.COM i) { // one's complement register
        int r1 = state.readRegister(i.r1);
        int result = 0xff - r1;

        boolean C = true;
        boolean N = (result & 0x80) != 0;
        boolean Z = (result & 0xff) == 0;
        boolean V = false;
        boolean S = xor(N, V);
        setFlag_CNZVS(C, N, Z, V, S);

        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.CP i) { // compare registers
        int r1 = state.readRegister(i.r1);
        int r2 = state.readRegister(i.r2);
        // perform subtraction for flag side effects.
        performSubtraction(r1, r2, 0);
    }

    public void visit(Instr.CPC i) { // compare registers with carry
        int r1 = state.readRegister(i.r1);
        int r2 = state.readRegister(i.r2);
        // perform subtraction for flag side effects.
        performSubtraction(r1, r2, (state.getFlag_C() ? 1 : 0));
    }

    public void visit(Instr.CPI i) { // compare register with immediate
        int r1 = state.readRegister(i.r1);
        int r2 = i.imm1;
        // perform subtraction for flag side effects.
        performSubtraction(r1, r2, 0);
    }

    public void visit(Instr.CPSE i) { // compare and skip next instruction if equal
        int r1 = state.readRegister(i.r1);
        int r2 = state.readRegister(i.r2);
        if (r1 == r2) skip();
    }

    public void visit(Instr.DEC i) { // decrement register
        int r1 = state.readRegisterUnsigned(i.r1);
        int result = r1 - 1;

        boolean N = (result & 0x080) != 0;
        boolean Z = (result & 0xff) == 0;
        boolean V = r1 == 0x80;
        boolean S = xor(N, V);
        setFlag_NZVS(N, Z, V, S);

        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.EICALL i) { // extended indirect call
        // Not implemented in Atmel Mega128L
        unimplemented(i);
    }

    public void visit(Instr.EIJMP i) { // extended indirect jump
        // Not implemented in Atmel Mega128L
        unimplemented(i);
    }

    public void visit(Instr.ELPM i) { // extended load program memory
        int address = readRegisterWord(Register.Z);
        int extra = state.readRAMPZ();
        byte val = state.readProgramByte(address + (extra << 16));
        state.writeRegister(Register.R0, val);
    }

    public void visit(Instr.ELPMD i) { // extended load program memory with destination
        int address = readRegisterWord(Register.Z);
        int extra = state.readRAMPZ();
        byte val = state.readProgramByte(address + (extra << 16));
        state.writeRegister(i.r1, val);
    }

    public void visit(Instr.ELPMPI i) { // extends load program memory with post decrement
        int address = readRegisterWord(Register.Z);
        int extra = state.readRAMPZ();
        byte val = state.readProgramByte(address + (extra << 16));
        state.writeRegister(i.r1, val);
        writeRegisterWord(Register.Z, address + 1);
    }

    public void visit(Instr.EOR i) { // exclusive or first register with second
        int r1 = state.readRegister(i.r1);
        int r2 = state.readRegister(i.r2);
        int result = r1 ^ r2;

        boolean N = (result & 0x080) != 0;
        boolean Z = (result & 0xff) == 0;
        boolean V = false;
        boolean S = xor(N, V);
        setFlag_NZVS(N, Z, V, S);

        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.FMUL i) { // fractional multiply
        int r1 = state.readRegisterUnsigned(i.r1);
        int r2 = state.readRegisterUnsigned(i.r2);
        int result = (r1 * r2) << 1;
        state.setFlag_Z((result & 0xffff) == 0);
        state.setFlag_C(Arithmetic.getBit(result, 16));
        state.writeRegisterWord(Register.R0, result);
    }

    public void visit(Instr.FMULS i) { // fractional multiply, signed
        int r1 = state.readRegister(i.r1);
        int r2 = state.readRegister(i.r2);
        int result = (r1 * r2) << 1;
        state.setFlag_Z((result & 0xffff) == 0);
        state.setFlag_C(Arithmetic.getBit(result, 16));
        state.writeRegisterWord(Register.R0, result);
    }

    public void visit(Instr.FMULSU i) { // fractional multiply signed with unsigned
        int r1 = state.readRegister(i.r1);
        int r2 = state.readRegisterUnsigned(i.r2);
        int result = (r1 * r2) << 1;
        state.setFlag_Z((result & 0xffff) == 0);
        state.setFlag_C(Arithmetic.getBit(result, 16));
        state.writeRegisterWord(Register.R0, result);
    }

    public void visit(Instr.ICALL i) { // indirect call through Z register
        pushPC(nextPC);
        int target = absolute(readRegisterWord(Register.Z));
        nextPC = target;
    }

    public void visit(Instr.IJMP i) { // indirect jump through Z register
        int target = absolute(readRegisterWord(Register.Z));
        nextPC = target;
    }

    public void visit(Instr.IN i) { // read byte from IO register
        byte val = state.readIORegister(i.imm1);
        state.writeRegister(i.r1, val);
    }

    public void visit(Instr.INC i) { // increment register
        int r1 = state.readRegisterUnsigned(i.r1);
        int result = r1 + 1;

        boolean N = (result & 0x080) != 0;
        boolean Z = (result & 0xff) == 0;
        boolean V = r1 == 0x7f;
        boolean S = xor(N, V);
        setFlag_NZVS(N, Z, V, S);

        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.JMP i) { // unconditional jump to absolute address
        nextPC = absolute(i.imm1);
    }

    public void visit(Instr.LD i) { // load from SRAM
        int address = readRegisterWord(i.r2);
        byte val = state.readDataByte(address);
        state.writeRegister(i.r1, val);
    }

    public void visit(Instr.LDD i) { // load with displacement from register Y or Z
        int address = readRegisterWord(i.r2) + i.imm1;
        byte val = state.readDataByte(address);
        state.writeRegister(i.r1, val);
    }

    public void visit(Instr.LDI i) { // load immediate
        state.writeRegister(i.r1, (byte) i.imm1);
    }

    public void visit(Instr.LDPD i) { // load from SRAM with pre-decrement
        int address = readRegisterWord(i.r2) - 1;
        byte val = state.readDataByte(address);
        state.writeRegister(i.r1, val);
        writeRegisterWord(i.r2, address);
    }

    public void visit(Instr.LDPI i) { // load from SRAM with post-increment
        int address = readRegisterWord(i.r2);
        byte val = state.readDataByte(address);
        state.writeRegister(i.r1, val);
        writeRegisterWord(i.r2, address + 1);
    }

    public void visit(Instr.LDS i) { // load from SRAM at absolute address
        byte val = state.readDataByte(i.imm1);
        state.writeRegister(i.r1, val);
    }

    public void visit(Instr.LPM i) { // load from program memory
        int address = readRegisterWord(Register.Z);
        byte val = state.readProgramByte(address);
        state.writeRegister(Register.R0, val);
    }

    public void visit(Instr.LPMD i) { // load from program memory with destination
        int address = readRegisterWord(Register.Z);
        byte val = state.readProgramByte(address);
        state.writeRegister(i.r1, val);
    }

    public void visit(Instr.LPMPI i) { // load from program memory with post-increment
        int address = readRegisterWord(Register.Z);
        byte val = state.readProgramByte(address);
        state.writeRegister(i.r1, val);
        writeRegisterWord(Register.Z, address + 1);
    }

    public void visit(Instr.LSL i) { // logical shift register left by one
        int r1 = state.readRegister(i.r1);
        int result = performLeftShift(r1, 0);
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.LSR i) { // logical shift register right by one
        int r1 = state.readRegister(i.r1);
        int result = performRightShift(r1, false);
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.MOV i) { // copy second register into first
        byte result = state.readRegister(i.r2);
        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.MOVW i) { // copy second register pair into first
        int result = readRegisterWord(i.r2);
        writeRegisterWord(i.r1, result);
    }

    public void visit(Instr.MUL i) { // multiply first register with second
        int r1 = state.readRegisterUnsigned(i.r1);
        int r2 = state.readRegisterUnsigned(i.r2);
        int result = r1 * r2;
        state.setFlag_C(Arithmetic.getBit(result, 15));
        state.setFlag_Z((result & 0xffff) == 0);
        writeRegisterWord(Register.R0, result);
    }

    public void visit(Instr.MULS i) { // multiply first register with second, signed
        int r1 = state.readRegister(i.r1);
        int r2 = state.readRegister(i.r2);
        int result = r1 * r2;
        state.setFlag_C(Arithmetic.getBit(result, 15));
        state.setFlag_Z((result & 0xffff) == 0);
        writeRegisterWord(Register.R0, result);
    }

    public void visit(Instr.MULSU i) { // multiply first register with second, signed and unsigned
        int r1 = state.readRegister(i.r1);
        int r2 = state.readRegisterUnsigned(i.r2);
        int result = r1 * r2;
        state.setFlag_C(Arithmetic.getBit(result, 15));
        state.setFlag_Z((result & 0xffff) == 0);
        writeRegisterWord(Register.R0, result);
    }

    public void visit(Instr.NEG i) { // negate register
        int r1 = state.readRegister(i.r1);
        int result = performSubtraction(0, r1, 0);
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.NOP i) { // no-op operation
        // do nothing.
    }

    public void visit(Instr.OR i) { // or first register with second
        int r1 = state.readRegister(i.r1);
        int r2 = state.readRegister(i.r2);
        int result = performOr(r1, r2);

        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.ORI i) { // or register with immediate
        int r1 = state.readRegister(i.r1);
        int r2 = i.imm1;
        int result = performOr(r1, r2);

        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.OUT i) { // write byte to IO register
        byte r1 = state.readRegister(i.r1);
        state.writeIORegister(r1, i.imm1);
    }

    public void visit(Instr.POP i) { // pop a byte from the stack (SPL:SPH IO registers)
        byte val = state.popByte();
        state.writeRegister(i.r1, val);
    }

    public void visit(Instr.PUSH i) { // push a byte to the stack
        byte val = state.readRegister(i.r1);
        state.pushByte(val);
    }

    public void visit(Instr.RCALL i) { // call a relative address
        pushPC(nextPC);
        nextPC = relative(i.imm1);
    }

    public void visit(Instr.RET i) { // return from procedure
        nextPC = popPC();
    }

    public void visit(Instr.RETI i) { // return from interrupt
        nextPC = popPC();
        state.setFlag_I(true);
        justReturnedFromInterrupt = true;
    }

    public void visit(Instr.RJMP i) { // relative jump
        nextPC = relative(i.imm1);
    }

    public void visit(Instr.ROL i) { // rotate register left through carry flag
        int r1 = state.readRegisterUnsigned(i.r1);
        int result = performLeftShift(r1, (state.getFlag_C() ? 1 : 0));

        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.ROR i) { // rotate register right through carry flag
        int r1 = state.readRegister(i.r1);
        int result = performRightShift(r1, state.getFlag_C());
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.SBC i) { // subtract second register from first with carry
        int r1 = state.readRegister(i.r1);
        int r2 = state.readRegister(i.r2);
        int result = performSubtraction(r1, r2, (state.getFlag_C() ? 1 : 0));
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.SBCI i) { // subtract immediate from register with carry
        int r1 = state.readRegister(i.r1);
        int r2 = i.imm1;
        int result = performSubtraction(r1, r2, (state.getFlag_C() ? 1 : 0));
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.SBI i) { // set bit in IO register
        state.getIORegister(i.imm1).setBit(i.imm2);
    }

    public void visit(Instr.SBIC i) { // skip if bit in IO register is clear
        byte val = state.readIORegister(i.imm1);
        boolean f = Arithmetic.getBit(val, i.imm2);
        if (!f) skip();
    }

    public void visit(Instr.SBIS i) { // skip if bit in IO register is set
        byte val = state.readIORegister(i.imm1);
        boolean f = Arithmetic.getBit(val, i.imm2);
        if (f) skip();
    }

    public void visit(Instr.SBIW i) { // subtract immediate from word
        int val = readRegisterWord(i.r1);
        int result = val - i.imm1;

        boolean Rdh7 = Arithmetic.getBit(val, 15);
        boolean R15 = Arithmetic.getBit(result, 15);

        boolean V = Rdh7 && !R15;
        boolean N = R15;
        boolean Z = (result & 0xffff) == 0;
        boolean C = R15 && !Rdh7;
        boolean S = xor(N, V);
        setFlag_CNZVS(C, N, Z, V, S);

        writeRegisterWord(i.r1, result);
    }

    public void visit(Instr.SBR i) { // set bits in register
        int r1 = state.readRegister(i.r1);
        int r2 = i.imm1;
        int result = performOr(r1, r2);

        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.SBRC i) { // skip if bit in register cleared
        byte r1 = state.readRegister(i.r1);
        boolean f = Arithmetic.getBit(r1, i.imm1);
        if (!f) skip();
    }

    public void visit(Instr.SBRS i) { // skip if bit in register set
        byte r1 = state.readRegister(i.r1);
        boolean f = Arithmetic.getBit(r1, i.imm1);
        if (f) skip();
    }

    public void visit(Instr.SEC i) { // set C (carry) flag
        state.setFlag_C(true);
    }

    public void visit(Instr.SEH i) { // set H (half carry) flag
        state.setFlag_H(true);
    }

    public void visit(Instr.SEI i) { // set I (interrupts) flag
        state.setFlag_I(true);
    }

    public void visit(Instr.SEN i) { // set N (negative) flag
        state.setFlag_N(true);
    }

    public void visit(Instr.SER i) { // set register to 0xFF
        state.writeRegister(i.r1, (byte)0xff);
    }

    public void visit(Instr.SES i) { // set S (signed) flag
        state.setFlag_S(true);
    }

    public void visit(Instr.SET i) { // set T flag
        state.setFlag_T(true);
    }

    public void visit(Instr.SEV i) { // set V flag
        state.setFlag_V(true);
    }

    public void visit(Instr.SEZ i) { // set Z (zero) flag
        state.setFlag_Z(true);
    }

    public void visit(Instr.SLEEP i) {
        unimplemented(i);
    }

    public void visit(Instr.SPM i) { // store register to program memory
        // TODO: figure out how this instruction behaves on Atmega128L
        unimplemented(i);
    }

    public void visit(Instr.ST i) { // store register to data-seg[r1]
        int address = readRegisterWord(i.r1);
        byte val = state.readRegister(i.r2);
        state.writeDataByte(val, address);
    }

    public void visit(Instr.STD i) { // store to data space with displacement from Y or Z
        int address = readRegisterWord(i.r1) + i.imm1;
        byte val = state.readRegister(i.r2);
        state.writeDataByte(val, address);
    }

    public void visit(Instr.STPD i) { // decrement r2 and store register to data-seg(r2)
        int address = readRegisterWord(i.r1) - 1;
        byte val = state.readRegister(i.r2);
        state.writeDataByte(val, address);
        writeRegisterWord(i.r1, address);
    }

    public void visit(Instr.STPI i) { // store register to data-seg(r2) and post-inc
        int address = readRegisterWord(i.r1);
        byte val = state.readRegister(i.r2);
        state.writeDataByte(val, address);
        writeRegisterWord(i.r1, address + 1);
    }

    public void visit(Instr.STS i) { // store direct to data-seg(imm1)
        byte val = state.readRegister(i.r1);
        state.writeDataByte(val, i.imm1);
    }

    public void visit(Instr.SUB i) { // subtract second register from first
        int r1 = state.readRegister(i.r1);
        int r2 = state.readRegister(i.r2);
        int result = performSubtraction(r1, r2, 0);
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.SUBI i) { // subtract immediate from register
        int r1 = state.readRegister(i.r1);
        int r2 = i.imm1;
        int result = performSubtraction(r1, r2, 0);
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.SWAP i) { // swap nibbles in register
        int result = state.readRegisterUnsigned(i.r1);
        result = (result >> 4) | (result << 4);
        state.writeRegister(i.r1, (byte) result);
    }

    public void visit(Instr.TST i) { // test for zero or minus
        int r1 = state.readRegister(i.r1);
        state.setFlag_V(false);
        computeFlag_ZN(r1);
        computeFlag_S();
    }

    public void visit(Instr.WDR i) { // watchdog reset
        unimplemented(i);
    }

    /**
     *  U T I L I T I E S
     * ------------------------------------------------------------
     *
     *  These are utility functions for expressing instructions
     *  more concisely. They are private and can be inlined by
     *  the JIT compiler or javac -O.
     *
     */

    private void relativeBranch(int offset) {
        nextPC = relative(offset);
        state.consumeCycle();
    }

    private void skip() {
        // skip over next instruction
        int dist = state.readInstr(nextPC).getSize();
        if (dist == 2)
            state.consumeCycle();
        else
            state.consumeCycles(2);
        nextPC = nextPC + dist;
    }

    private int relative(int imm1) {
        return 2 + 2 * imm1 + state.getPC();
    }

    private int absolute(int imm1) {
        return 2 * imm1;
    }

    private void pushPC(int pc) {
        pc = pc / 2;
        state.pushByte(Arithmetic.high(pc));
        state.pushByte(Arithmetic.low(pc));
    }

    private void pushPC() {
        int pc = state.getPC() / 2;
        state.pushByte(Arithmetic.high(pc));
        state.pushByte(Arithmetic.low(pc));
    }

    private int popPC() {
        byte low = state.popByte();
        byte high = state.popByte();
        return Arithmetic.uword(low, high) * 2;
    }

    private int readRegisterWord(Register r1) {
        return state.readRegisterWord(r1);
    }

    private void writeRegisterWord(Register r1, int val) {
        state.writeRegisterWord(r1, val);
    }

    private void unimplemented(Instr i) {
        if (TRACE) {
            ColorTerminal.printRed("unimplemented: " + i.getVariant());
            ColorTerminal.nextln();
        }
    }

    private boolean bit(int val, int bit) {
        return Arithmetic.getBit(val, bit);
    }

    private boolean xor(boolean a, boolean b) {
        return (a && !b) || (b && !a);
    }

    private void setFlag_HCNZVS(boolean H, boolean C, boolean N, boolean Z, boolean V, boolean S) {
        state.setFlag_H(H);
        state.setFlag_C(C);
        state.setFlag_N(N);
        state.setFlag_Z(Z);
        state.setFlag_V(V);
        state.setFlag_S(S);
    }

    private void setFlag_CNZVS(boolean C, boolean N, boolean Z, boolean V, boolean S) {
        state.setFlag_C(C);
        state.setFlag_N(N);
        state.setFlag_Z(Z);
        state.setFlag_V(V);
        state.setFlag_S(S);
    }

    private void setFlag_NZVS(boolean N, boolean Z, boolean V, boolean S) {
        state.setFlag_N(N);
        state.setFlag_Z(Z);
        state.setFlag_V(V);
        state.setFlag_S(S);
    }

    private void computeFlag_ZN(int result) {
        state.setFlag_Z((result & 0xff) == 0);
        state.setFlag_N(Arithmetic.getBit(result, 7));
    }

    private void computeFlag_S() {
        state.setFlag_S(xor(state.getFlag_N(), state.getFlag_V()));
    }

    private void computeFlag_V() {
        state.setFlag_V(xor(state.getFlag_N(), state.getFlag_C()));
    }

    private int performAddition(int r1, int r2, int carry) {
        int result = r1 + r2 + carry;
        int ral = r1 & 0xf;
        int rbl = r2 & 0xf;

        boolean Rd7 = Arithmetic.getBit(r1, 7);
        boolean Rr7 = Arithmetic.getBit(r2, 7);
        boolean R7 = Arithmetic.getBit(result, 7);

        // set the flags as per instruction set documentation.
        boolean H = ((ral + rbl + carry) & 0x10) != 0;
        boolean C = (result & 0x100) != 0;
        boolean N = (result & 0x080) != 0;
        boolean Z = (result & 0xff) == 0;
        boolean V = (Rd7 && Rr7 && !R7) || (!Rd7 && !Rr7 && R7);
        boolean S = xor(N, V);

        setFlag_HCNZVS(H, C, N, Z, V, S);
        return result;
    }

    private int performSubtraction(int r1, int r2, int carry) {
        int result = r1 - r2 - carry;

        boolean Rd7 = Arithmetic.getBit(r1, 7);
        boolean Rr7 = Arithmetic.getBit(r2, 7);
        boolean R7 = Arithmetic.getBit(result, 7);
        boolean Rd3 = Arithmetic.getBit(r1, 3);
        boolean Rr3 = Arithmetic.getBit(r2, 3);
        boolean R3 = Arithmetic.getBit(result, 3);

        // set the flags as per instruction set documentation.
        boolean H = (!Rd3 && Rr3) || (Rr3 && R3) || (R3 && !Rd3);
        boolean C = (!Rd7 && Rr7) || (Rr7 && R7) || (R7 && !Rd7);
        boolean N = (result & 0x080) != 0;
        boolean Z = (result & 0xff) == 0;
        boolean V = (Rd7 && !Rr7 && !R7) || (!Rd7 && Rr7 && R7);
        boolean S = xor(N, V);

        setFlag_HCNZVS(H, C, N, Z, V, S);
        return result;
    }

    private int performLeftShift(int r1, int lowbit) {
        int result = r1 << 1 | lowbit;

        boolean H = (result & 0x010) != 0;
        boolean C = (result & 0x100) != 0;
        boolean N = (result & 0x080) != 0;
        boolean Z = (result & 0xff) == 0;
        boolean V = xor(N, C);
        boolean S = xor(N, V);
        setFlag_HCNZVS(H, C, N, Z, V, S);
        return result;
    }

    private int performRightShift(int r1, boolean highbit) {
        int result = ((r1 & 0xff) >> 1) | (highbit ? 0x80 : 0);

        boolean C = (r1 & 0x01) != 0;
        boolean N = highbit;
        boolean Z = (result & 0xff) == 0;
        boolean V = xor(N, C);
        boolean S = xor(N, V);
        setFlag_CNZVS(C, N, Z, V, S);
        return result;
    }

    private int performOr(int r1, int r2) {
        int result = r1 | r2;

        boolean N = (result & 0x080) != 0;
        boolean Z = (result & 0xff) == 0;
        boolean V = false;
        boolean S = xor(N, V);
        setFlag_NZVS(N, Z, V, S);

        return result;
    }

    private int performAnd(int r1, int r2) {
        int result = r1 & r2;

        boolean N = (result & 0x080) != 0;
        boolean Z = (result & 0xff) == 0;
        boolean V = false;
        boolean S = xor(N, V);
        setFlag_NZVS(N, Z, V, S);

        return result;
    }

}
