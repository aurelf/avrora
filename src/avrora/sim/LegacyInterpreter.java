
package avrora.sim;

import avrora.core.InstrVisitor;
import avrora.core.Program;
import avrora.core.Instr;
import avrora.core.Register;
import avrora.Avrora;
import avrora.util.Arithmetic;

/**
 * @author Ben L. Titzer
 */
class LegacyInterpreter extends BaseInterpreter implements InstrVisitor {

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
    protected LegacyInterpreter(Simulator simulator, Program p, int flash_size, int ioreg_size, int sram_size) {
        super(simulator, p, flash_size, ioreg_size, sram_size);
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
                int curPC = nextPC;
                Instr i = impression.readInstr(nextPC);
                nextPC = nextPC + i.properties.size;

                // visit the actual instruction (or probe)
                // OPTIMIZATION OPPORTUNITY: common case of no active global probes
                // could be approximately 18% of loop overhead
                simulator.activeProbe.fireBefore(i, curPC, this);
                execute(i);
                simulator.activeProbe.fireAfter(i, curPC, this);
            }
        }
    }


    private void execute(Instr i) {
        i.accept(this);
        pc = nextPC;
        // process any timed events and advance state clock
        advanceCycles(cyclesConsumed + i.properties.cycles);
    }

    private void executeProbed(Instr instr, int address, Simulator.Probe probe) {
        // fire the probe(s) before
        probe.fireBefore(instr, address, this);

        // execute actual instruction
        instr.accept(this);
        pc = nextPC;
        advanceCycles(cyclesConsumed + instr.properties.cycles);

        // fire the probe(s) after
        probe.fireAfter(instr, address, this);

    }

    public void visit(Instr.ADC i) { // add two registers and carry flag
        int r1 = getRegisterUnsigned(i.r1);
        int r2 = getRegisterUnsigned(i.r2);
        int result = performAddition(r1, r2, C ? 1 : 0);
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.ADD i) { // add second register to first
        int r1 = getRegisterUnsigned(i.r1);
        int r2 = getRegisterUnsigned(i.r2);
        int result = performAddition(r1, r2, 0);
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.ADIW i) { // add immediate to word register
        int r1 = getRegisterWord(i.r1);
        int r2 = i.imm1;
        int result = r1 + r2;
        boolean R15 = Arithmetic.getBit(result, 15);
        boolean Rdh7 = Arithmetic.getBit(r1, 15);

        C = (!R15 && Rdh7);
        N = (R15);
        V = (!Rdh7 && R15);
        Z = ((result & 0xffff) == 0);
        S = (xor(N, V));

        setRegisterWord(i.r1, result);
    }

    public void visit(Instr.AND i) { // and first register with second
        int r1 = getRegisterByte(i.r1);
        int r2 = getRegisterByte(i.r2);
        int result = performAnd(r1, r2);
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.ANDI i) { // and register with immediate
        int r1 = getRegisterByte(i.r1);
        int r2 = i.imm1;
        int result = performAnd(r1, r2);
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.ASR i) { // arithmetic shift right by one bit
        int r1 = getRegisterByte(i.r1);
        int result = performRightShift(r1, (r1 & 0x80) != 0);
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.BCLR i) { // clear bit in SREG
        SREG_reg.clearBit(i.imm1);
    }

    public void visit(Instr.BLD i) { // load bit from T flag into register
        byte val = getRegisterByte(i.r1);
        if (T)
            val = Arithmetic.setBit(val, i.imm1);
        else
            val = Arithmetic.clearBit(val, i.imm1);
        setRegisterByte(i.r1, val);
    }

    public void visit(Instr.BRBC i) { // branch if bit in SREG is clear
        byte val = getSREG();
        boolean f = Arithmetic.getBit(val, i.imm1);
        if (!f)
            relativeBranch(i.imm2);
    }

    public void visit(Instr.BRBS i) { // branch if bit in SREG is set
        byte val = getSREG();
        boolean f = Arithmetic.getBit(val, i.imm1);
        if (f)
            relativeBranch(i.imm2);
    }

    public void visit(Instr.BRCC i) { // branch if C (carry) flag is clear
        if (!C)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRCS i) { // branch if C (carry) flag is set
        if (C)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BREAK i) {
        simulator.stop();
    }

    public void visit(Instr.BREQ i) { // branch if equal
        if (Z)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRGE i) { // branch if greater or equal (signed)
        if (!S)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRHC i) { // branch if H (half carry) flag is clear
        if (!H)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRHS i) { // branch if H (half carry) flag is set
        if (H)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRID i) { // branch if interrupts are disabled
        if (!I)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRIE i) { // branch if interrupts are enabled
        if (I)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRLO i) { // branch if lower
        if (C)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRLT i) { // branch if less than zero, signed
        if (S)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRMI i) { // branch if minus
        if (N)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRNE i) { // branch if not equal
        if (!Z)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRPL i) { // branch if plus
        if (!N)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRSH i) { // branch if same or higher
        if (!C)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRTC i) { // branch if T flag clear
        if (!T)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRTS i) { // branch if T flag set
        if (T)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRVC i) { // branch if V flag clear
        if (!V)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BRVS i) { // branch if V flag set
        if (V)
            relativeBranch(i.imm1);
    }

    public void visit(Instr.BSET i) { // set flag in SREG
        SREG_reg.setBit(i.imm1);
    }

    public void visit(Instr.BST i) { // store bit in register to T flag
        byte val = getRegisterByte(i.r1);
        T = Arithmetic.getBit(val, i.imm1);
    }

    public void visit(Instr.CALL i) { // call an absolute address
        pushPC(nextPC);
        nextPC = absolute(i.imm1);
    }

    public void visit(Instr.CBI i) { // clear bit in IO register
        getIOReg(i.imm1).clearBit(i.imm2);
    }

    public void visit(Instr.CBR i) { // clear bits in register
        int r1 = getRegisterByte(i.r1);
        int r2 = i.imm1;
        int result = performAnd(r1, ~r2);
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.CLC i) { // clear C flag
        C = (false);
    }

    public void visit(Instr.CLH i) { // clear H flag
        H = (false);
    }

    public void visit(Instr.CLI i) { // clear I (interrupts) flag
        I = (false);
    }

    public void visit(Instr.CLN i) { // clear N flag
        N = (false);
    }

    public void visit(Instr.CLR i) { // clear register (set to zero)
        S = (false);
        V = (false);
        N = (false);
        Z = (true);
        setRegisterByte(i.r1, (byte) 0);
    }

    public void visit(Instr.CLS i) { // clear S flag
        S = (false);
    }

    public void visit(Instr.CLT i) { // clear T flag
        T = (false);
    }

    public void visit(Instr.CLV i) { // clear V flag
        V = (false);
    }

    public void visit(Instr.CLZ i) { // clear Z flag
        Z = (false);
    }

    public void visit(Instr.COM i) { // one's complement register
        int r1 = getRegisterByte(i.r1);
        int result = 0xff - r1;

        C = true;
        N = (result & 0x80) != 0;
        Z = (result & 0xff) == 0;
        V = false;
        S = xor(N, V);

        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.CP i) { // compare registers
        int r1 = getRegisterByte(i.r1);
        int r2 = getRegisterByte(i.r2);
        // perform subtraction for flag side effects.
        performSubtraction(r1, r2, 0);
    }

    public void visit(Instr.CPC i) { // compare registers with carry
        int r1 = getRegisterByte(i.r1);
        int r2 = getRegisterByte(i.r2);
        // perform subtraction for flag side effects.
        performSubtractionPZ(r1, r2, (C ? 1 : 0));
    }

    public void visit(Instr.CPI i) { // compare register with immediate
        int r1 = getRegisterByte(i.r1);
        int r2 = i.imm1;
        // perform subtraction for flag side effects.
        performSubtraction(r1, r2, 0);
    }

    public void visit(Instr.CPSE i) { // compare and skip next instruction if equal
        int r1 = getRegisterByte(i.r1);
        int r2 = getRegisterByte(i.r2);
        // TODO: test this instruction more thoroughly!!!!
        performSubtraction(r1, r2, 0);
        if (r1 == r2) skip();
    }

    public void visit(Instr.DEC i) { // decrement register
        int r1 = getRegisterUnsigned(i.r1);
        int result = r1 - 1;

        N = (result & 0x080) != 0;
        Z = (result & 0xff) == 0;
        V = r1 == 0x80;
        S = xor(N, V);

        setRegisterByte(i.r1, (byte) result);
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
        int address = getRegisterWord(Register.Z);
        int extra = getIORegisterByte(RAMPZ);
        byte val = getProgramByte(address + (extra << 16));
        setRegisterByte(Register.R0, val);
    }

    public void visit(Instr.ELPMD i) { // extended load program memory with destination
        int address = getRegisterWord(Register.Z);
        int extra = getIORegisterByte(RAMPZ);
        byte val = getProgramByte(address + (extra << 16));
        setRegisterByte(i.r1, val);
    }

    public void visit(Instr.ELPMPI i) { // extends load program memory with post decrement
        int address = getRegisterWord(Register.Z);
        int extra = getIORegisterByte(RAMPZ);
        byte val = getProgramByte(address + (extra << 16));
        setRegisterByte(i.r1, val);
        setRegisterWord(Register.Z, address + 1);
    }

    public void visit(Instr.EOR i) { // exclusive or first register with second
        int r1 = getRegisterByte(i.r1);
        int r2 = getRegisterByte(i.r2);
        int result = r1 ^ r2;

        N = (result & 0x080) != 0;
        Z = (result & 0xff) == 0;
        V = false;
        S = xor(N, V);

        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.FMUL i) { // fractional multiply
        int r1 = getRegisterUnsigned(i.r1);
        int r2 = getRegisterUnsigned(i.r2);
        int result = (r1 * r2) << 1;
        Z = ((result & 0xffff) == 0);
        C = (Arithmetic.getBit(result, 16));
        setRegisterWord(Register.R0, result);
    }

    public void visit(Instr.FMULS i) { // fractional multiply, signed
        int r1 = getRegisterByte(i.r1);
        int r2 = getRegisterByte(i.r2);
        int result = (r1 * r2) << 1;
        Z = ((result & 0xffff) == 0);
        C = (Arithmetic.getBit(result, 16));
        setRegisterWord(Register.R0, result);
    }

    public void visit(Instr.FMULSU i) { // fractional multiply signed with unsigned
        int r1 = getRegisterByte(i.r1);
        int r2 = getRegisterUnsigned(i.r2);
        int result = (r1 * r2) << 1;
        Z = ((result & 0xffff) == 0);
        C = (Arithmetic.getBit(result, 16));
        setRegisterWord(Register.R0, result);
    }

    public void visit(Instr.ICALL i) { // indirect call through Z register
        pushPC(nextPC);
        int target = absolute(getRegisterWord(Register.Z));
        nextPC = target;
    }

    public void visit(Instr.IJMP i) { // indirect jump through Z register
        int target = absolute(getRegisterWord(Register.Z));
        nextPC = target;
    }

    public void visit(Instr.IN i) { // read byte from IO register
        byte val = getIORegisterByte(i.imm1);
        setRegisterByte(i.r1, val);
    }

    public void visit(Instr.INC i) { // increment register
        int r1 = getRegisterUnsigned(i.r1);
        int result = r1 + 1;

        N = (result & 0x080) != 0;
        Z = (result & 0xff) == 0;
        V = r1 == 0x7f;
        S = xor(N, V);

        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.JMP i) { // unconditional jump to absolute address
        nextPC = absolute(i.imm1);
    }

    public void visit(Instr.LD i) { // load from SRAM
        int address = getRegisterWord(i.r2);
        byte val = getDataByte(address);
        setRegisterByte(i.r1, val);
    }

    public void visit(Instr.LDD i) { // load with displacement from register Y or Z
        int address = getRegisterWord(i.r2) + i.imm1;
        byte val = getDataByte(address);
        setRegisterByte(i.r1, val);
    }

    public void visit(Instr.LDI i) { // load immediate
        setRegisterByte(i.r1, (byte) i.imm1);
    }

    public void visit(Instr.LDPD i) { // load from SRAM with pre-decrement
        int address = getRegisterWord(i.r2) - 1;
        byte val = getDataByte(address);
        setRegisterByte(i.r1, val);
        setRegisterWord(i.r2, address);
    }

    public void visit(Instr.LDPI i) { // load from SRAM with post-increment
        int address = getRegisterWord(i.r2);
        byte val = getDataByte(address);
        setRegisterByte(i.r1, val);
        setRegisterWord(i.r2, address + 1);
    }

    public void visit(Instr.LDS i) { // load from SRAM at absolute address
        byte val = getDataByte(i.imm1);
        setRegisterByte(i.r1, val);
    }

    public void visit(Instr.LPM i) { // load from program memory
        int address = getRegisterWord(Register.Z);
        byte val = getProgramByte(address);
        setRegisterByte(Register.R0, val);
    }

    public void visit(Instr.LPMD i) { // load from program memory with destination
        int address = getRegisterWord(Register.Z);
        byte val = getProgramByte(address);
        setRegisterByte(i.r1, val);
    }

    public void visit(Instr.LPMPI i) { // load from program memory with post-increment
        int address = getRegisterWord(Register.Z);
        byte val = getProgramByte(address);
        setRegisterByte(i.r1, val);
        setRegisterWord(Register.Z, address + 1);
    }

    public void visit(Instr.LSL i) { // logical shift register left by one
        int r1 = getRegisterByte(i.r1);
        int result = performLeftShift(r1, 0);
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.LSR i) { // logical shift register right by one
        int r1 = getRegisterByte(i.r1);
        int result = performRightShift(r1, false);
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.MOV i) { // copy second register into first
        byte result = getRegisterByte(i.r2);
        setRegisterByte(i.r1, result);
    }

    public void visit(Instr.MOVW i) { // copy second register pair into first
        int result = getRegisterWord(i.r2);
        setRegisterWord(i.r1, result);
    }

    public void visit(Instr.MUL i) { // multiply first register with second
        int r1 = getRegisterUnsigned(i.r1);
        int r2 = getRegisterUnsigned(i.r2);
        int result = r1 * r2;
        C = (Arithmetic.getBit(result, 15));
        Z = ((result & 0xffff) == 0);
        setRegisterWord(Register.R0, result);
    }

    public void visit(Instr.MULS i) { // multiply first register with second, signed
        int r1 = getRegisterByte(i.r1);
        int r2 = getRegisterByte(i.r2);
        int result = r1 * r2;
        C = (Arithmetic.getBit(result, 15));
        Z = ((result & 0xffff) == 0);
        setRegisterWord(Register.R0, result);
    }

    public void visit(Instr.MULSU i) { // multiply first register with second, signed and unsigned
        int r1 = getRegisterByte(i.r1);
        int r2 = getRegisterUnsigned(i.r2);
        int result = r1 * r2;
        C = (Arithmetic.getBit(result, 15));
        Z = ((result & 0xffff) == 0);
        setRegisterWord(Register.R0, result);
    }

    public void visit(Instr.NEG i) { // negate register
        int r1 = getRegisterByte(i.r1);
        int result = performSubtraction(0, r1, 0);
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.NOP i) { // no-op operation
        // do nothing.
    }

    public void visit(Instr.OR i) { // or first register with second
        int r1 = getRegisterByte(i.r1);
        int r2 = getRegisterByte(i.r2);
        int result = performOr(r1, r2);

        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.ORI i) { // or register with immediate
        int r1 = getRegisterByte(i.r1);
        int r2 = i.imm1;
        int result = performOr(r1, r2);

        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.OUT i) { // write byte to IO register
        byte r1 = getRegisterByte(i.r1);
        setIORegisterByte(i.imm1, r1);
    }

    public void visit(Instr.POP i) { // pop a byte from the stack (SPL:SPH IO registers)
        byte val = popByte();
        setRegisterByte(i.r1, val);
    }

    public void visit(Instr.PUSH i) { // push a byte to the stack
        byte val = getRegisterByte(i.r1);
        pushByte(val);
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
        I = (true);
        justReturnedFromInterrupt = true;
    }

    public void visit(Instr.RJMP i) { // relative jump
        nextPC = relative(i.imm1);
    }

    public void visit(Instr.ROL i) { // rotate register left through carry flag
        int r1 = getRegisterUnsigned(i.r1);
        int result = performLeftShift(r1, (C ? 1 : 0));

        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.ROR i) { // rotate register right through carry flag
        int r1 = getRegisterByte(i.r1);
        int result = performRightShift(r1, C);
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.SBC i) { // subtract second register from first with carry
        int r1 = getRegisterByte(i.r1);
        int r2 = getRegisterByte(i.r2);
        int result = performSubtractionPZ(r1, r2, (C ? 1 : 0));
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.SBCI i) { // subtract immediate from register with carry
        int r1 = getRegisterByte(i.r1);
        int r2 = i.imm1;
        int result = performSubtractionPZ(r1, r2, (C ? 1 : 0));
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.SBI i) { // set bit in IO register
        getIOReg(i.imm1).setBit(i.imm2);
    }

    public void visit(Instr.SBIC i) { // skip if bit in IO register is clear
        // TODO: use readBit() and test
        byte val = getIORegisterByte(i.imm1);
        boolean f = Arithmetic.getBit(val, i.imm2);
        if (!f) skip();
    }

    public void visit(Instr.SBIS i) { // skip if bit in IO register is set
        byte val = getIORegisterByte(i.imm1);
        boolean f = Arithmetic.getBit(val, i.imm2);
        if (f) skip();
    }

    public void visit(Instr.SBIW i) { // subtract immediate from word
        int val = getRegisterWord(i.r1);
        int result = val - i.imm1;

        boolean Rdh7 = Arithmetic.getBit(val, 15);
        boolean R15 = Arithmetic.getBit(result, 15);

        V = Rdh7 && !R15;
        N = R15;
        Z = (result & 0xffff) == 0;
        C = R15 && !Rdh7;
        S = xor(N, V);

        setRegisterWord(i.r1, result);
    }

    public void visit(Instr.SBR i) { // set bits in register
        int r1 = getRegisterByte(i.r1);
        int r2 = i.imm1;
        int result = performOr(r1, r2);

        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.SBRC i) { // skip if bit in register cleared
        byte r1 = getRegisterByte(i.r1);
        boolean f = Arithmetic.getBit(r1, i.imm1);
        if (!f) skip();
    }

    public void visit(Instr.SBRS i) { // skip if bit in register set
        byte r1 = getRegisterByte(i.r1);
        boolean f = Arithmetic.getBit(r1, i.imm1);
        if (f) skip();
    }

    public void visit(Instr.SEC i) { // set C (carry) flag
        C = (true);
    }

    public void visit(Instr.SEH i) { // set H (half carry) flag
        H = (true);
    }

    public void visit(Instr.SEI i) { // set I (interrupts) flag
        I = (true);
    }

    public void visit(Instr.SEN i) { // set N (negative) flag
        N = (true);
    }

    public void visit(Instr.SER i) { // set register to 0xFF
        setRegisterByte(i.r1, (byte) 0xff);
    }

    public void visit(Instr.SES i) { // set S (signed) flag
        S = (true);
    }

    public void visit(Instr.SET i) { // set T flag
        T = (true);
    }

    public void visit(Instr.SEV i) { // set V flag
        V = (true);
    }

    public void visit(Instr.SEZ i) { // set Z (zero) flag
        Z = (true);
    }

    public void visit(Instr.SLEEP i) {
        sleeping = true;
    }

    public void visit(Instr.SPM i) { // store register to program memory
        // TODO: figure out how this instruction behaves on Atmega128L
        unimplemented(i);
    }

    public void visit(Instr.ST i) { // store register to data-seg[r1]
        int address = getRegisterWord(i.r1);
        byte val = getRegisterByte(i.r2);
        setDataByte(address, val);
    }

    public void visit(Instr.STD i) { // store to data space with displacement from Y or Z
        int address = getRegisterWord(i.r1) + i.imm1;
        byte val = getRegisterByte(i.r2);
        setDataByte(address, val);
    }

    public void visit(Instr.STPD i) { // decrement r2 and store register to data-seg(r2)
        int address = getRegisterWord(i.r1) - 1;
        byte val = getRegisterByte(i.r2);
        setDataByte(address, val);
        setRegisterWord(i.r1, address);
    }

    public void visit(Instr.STPI i) { // store register to data-seg(r2) and post-inc
        int address = getRegisterWord(i.r1);
        byte val = getRegisterByte(i.r2);
        setDataByte(address, val);
        setRegisterWord(i.r1, address + 1);
    }

    public void visit(Instr.STS i) { // store direct to data-seg(imm1)
        byte val = getRegisterByte(i.r1);
        setDataByte(i.imm1, val);
    }

    public void visit(Instr.SUB i) { // subtract second register from first
        int r1 = getRegisterByte(i.r1);
        int r2 = getRegisterByte(i.r2);
        int result = performSubtraction(r1, r2, 0);
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.SUBI i) { // subtract immediate from register
        int r1 = getRegisterByte(i.r1);
        int r2 = i.imm1;
        int result = performSubtraction(r1, r2, 0);
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.SWAP i) { // swap nibbles in register
        int result = getRegisterUnsigned(i.r1);
        result = (result >> 4) | (result << 4);
        setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.TST i) { // test for zero or minus
        int r1 = getRegisterByte(i.r1);
        V = (false);
        Z = ((r1 & 0xff) == 0);
        N = (Arithmetic.getBit(r1, 7));
        S = (xor(N, V));
    }

    public void visit(Instr.WDR i) { // watchdog reset
        unimplemented(i);
    }

    //
    //  U T I L I T I E S
    // ------------------------------------------------------------
    //
    //  These are utility functions for expressing instructions
    //  more concisely. They are private and can be inlined by
    //  the JIT compiler or javac -O.
    //
    //

    private void relativeBranch(int offset) {
        nextPC = relative(offset);
        cyclesConsumed++;
    }

    private void skip() {
        // skip over next instruction
        int dist = impression.readInstr(nextPC).properties.size;
        if (dist == 2)
            cyclesConsumed++;
        else
            cyclesConsumed += 2;
        nextPC = nextPC + dist;
    }

    private int relative(int imm1) {
        return 2 + 2 * imm1 + pc;
    }

    private int absolute(int imm1) {
        return 2 * imm1;
    }

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

    private boolean xor(boolean a, boolean b) {
        return (a && !b) || (b && !a);
    }

    private int performAddition(int r1, int r2, int carry) {
        int result = r1 + r2 + carry;
        int ral = r1 & 0xf;
        int rbl = r2 & 0xf;

        boolean Rd7 = Arithmetic.getBit(r1, 7);
        boolean Rr7 = Arithmetic.getBit(r2, 7);
        boolean R7 = Arithmetic.getBit(result, 7);

        // set the flags as per instruction set documentation.
        H = ((ral + rbl + carry) & 0x10) != 0;
        C = (result & 0x100) != 0;
        N = (result & 0x080) != 0;
        Z = (result & 0xff) == 0;
        V = (Rd7 && Rr7 && !R7) || (!Rd7 && !Rr7 && R7);
        S = xor(N, V);

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
        H = (!Rd3 && Rr3) || (Rr3 && R3) || (R3 && !Rd3);
        C = (!Rd7 && Rr7) || (Rr7 && R7) || (R7 && !Rd7);
        N = (result & 0x080) != 0;
        Z = (result & 0xff) == 0;
        V = (Rd7 && !Rr7 && !R7) || (!Rd7 && Rr7 && R7);
        S = xor(N, V);

        return result;
    }

    // perform subtraction, but preserve zero flag if result is zero
    private int performSubtractionPZ(int r1, int r2, int carry) {
        int result = r1 - r2 - carry;

        boolean Rd7 = Arithmetic.getBit(r1, 7);
        boolean Rr7 = Arithmetic.getBit(r2, 7);
        boolean R7 = Arithmetic.getBit(result, 7);
        boolean Rd3 = Arithmetic.getBit(r1, 3);
        boolean Rr3 = Arithmetic.getBit(r2, 3);
        boolean R3 = Arithmetic.getBit(result, 3);

        // set the flags as per instruction set documentation.
        H = (!Rd3 && Rr3) || (Rr3 && R3) || (R3 && !Rd3);
        C = (!Rd7 && Rr7) || (Rr7 && R7) || (R7 && !Rd7);
        N = (result & 0x080) != 0;
        Z = (result & 0xff) == 0 ? Z : false;
        V = (Rd7 && !Rr7 && !R7) || (!Rd7 && Rr7 && R7);
        S = xor(N, V);

        return result;
    }

    private int performLeftShift(int r1, int lowbit) {
        int result = r1 << 1 | lowbit;

        H = (result & 0x010) != 0;
        C = (result & 0x100) != 0;
        N = (result & 0x080) != 0;
        Z = (result & 0xff) == 0;
        V = xor(N, C);
        S = xor(N, V);

        return result;
    }

    private int performRightShift(int r1, boolean highbit) {
        int result = ((r1 & 0xff) >> 1) | (highbit ? 0x80 : 0);

        C = (r1 & 0x01) != 0;
        N = highbit;
        Z = (result & 0xff) == 0;
        V = xor(N, C);
        S = xor(N, V);

        return result;
    }

    private int performOr(int r1, int r2) {
        int result = r1 | r2;

        N = (result & 0x080) != 0;
        Z = (result & 0xff) == 0;
        V = false;
        S = xor(N, V);

        return result;
    }

    private int performAnd(int r1, int r2) {
        int result = r1 & r2;

        N = (result & 0x080) != 0;
        Z = (result & 0xff) == 0;
        V = false;
        S = xor(N, V);

        return result;
    }

    protected void insertProbe(Simulator.Probe p, int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if (pi != null)
            pi.add(p);
        else {
            pi = new ProbedInstr(impression.readInstr(addr), addr, p);
            impression.writeInstr(pi, addr);
        }
    }

    protected void removeProbe(Simulator.Probe p, int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if (pi != null) {
            pi.remove(p);
            if (pi.isEmpty())
                impression.writeInstr(pi.instr, pi.address);
        }
    }

    protected void insertBreakPoint(int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if (pi != null)
            pi.setBreakPoint();
        else {
            pi = new ProbedInstr(impression.readInstr(addr), addr, null);
            impression.writeInstr(pi, addr);
            pi.setBreakPoint();
        }
    }

    protected void removeBreakPoint(int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if (pi != null) pi.unsetBreakPoint();
    }

    private ProbedInstr getProbedInstr(int addr) {
        Instr i = impression.readInstr(addr);
        if (i instanceof ProbedInstr)
            return ((ProbedInstr) i);
        else
            return null;
    }


}
