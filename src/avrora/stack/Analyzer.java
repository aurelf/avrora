package avrora.stack;

import avrora.core.Program;
import avrora.core.InstrVisitor;
import avrora.core.Instr;
import avrora.core.Register;

import java.util.HashSet;

/**
 * The <code>Analyzer</code> class implements the analysis phase that determines
 * the transition relation between the states in the abstract state space. It is
 * modelled on the simulator, but only does abstract interpretation rather than
 * executing the entire program.
 *
 * @author Ben L. Titzer
 */
public class Analyzer implements InstrVisitor {

    protected final Program program;
    protected final StateSpace space;
    AbstractState oldState;
    AbstractState state;
    StateCollection unexploredStates;

    protected static class StateCollection {

        Link head;
        Link free;

        protected static class Link {
            AbstractState state;
            Link next;
        }

        protected void add(AbstractState s) {
            Link link;

            if (free != null) {
                link = free;
                free = free.next;
            } else {
                link = new Link();
            }

            link.state = s;
            link.next = head;
            head = link;
        }

        protected boolean isEmpty() {
            return head != null;
        }

        protected AbstractState choose() {
            if (head == null) return null;
            Link newhead = head.next;
            head.next = free;
            free = head;
            head = newhead;
            return free.state;
        }
    }

    public Analyzer(Program p) {
        program = p;
        space = new StateSpace();
        unexploredStates = new StateCollection();
    }

    public void run() {
        pushState(new AbstractState());

        while (!unexploredStates.isEmpty()) {
            oldState = unexploredStates.choose();
            state = oldState.copy();

            int pc = state.getPC();
            Instr i = program.readInstr(pc);
            state.setPC(pc + i.getSize());
            i.accept(this);
        }
    }

    /**
     *  V I S I T O R   M E T H O D S
     * ------------------------------------------------------------
     *
     *  These visit methods implement the analysis of individual
     *  instructions for building the reachable state space of the
     *  program.
     *
     */

    public void visit(Instr.ADC i) { // add register to register with carry
        char r1 = state.readRegister(i.r1);
        char r2 = state.readRegister(i.r2);
        char result = performAddition(r1, r2, state.getFlag_C());
    }

    public void visit(Instr.ADD i) { // add register to register
    }

    public void visit(Instr.ADIW i) {// add immediate to word register
    }

    public void visit(Instr.AND i) {// and register with register
        char r1 = state.readRegister(i.r1);
        char r2 = state.readRegister(i.r2);
        char result = performAnd(r1, r2);
        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.ANDI i) {// and register with immediate
        char r1 = state.readRegister(i.r1);
        char r2 = AbstractArithmetic.knownVal((byte)i.imm1);
        char result = performAnd(r1, r2);
        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.ASR i) {// arithmetic shift right
        char val = state.readRegister(i.r1);
        char result = performRightShift(val, AbstractArithmetic.getBit(val, 7));
        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.BCLR i) {// clear bit in status register
        state.setSREG_bit(i.imm1, AbstractArithmetic.OFF);
    }

    public void visit(Instr.BLD i) {// load bit from T flag into register
        char T = state.getFlag_T();
        char val = state.readRegister(i.r1);
        char result = AbstractArithmetic.setBit(val, i.imm1, T);
        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.BRBC i) {// branch if bit in status register is clear
        char val = state.readSREG();
        char bit = AbstractArithmetic.getBit(val, i.imm1);
        branchOnCondition(not(bit), i.imm2);
    }

    public void visit(Instr.BRBS i) {// branch if bit in status register is set
        char val = state.readSREG();
        char bit = AbstractArithmetic.getBit(val, i.imm1);
        branchOnCondition(bit, i.imm2);
    }

    public void visit(Instr.BRCC i) {// branch if carry flag is clear
        char cond = state.getFlag_C();
        branchOnCondition(not(cond), i.imm1);
    }

    public void visit(Instr.BRCS i) { // branch if carry flag is set
        char cond = state.getFlag_C();
        branchOnCondition(cond, i.imm1);
    }

    public void visit(Instr.BREAK i) {// break
    }

    public void visit(Instr.BREQ i) {// branch if equal
        branchOnCondition(state.getFlag_Z(), i.imm1);
    }

    public void visit(Instr.BRGE i) {// branch if greater or equal (signed)
        branchOnCondition(not(xor(state.getFlag_N(), state.getFlag_V())), i.imm1);
    }

    public void visit(Instr.BRHC i) {// branch if H flag is clear
        branchOnCondition(not(state.getFlag_H()), i.imm1);
    }

    public void visit(Instr.BRHS i) {// branch if H flag is set
        branchOnCondition(state.getFlag_H(), i.imm1);
    }

    public void visit(Instr.BRID i) {// branch if interrupts are disabled
        branchOnCondition(not(state.getFlag_I()), i.imm1);
    }

    public void visit(Instr.BRIE i) {// branch if interrupts are enabled
        branchOnCondition(state.getFlag_I(), i.imm1);
    }

    public void visit(Instr.BRLO i) { // branch if lower
        branchOnCondition(state.getFlag_C(), i.imm1);
    }

    public void visit(Instr.BRLT i) { // branch if less than zero (signed)
        branchOnCondition(xor(state.getFlag_N(), state.getFlag_V()), i.imm1);
    }

    public void visit(Instr.BRMI i) { // branch if minus
        branchOnCondition(state.getFlag_N(), i.imm1);
    }

    public void visit(Instr.BRNE i) { // branch if not equal
        branchOnCondition(state.getFlag_Z(), i.imm1);
    }

    public void visit(Instr.BRPL i) { // branch if positive
        branchOnCondition(not(state.getFlag_N()), i.imm1);
    }

    public void visit(Instr.BRSH i) { // branch if same or higher
        branchOnCondition(not(state.getFlag_C()), i.imm1);
    }

    public void visit(Instr.BRTC i) { // branch if T flag is clear
        branchOnCondition(not(state.getFlag_T()), i.imm1);
    }

    public void visit(Instr.BRTS i) { // branch if T flag is set
        branchOnCondition(state.getFlag_T(), i.imm1);
    }

    public void visit(Instr.BRVC i) { // branch if V flag is clear
        branchOnCondition(not(state.getFlag_V()), i.imm1);
    }

    public void visit(Instr.BRVS i) { // branch if V flag is set
        branchOnCondition(state.getFlag_V(), i.imm1);
    }

    public void visit(Instr.BSET i) { // set flag in status register
        state.setSREG_bit(i.imm1, AbstractArithmetic.ON);
    }

    public void visit(Instr.BST i) { // store bit in register into T flag
        char val = state.readRegister(i.r1);
        char T = AbstractArithmetic.getBit(val, i.imm1);
        state.setFlag_T(T);
    }

    public void visit(Instr.CALL i) { // call absolute address
    }

    public void visit(Instr.CBI i) { // clear bit in IO register
        char val = state.readIORegister(i.imm1);
        char result = AbstractArithmetic.setBit(val, i.imm2, AbstractArithmetic.OFF);
        state.writeIORegister(i.imm1, result);
    }

    public void visit(Instr.CBR i) { // clear bits in register
        char r1 = state.readRegister(i.r1);
        char r2 = AbstractArithmetic.knownVal((byte)~i.imm1);
        char result = performAnd(r1, r2);
        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.CLC i) { // clear C flag
        state.setFlag_C(AbstractArithmetic.OFF);
    }

    public void visit(Instr.CLH i) { // clear H flag
        state.setFlag_H(AbstractArithmetic.OFF);
    }

    public void visit(Instr.CLI i) { // clear I flag
        state.setFlag_I(AbstractArithmetic.OFF);
    }

    public void visit(Instr.CLN i) { // clear N flag
        state.setFlag_N(AbstractArithmetic.OFF);
    }

    public void visit(Instr.CLR i) { // clear register (set to zero)
        state.setFlag_S(AbstractArithmetic.OFF);
        state.setFlag_V(AbstractArithmetic.OFF);
        state.setFlag_N(AbstractArithmetic.OFF);
        state.setFlag_Z(AbstractArithmetic.ON);
        state.writeRegister(i.r1, AbstractArithmetic.ZERO);
    }

    public void visit(Instr.CLS i) { // clear S flag
        state.setFlag_S(AbstractArithmetic.OFF);
    }

    public void visit(Instr.CLT i) { // clear T flag
        state.setFlag_T(AbstractArithmetic.OFF);
    }

    public void visit(Instr.CLV i) { // clear V flag
        state.setFlag_V(AbstractArithmetic.OFF);
    }

    public void visit(Instr.CLZ i) { // clear Z flag
        state.setFlag_Z(AbstractArithmetic.OFF);
    }

    public void visit(Instr.COM i) { // one's compliment register
        // TODO: implement me
    }

    public void visit(Instr.CP i) { // compare registers
        // TODO: implement me
    }

    // TODO: implement me
    public void visit(Instr.CPC i) { // compare registers with carry
    }

    public void visit(Instr.CPI i) { // compare register with immediate
        // TODO: implement me
    }

    public void visit(Instr.CPSE i) { // compare registers and skip if equal
        // TODO: implement me
    }

    public void visit(Instr.DEC i) { // decrement register by one
        // TODO: implement me
    }

    public void visit(Instr.EICALL i) { // extended indirect call
        // TODO: implement me
    }

    public void visit(Instr.EIJMP i) { // extended indirect jump
        // TODO: implement me
    }

    public void visit(Instr.ELPM i) { // extended load program memory to r0
        // TODO: implement me
    }

    public void visit(Instr.ELPMD i) { // extended load program memory to register
        // TODO: implement me
    }

    public void visit(Instr.ELPMPI i) { // extended load program memory to register and post-increment
        // TODO: implement me
    }

    public void visit(Instr.EOR i) { // exclusive or register with register
        char result;

        if (i.r1 == i.r2) { // recognize A ^ A = A
            result = AbstractArithmetic.ZERO;
        } else {
            char r1 = state.readRegister(i.r1);
            char r2 = state.readRegister(i.r2);
            result = xor(r1, r2);
        }

        char N = AbstractArithmetic.getBit(result, 7);
        char Z = AbstractArithmetic.couldBeZero(result);
        char V = AbstractArithmetic.OFF;
        char S = xor(N, V);
        setFlag_NZVS(N, Z, V, S);

        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.FMUL i) { // fractional multiply register with register to r0
        // TODO: implement me
    }

    public void visit(Instr.FMULS i) { // signed fractional multiply register with register to r0
        // TODO: implement me
    }

    public void visit(Instr.FMULSU i) { // signed/unsigned fractional multiply register with register to r0
        // TODO: implement me
    }

    public void visit(Instr.ICALL i) { // indirect call through Z register
        // TODO: implement me
    }

    public void visit(Instr.IJMP i) { // indirect jump through Z register
        // TODO: implement me
    }

    public void visit(Instr.IN i) { // read from IO register into register
        char val = state.readIORegister(i.imm1);
        state.writeRegister(i.r1, val);
    }

    public void visit(Instr.INC i) { // increment register by one
        // TODO: implement me
    }

    public void visit(Instr.JMP i) { // absolute jump
        state.setPC(absolute(i.imm1));
    }

    public void visit(Instr.LD i) { // load from SRAM
        // TODO: implement me
    }

    public void visit(Instr.LDD i) { // load from SRAM with displacement
        // TODO: implement me
    }

    public void visit(Instr.LDI i) { // load immediate into register
        state.writeRegister(i.r1, AbstractArithmetic.knownVal((byte) i.imm1));
    }

    public void visit(Instr.LDPD i) { // load from SRAM with pre-decrement
        // TODO: implement me
    }

    public void visit(Instr.LDPI i) { // load from SRAM with post-increment
        // TODO: implement me
    }

    public void visit(Instr.LDS i) { // load direct from SRAM
        // TODO: implement me
    }

    public void visit(Instr.LPM i) { // load program memory into r0
        // TODO: implement me
    }

    public void visit(Instr.LPMD i) { // load program memory into register
        // TODO: implement me
    }

    public void visit(Instr.LPMPI i) { // load program memory into register and post-increment
        // TODO: implement me
    }

    public void visit(Instr.LSL i) { // logical shift left
        char val = state.readRegister(i.r1);
        char result = performLeftShift(val, AbstractArithmetic.OFF);
        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.LSR i) { // logical shift right
        char val = state.readRegister(i.r1);
        char result = performRightShift(val, AbstractArithmetic.OFF);
        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.MOV i) { // copy register to register
        char result = state.readRegister(i.r2);
        state.writeRegister(i.r1, result);
        pushState(state);
    }

    public void visit(Instr.MOVW i) { // copy two registers to two registers
        Register src = i.r2;
        Register dst = i.r1;

        char vall = state.readRegister(src);
        char valh = state.readRegister(src.nextRegister());

        state.writeRegister(dst, vall);
        state.writeRegister(dst.nextRegister(), valh);
    }

    public void visit(Instr.MUL i) { // multiply register with register to r0
        // TODO: implement me
    }

    public void visit(Instr.MULS i) { // signed multiply register with register to r0
        // TODO: implement me
    }

    public void visit(Instr.MULSU i) { // signed/unsigned multiply register with register to r0
        // TODO: implement me
    }

    public void visit(Instr.NEG i) { // two's complement register
        // TODO: implement me
    }

    public void visit(Instr.NOP i) { // do nothing operation
        // TODO: implement me
    }

    public void visit(Instr.OR i) { // or register with register
        char r1 = state.readRegister(i.r1);
        char r2 = state.readRegister(i.r2);
        char result = performOr(r1, r2);
        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.ORI i) { // or register with immediate
        char r1 = state.readRegister(i.r1);
        char r2 = AbstractArithmetic.knownVal((byte)i.imm1);
        char result = performOr(r1, r2);
        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.OUT i) { // write from register to IO register
        char val = state.readRegister(i.r1);
        state.writeIORegister(i.imm1, val);
    }

    public void visit(Instr.POP i) { // pop from the stack to register
        // TODO: implement me
    }

    public void visit(Instr.PUSH i) { // push register to the stack
        // TODO: implement me
    }

    public void visit(Instr.RCALL i) { // relative call
        // TODO: implement me
    }

    public void visit(Instr.RET i) { // return to caller
        // TODO: implement me
    }

    public void visit(Instr.RETI i) { // return from interrupt
        // TODO: implement me
    }

    public void visit(Instr.RJMP i) { // relative jump
        state.setPC(relative(i.imm1));
    }

    public void visit(Instr.ROL i) { // rotate left through carry flag
        char val = state.readRegister(i.r1);
        char result = performLeftShift(val, state.getFlag_C());
        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.ROR i) { // rotate right through carry flag
        char val = state.readRegister(i.r1);
        char result = performRightShift(val, state.getFlag_C());
        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.SBC i) { // subtract register from register with carry
        // TODO: implement me
    }

    public void visit(Instr.SBCI i) { // subtract immediate from register with carry
        // TODO: implement me
    }

    public void visit(Instr.SBI i) { // set bit in IO register
        // TODO: implement me
    }

    public void visit(Instr.SBIC i) { // skip if bit in IO register is clear
        char reg = state.readIORegister(i.imm1);
        char bit = AbstractArithmetic.getBit(reg, i.imm2);
        skipOnCondition(not(bit));
    }

    public void visit(Instr.SBIS i) { // skip if bit in IO register is set
        char reg = state.readIORegister(i.imm1);
        char bit = AbstractArithmetic.getBit(reg, i.imm2);
        skipOnCondition(bit);
    }

    public void visit(Instr.SBIW i) { // subtract immediate from word
        // TODO: implement me
    }

    public void visit(Instr.SBR i) { // set bits in register
        char r1 = state.readRegister(i.r1);
        char r2 = AbstractArithmetic.knownVal((byte)i.imm1);
        char result = performOr(r1, r2);
        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.SBRC i) { // skip if bit in register cleared
        char bit = AbstractArithmetic.getBit(state.readRegister(i.r1), i.imm1);
        skipOnCondition(not(bit));
    }

    public void visit(Instr.SBRS i) { // skip if bit in register set
        char bit = AbstractArithmetic.getBit(state.readRegister(i.r1), i.imm1);
        skipOnCondition(bit);
    }

    public void visit(Instr.SEC i) { // set C (carry) flag
        state.setFlag_C(AbstractArithmetic.ON);
    }

    public void visit(Instr.SEH i) { // set H (half carry) flag
        state.setFlag_H(AbstractArithmetic.ON);
    }

    public void visit(Instr.SEI i) { // set I (interrupt enable) flag
        state.setFlag_I(AbstractArithmetic.ON);
    }

    public void visit(Instr.SEN i) { // set N (negative) flag
        state.setFlag_N(AbstractArithmetic.ON);
    }

    public void visit(Instr.SER i) { // set bits in register
        state.writeRegister(i.r1, AbstractArithmetic.knownVal((byte) 0xff));
    }

    public void visit(Instr.SES i) { // set S (signed) flag
        state.setFlag_S(AbstractArithmetic.ON);
    }

    public void visit(Instr.SET i) { // set T flag
        state.setFlag_T(AbstractArithmetic.ON);
    }

    public void visit(Instr.SEV i) { // set V (overflow) flag
        state.setFlag_V(AbstractArithmetic.ON);
    }

    public void visit(Instr.SEZ i) { // set Z (zero) flag
        state.setFlag_Z(AbstractArithmetic.ON);
    }

    public void visit(Instr.SLEEP i) { // enter sleep mode
    }

    public void visit(Instr.SPM i) { // store to program memory from r0
        // TODO: implement me
    }

    public void visit(Instr.ST i) { // store from register to SRAM
        // TODO: implement me
    }

    public void visit(Instr.STD i) { // store from register to SRAM with displacement
        // TODO: implement me
    }

    public void visit(Instr.STPD i) { // store from register to SRAM with pre-decrement
        // TODO: implement me
    }

    public void visit(Instr.STPI i) { // store from register to SRAM with post-increment
        // TODO: implement me
    }

    public void visit(Instr.STS i) { // store direct to SRAM
        // TODO: implement me
    }

    public void visit(Instr.SUB i) { // subtract register from register
        // TODO: implement me
    }

    public void visit(Instr.SUBI i) { // subtract immediate from register
        // TODO: implement me
    }

    public void visit(Instr.SWAP i) { // swap nibbles in register
        char result = state.readRegister(i.r1);
        int high = ((result & 0xF0F0) >> 4);
        int low = ((result & 0x0F0F) << 4);
        result = (char) (low | high);
        state.writeRegister(i.r1, result);
    }

    public void visit(Instr.TST i) { // compare registers
        // TODO: implement me
    }

    public void visit(Instr.WDR i) { // watchdog timer reset
    }


    /**
     *
     *  U T I L I T I E S
     * ----------------------------------------------------------------------
     *
     *  These are some utility functions to help with implementing the
     * transfer functions.
     *
     */


    protected void pushState(AbstractState s) {
        if (space.contains(s)) return;
        space.addState(s);
        unexploredStates.add(s);
    }

    private void branchOnCondition(char cond, int offset) {
        if ( cond == AbstractArithmetic.ON ) // branch is taken
            relativeBranch(offset);
        else if ( cond == AbstractArithmetic.OFF ) ; // branch is not taken
        else { // branch could go either way
            AbstractState nottaken = state.copy();
            pushState(nottaken);
            relativeBranch(offset);
        }
    }

    private void skipOnCondition(char cond) {
        branchOnCondition(cond, program.readInstr(state.getPC()).getSize());
    }

    private void relativeBranch(int offset) {
        state.setPC(relative(offset));
    }

    private void setFlag_HCNZVS(char H, char C, char N, char Z, char V, char S) {
        state.setFlag_H(H);
        state.setFlag_C(C);
        state.setFlag_N(N);
        state.setFlag_Z(Z);
        state.setFlag_V(V);
        state.setFlag_S(S);
    }

    private void setFlag_CNZVS(char C, char N, char Z, char V, char S) {
        state.setFlag_C(C);
        state.setFlag_N(N);
        state.setFlag_Z(Z);
        state.setFlag_V(V);
        state.setFlag_S(S);
    }

    private void setFlag_NZVS(char N, char Z, char V, char S) {
        state.setFlag_N(N);
        state.setFlag_Z(Z);
        state.setFlag_V(V);
        state.setFlag_S(S);
    }

    private char performAddition(char r1, char r2, char carry) {
        int resultA = r1 + r2 + carry;
        if ( carry != AbstractArithmetic.ZERO ) resultA = r2;
        int ral = r1 & 0xf;
        int rbl = r2 & 0xf;

        char Rd7 = AbstractArithmetic.getBit(r1, 7);
        char Rr7 = AbstractArithmetic.getBit(r2, 7);
        char R7 = AbstractArithmetic.getBit(result, 7);

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

    private char performRightShift(char val, char highbit) {
        char result = (char)(((val & 0xfefe) >> 1) | (highbit));

        char C = AbstractArithmetic.getBit(val, 1);
        char N = highbit;
        char Z = AbstractArithmetic.couldBeZero(result);
        char V = xor(N, C);
        char S = xor(N, V);
        setFlag_CNZVS(C, N, Z, V, S);
        return result;
    }



    private char performLeftShift(char val, char lowbit) {
        char result = (char)(((val & 0x7f7f) << 1) | lowbit);

        char H = AbstractArithmetic.getBit(result, 3);
        char C = AbstractArithmetic.getBit(val, 7);
        char N = AbstractArithmetic.getBit(result, 7);
        char Z = AbstractArithmetic.couldBeZero(result);
        char V = xor(N, C);
        char S = xor(N, V);
        setFlag_HCNZVS(H, C, N, Z, V, S);
        return result;

    }

    private char performOr(char r1, char r2) {
        char result = AbstractArithmetic.logicalOr(r1, r2);

        char N = AbstractArithmetic.getBit(result, 7);
        char Z = AbstractArithmetic.couldBeZero(result);
        char V = AbstractArithmetic.OFF;
        char S = xor(N, V);
        setFlag_NZVS(N, Z, V, S);

        return result;
    }

    private char performAnd(char r1, char r2) {
        char result = AbstractArithmetic.logicalAnd(r1, r2);

        char N = AbstractArithmetic.getBit(result, 7);
        char Z = AbstractArithmetic.couldBeZero(result);
        char V = AbstractArithmetic.OFF;
        char S = xor(N, V);
        setFlag_NZVS(N, Z, V, S);

        return result;
    }

    private char xor(char v1, char v2) {
        char mask = AbstractArithmetic.commonMask(v1, v2);
        return AbstractArithmetic.canon(mask, (char)(v1 ^ v2));
    }

    private char not(char v1) {
        return (char)(v1 ^ 0x01);
    }

    private int relative(int imm1) {
        return 2 + 2 * imm1 + state.getPC();
    }

    private int absolute(int imm1) {
        return 2 * imm1;
    }


}
