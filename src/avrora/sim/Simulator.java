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
import avrora.core.Operand;
import avrora.core.*;
import avrora.sim.util.MulticastProbe;
import avrora.sim.util.PeriodicTrigger;
import avrora.sim.util.DeltaQueue;
import avrora.sim.mcu.ATMega128L;
import avrora.sim.mcu.Microcontroller;
import avrora.util.StringUtil;
import avrora.util.Terminal;
import avrora.util.Verbose;

/**
 * The <code>Simulator</code> class implements a full processor simulator
 * for the AVR instruction set. It is the base class of specific implementations
 * that implement processor-specific behavior.
 *
 * @author Ben L. Titzer
 */
public abstract class Simulator implements InstrVisitor, IORegisterConstants {

    Verbose.Printer eventPrinter = Verbose.getVerbosePrinter("sim.event");
    Verbose.Printer interruptPrinter = Verbose.getVerbosePrinter("sim.interrupt");

    /**
     * The <code>TRACEPROBE</code> field represents a simple probe
     * that prints an instruction to the terminal as it is encountered.
     * This is useful for tracing program execution over simulation.
     */
    public static final Probe TRACEPROBE = new Probe() {
        public void fireBefore(Instr i, int pc, State s) {
            Terminal.printBrightCyan(StringUtil.toHex(pc, 4) + ": ");
            Terminal.printBrightBlue(i.getVariant() + " ");
            Terminal.print(i.getOperands());
            Terminal.nextln();
        }

        public void fireAfter(Instr i, int pc, State s) {
            // do nothing.
        }
    };

    /**
     * The <code>program</code> field allows descendants of the <code>Simulator</code>
     * class to access the program that is currently loaded in the simulator.
     */
    protected final Program program;

    protected final Microcontroller microcontroller;

    /**
     * The <code>state</code> field stores a reference to the <code>State</code>
     * object that represents the state of the processor in the simulation. It is
     * protected to allow subclasses access.
     */
    protected State state;

    /**
     * The <code>activeProbe</code> field stores a reference to a
     * <code>MulticastProbe</code> that contains all of the probes to be fired
     * before and after the main execution runLoop--i.e. before and after
     * every instruction.
     */
    protected final MulticastProbe activeProbe;

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

    /**
     * The <code>nextPC</code> field is used internally in maintaining the correct
     * execution order of the instructions.
     */
    protected int nextPC;

    protected int cyclesConsumed;

    /**
     * The <code>interrupts</code> array stores a reference to an <code>Interrupt</code>
     * instance for each of the interrupt vectors supported in the simulator.
     */
    protected Interrupt[] interrupts;

    /**
     * The <code>eventQueue</code> field stores a reference to the event queue,
     * a delta list of all events to be processed in order.
     */
    protected DeltaQueue eventQueue;

    /**
     * The <code>MAX_INTERRUPTS</code> fields stores the maximum number of
     * interrupt vectors supported by the simulator.
     */
    public static int MAX_INTERRUPTS = 35;

    /**
     * The constructor creates the internal data structures and initial
     * state of the processor. It constructs an instance of the simulator
     * that is ready to have devices attached, IO registers probed, and
     * probes and events inserted. Users should not create
     * <code>Simulator</code> instances directly, but instead should
     * get an instance of the appropriate processor and load the
     * program into it.
     *
     * @param p the program to load into the simulator
     */
    public Simulator(Microcontroller mcu, Program p) {
        microcontroller = mcu;
        program = p;
        interrupts = new Interrupt[MAX_INTERRUPTS];
        activeProbe = new MulticastProbe();

        // set all interrupts to ignore
        for (int cntr = 0; cntr < MAX_INTERRUPTS; cntr++)
            interrupts[cntr] = IGNORE;

        // reset the state of the simulation
        reset();
    }

    /**
     * The <code>Simulator.Probe</code> interface represents a
     * programmer-defined probe that can be inserted at particular instructions
     * or at every instruction. This probe allows execution of client code
     * for profiling, analysis, or program understanding. A method that
     * is invoked before an instruction is executed and a method that is
     * invoked after the instruction is executed are provided, although
     * most probes will use only one of these methods.
     *
     * @author Ben L. Titzer
     */
    public interface Probe {

        /**
         * The <code>fireBefore()</code> method is called before the probed instruction
         * executes.
         *
         * @param i       the instruction being probed
         * @param address the address at which this instruction resides
         * @param state   the state of the simulation
         */
        public void fireBefore(Instr i, int address, State state);

        /**
         * The <code>fireAfter()</code> method is called after the probed instruction
         * executes.
         *
         * @param i       the instruction being probed
         * @param address the address at which this instruction resides
         * @param state   the state of the simulation
         */
        public void fireAfter(Instr i, int address, State state);
    }

    /**
     * The <code>Simulator.Trigger</code> interface represents a trigger that is
     * fired when a timed event occurs within the simulator. Users of the simulator
     * can insert timed events that model environmental factors, implement timeouts,
     * timers, or any other type of functionality that is simulation-time dependent.
     */
    public interface Trigger {
        /**
         * The <code>fire()</code> method is called when the event to which it is
         * tied happens with in the simulator.
         */
        public void fire();
    }

    public interface MemoryProbe {

        public void fireBeforeRead(Instr i, int address, State state, byte value);

        public void fireBeforeWrite(Instr i, int address, State state, byte value);

        public void fireAfterRead(Instr i, int address, State state, byte value);

        public void fireAfterWrite(Instr i, int address, State state, byte value);
    }

    /**
     * The <code>BreakPointException</code> is an exception that is thrown
     * by the simulator before it executes an instruction which has a breakpoint.
     * When this exception is thrown within the simulator, the simulator
     * is left in a state where it is ready to be resumed where it left off
     * by the <code>start()</code> method. When resuming, the breakpointed
     * instruction will not cause a second <code>BreakPointException</code>
     * until the the instruction is executed a second time.
     *
     * @author Ben L. Titzer
     */
    public static class BreakPointException extends RuntimeException {
        /**
         * The <code>instr</code> field stores the instruction that
         * caused the breakpoint.
         */
        public final Instr instr;

        /**
         * The <code>address</code> field stores the address of the
         * instruction that caused the breakpoint.
         */
        public final int address;

        /**
         * The <code>state</code> field stores a reference to the state
         * of the simulator when the breakpoint occurred, before executing
         * the instruction.
         */
        public final State state;

        BreakPointException(Instr i, int a, State s) {
            super("breakpoint @ " + StringUtil.toHex(a, 4) + " reached");
            instr = i;
            address = a;
            state = s;
        }
    }

    /**
     * The <code>TimeoutException</code> is thrown by the simulator when a timeout
     * reaches zero. Timeouts can be used to ensure termination of the simulator
     * during testing, and implementing timestepping in surrounding tools such
     * as interactive debuggers or visualizers.
     * <p/>
     * When the exception is thrown, the simulator is left in a state that is
     * safe to be resumed by a <code>start()</code> call.
     *
     * @author Ben L. Titzer
     */
    public static class TimeoutException extends RuntimeException {

        /**
         * The <code>instr</code> field stores the next instruction to be
         * executed after the timeout.
         */
        public final Instr instr;

        /**
         * The <code>address</code> field stores the address of the next
         * instruction to be executed after the timeout.
         */
        public final int address;

        /**
         * The <code>state</code> field stores the state of the simulation
         * at the point at which the timeout occurred.
         */
        public final State state;

        /**
         * The <code>timeout</code> field stores the value (in clock cycles)
         * of the timeout that occurred.
         */
        public final long timeout;

        TimeoutException(Instr i, int a, State s, long t) {
            super("timeout @ " + StringUtil.toHex(a, 4) + " reached after " + t + " instructions");
            instr = i;
            address = a;
            state = s;
            timeout = t;
        }
    }

    /**
     * The ProbedInstr class represents a wrapper around an instruction in
     * the program that executes the probes before executing the instruction
     * and after the instruction. For most methods on the <code>Instr</code>
     * class, it simply forwards the call to the original instruction.
     */
    class ProbedInstr extends Instr {
        private final int address;
        private final Instr instr;
        private final MulticastProbe probe;

        private boolean breakPoint;
        private boolean breakFired;

        ProbedInstr(Instr i, int a, Probe p) {
            instr = i;
            address = a;
            probe = new MulticastProbe();
            probe.add(p);
        }

        void add(Probe p) {
            probe.add(p);
        }

        void remove(Probe p) {
            probe.remove(p);
        }

        void setBreakPoint() {
            if (!breakPoint) breakFired = false;
            breakPoint = true;
        }

        void unsetBreakPoint() {
            breakPoint = false;
            breakFired = false;
        }

        boolean isEmpty() {
            return probe.isEmpty();
        }

        public int getCycles() {
            // This is an extremely subtle hack to keep timings correct.
            // A ProbedInstr causes the execute(Instr i) method to be called
            // twice, once for the ProbedInstr instance, and once for the
            // actual instruction. Since execute() advances the cycle count,
            // don't allow it to advance the cycle count twice for this
            // instruction.
            return 0;
        }

        public void accept(InstrVisitor v) {

            // if the simulator is visiting us, execute the instruction instead of accept(v).
            if (v == Simulator.this) {
                // breakpoint processing.
                if (breakPoint) {
                    if (!breakFired) {
                        breakFired = true;
                        throw new BreakPointException(instr, address, state);
                    } else
                        breakFired = false;
                }

                // fire the probe(s) before
                probe.fireBefore(instr, address, state);

                // execute actual instruction
                execute(instr);

                // fire the probe(s) after
                probe.fireAfter(instr, address, state);
            } else {
                instr.accept(v);
            }
        }

        public int getSize() {
            return instr.getSize();
        }

        public Instr build(int address, Operand[] ops) {
            return instr.build(address, ops);
        }

        public String getOperands() {
            return instr.getOperands();
        }

        public String getName() {
            return instr.getName();
        }

        public String getVariant() {
            return instr.getVariant();
        }
    }

    /**
     * The <code>Interrupt</code> interface represents the behavior of an interrupt
     * (how it manipulates the state of the processor) when it is posted and
     * when it is triggered (handler is executed by the processor). For example,
     * an external interrupt, when posted, sets a bit in an IO register, and if
     * the interrupt is not masked, will add it to the pending interrupts on
     * the processor. When the interrupt head, it remains flagged (the bit
     * in the IO register remains on). Some interrupts clear bits in IO registers
     * on triggered (e.g. timer interrupts). This interface allows both of these
     * behaviors to be implemented.
     *
     * @author Ben L. Titzer
     */
    public interface Interrupt {
        public void force();

        public void fire();
    }


    public class MaskableInterrupt implements Interrupt {
        protected final int interruptNumber;

        protected final MaskRegister maskRegister;
        protected final FlagRegister flagRegister;
        protected final int bit;

        protected final boolean sticky;

        public MaskableInterrupt(int num, MaskRegister mr, FlagRegister fr, int b, boolean e) {
            interruptNumber = num;
            maskRegister = mr;
            flagRegister = fr;
            bit = b;
            sticky = e;
        }

        public void force() {
            // flagging the bit will cause the interrupt to be posted if it is not masked
            flagRegister.flagBit(bit);
        }

        public void fire() {
            if (!sticky) {
                // setting the flag register bit will actually clear and unpost the interrupt
                flagRegister.setBit(bit);
            }
        }
    }

    /**
     * The <code>IGNORE</code> field stores a reference to a singleton
     * anonymous class that ignores posting and firing of an interrupt. This
     * is the default value for interrupts in a freshly initialized
     * <code>Simulator</code> instance.
     */
    public static final Interrupt IGNORE = new Interrupt() {
        public void force() {
        }

        public void fire() {
        }
    };

    public Microcontroller getMicrocontroller() {
        return microcontroller;
    }


    /**
     * The <code>getState()</code> retrieves a reference to the current
     * state of the simulation, including the values of all registers, the
     * SRAM, the IO register, the program memory, program counter, etc.
     * This state is mutable.
     *
     * @return a reference to the current state of the simulation
     */
    public State getState() {
        return state;
    }

    /**
     * The <code>start()</code> method begins the simulation. It causes the
     * simulator to enter a runLoop that executes instructions, firing probes
     * and triggers as it executes. The <code>start()</code> method returns
     * normally when the </code>break</code> AVR instruction is executed,
     * when a <code>BreakPointException</code> is thrown, when a <code>
     * TimeoutException</code> is thrown, or when the <code>stop()</code>
     * method on this simulator instance is called.
     */
    public void start() {
        shouldRun = true;
        runLoop();
    }

    /**
     * The <code>stop()</code> method stops the simulation if it is running.
     * This method can be called from within a probe or trigger or from another
     * thread.
     */
    public void stop() {
        shouldRun = false;
    }

    /**
     * The <code>reset()</code> method stops the simulation and resets its
     * state to the default initial state. Probes inserted in the program
     * are retained. All triggers are removed.
     */
    public void reset() {
        state = constructState();
        eventQueue = new DeltaQueue();
        justReturnedFromInterrupt = false;
    }

    protected State constructState() {
        return new State(program,
                microcontroller.getFlashSize(),
                microcontroller.getIORegSize(), 
                microcontroller.getRamSize());
    }

    private void runLoop() {

        nextPC = state.getPC();
        cyclesConsumed = 0;

        while (shouldRun) {

            if (justReturnedFromInterrupt) {
                // don't process the interrupt if we just returned from
                // an interrupt handler, because the hardware manual says
                // that at least one instruction is executed after
                // returning from an interrupt.
                justReturnedFromInterrupt = false;
            } else if ( state.getFlag_I() ) {
                long interruptPostMask = state.getPostedInterrupts();

                // check if there are any pending (posted) interrupts
                if (interruptPostMask != 0) {
                    // the lowest set bit is the highest priority posted interrupt
                    int lowestbit = Arithmetic.lowestBit(interruptPostMask);

                    // fire the interrupt (update flag register(s) state)
                    triggerInterrupt(lowestbit);

                    // store the return address
                    pushPC(nextPC);

                    // set PC to interrupt handler
                    nextPC = getInterruptVectorAddress(lowestbit);
                    state.setPC(nextPC);

                    // disable interrupts
                    state.setFlag_I(false);

                    // process any timed events
                    advanceCycles(4);
                    sleeping = false;
                }
            }

            if ( sleeping ) {
                long delta = eventQueue.getHeadDelta();
                if ( delta <= 0 ) delta = 1;
                advanceCycles(delta);
            } else {
                // get the current instruction
                int curPC = nextPC;
                Instr i = state.getInstr(nextPC);
                nextPC = nextPC + i.getSize();

                // visit the actual instruction (or probe)
                activeProbe.fireBefore(i, curPC, state);
                execute(i);
                activeProbe.fireAfter(i, curPC, state);
            }
        }
    }

    private void advanceCycles(long delta) {
        state.consumeCycles(delta);
        eventQueue.advance(delta);
        cyclesConsumed = 0;
    }

    /**
     * The <code>getInterruptVectorAddress()</code> method computes the location in memory
     * to jump to for the given interrupt number. On the Atmega128L, the starting point is
     * the beginning of memory and each interrupt vector slot is 4 bytes. On older
     * architectures, this is not the case, therefore this method has to be implemented
     * according to the specific device being simulated.
     *
     * @param inum the interrupt number
     * @return the byte address that represents the address in the program to jump to
     *         when this interrupt is fired
     */
    protected int getInterruptVectorAddress(int inum) {
        return (inum - 1) * 4;
    }

    private void execute(Instr i) {
        i.accept(this);
        state.setPC(nextPC);
        // process any timed events and advance state clock
        advanceCycles(cyclesConsumed + i.getCycles());
    }

    /**
     * The <code>insertProbe()</code> method allows a probe to be inserted
     * that is executed before and after every instruction that is executed
     * by the simulator
     *
     * @param p the probe to insert
     */
    public void insertProbe(Probe p) {
        activeProbe.add(p);
    }

    /**
     * The <code>insertProbe()</code> method allows a probe to be inserted
     * at a particular address in the program that corresponds to an
     * instruction. The probe is then fired before and after that particular
     * instruction is executed.
     *
     * @param p    the probe to insert
     * @param addr the address at which to insert the probe
     */
    public void insertProbe(Probe p, int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if (pi != null)
            pi.add(p);
        else {
            pi = new ProbedInstr(state.getInstr(addr), addr, p);
            state.setInstr(pi, addr);
        }
    }

    /**
     * The <code>removeProbe()</code> method removes a probe from the global
     * probe table (the probes executed before and after every instruction).
     * The comparison used is reference equality, not <code>.equals()</code>.
     *
     * @param b the probe to remove
     */
    public void removeProbe(Probe b) {
        activeProbe.remove(b);
    }

    /**
     * The <code>removeProbe()</code> method removes a probe from the
     * instruction at the specified the address. The comparison used is
     * reference equality, not <code>.equals()</code>.
     *
     * @param p    the probe to remove
     * @param addr the address from which to remove the probe
     */
    public void removeProbe(Probe p, int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if (pi != null) {
            pi.remove(p);
            if (pi.isEmpty())
                state.setInstr(pi.instr, pi.address);
        }
    }

    private ProbedInstr getProbedInstr(int addr) {
        Instr i = state.getInstr(addr);
        if (i instanceof ProbedInstr)
            return ((ProbedInstr) i);
        else
            return null;
    }

    /**
     * The <code>insertBreakPoint()</code> method inserts a breakpoint
     * at the instruction at the specified address. At most one breakpoint
     * can be inserted at a particular instruction. Subsequent calls to
     * this method would then have no effect for the same address.
     *
     * @param addr
     */
    public void insertBreakPoint(int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if (pi != null)
            pi.setBreakPoint();
        else {
            pi = new ProbedInstr(state.getInstr(addr), addr, null);
            state.setInstr(pi, addr);
            pi.setBreakPoint();
        }
    }

    /**
     * The <code>removeBreakPoint</code> method removes all breakpoints at
     * the specified instruction at the specified address.
     *
     * @param addr
     */
    public void removeBreakPoint(int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if (pi != null) pi.unsetBreakPoint();
    }

    /**
     * The <code>setWatchPoint()</code> method allows a probe to be inserted
     * at a memory location. The probe will be executed before every read
     * or write to that memory location.
     *
     * @param p         the probe to insert
     * @param data_addr the data address at which to insert the probe
     */
    public void setWatchPoint(MemoryProbe p, int data_addr) {
        // TODO: implement watchpoints
    }

    /**
     * The <code>forceInterrupt()</code> method forces the simulator to post the
     * specified interrupt regardless of the normal source of the interrupt.
     * If there is a flag register associated with the specified interrupt, then
     * the flag register's value will be set as if the original source of
     * the interrupt (e.g. a timer) had posted the interrupt. As with a normal
     * post of the interrupt, if the interrupt is masked out via a mask register
     * or the master interrupt enable bit, the interrupt will not be delivered.
     * The main reason that this interface exists is for forcing programs to
     * handle interrupts and observe their behavior.
     *
     * @param num the interrupt number to force
     */
    public void forceInterrupt(int num) {
        interruptPrinter.println("Simulator.forceInterrupt(" + num + ")");
        interrupts[num].force();
    }

    protected void triggerInterrupt(int num) {
        interruptPrinter.println("Simulator.triggerInterrupt(" + num + ")");
        interrupts[num].fire();
    }

    /**
     * The <code>addTimerEvent()</code> method inserts a trigger into the
     * event queue of the simulator with the specified delay in clock cycles.
     * The trigger will then be executed at the future time specified
     *
     * @param e      the trigger to be inserted
     * @param cycles the number of cycles in the future at which to trigger
     */
    public void addTimerEvent(Trigger e, long cycles) {
        eventPrinter.println("Simulator.addTimerEvent(" + cycles + ")");
        eventQueue.add(e, cycles);
    }

    /**
     * The <code>addPeriodicTimerEvent()</code> method inserts a trigger into
     * the event queue of the simulator with the specified period. The <code>
     * PeriodicTrigger</code> instance created will continually reinsert the
     * trigger after each firing to achieve predictable periodic behavior.
     *
     * @param e      the trigger to insert
     * @param period the period in clock cycles
     * @return the <code>PeriodicTrigger</code> instance inserted
     */
    public PeriodicTrigger addPeriodicTimerEvent(Trigger e, long period) {
        eventPrinter.println("Simulator.addPeriodicTimerEvent(" + period + ")");
        PeriodicTrigger pt = new PeriodicTrigger(this, e, period);
        eventQueue.add(pt, period);
        return pt;
    }

    /**
     * The <code>removeTimerEvent()</code> method removes a trigger from
     * the event queue of the simulator. The comparison used is reference
     * equality, not <code>.equals()</code>.
     *
     * @param e the trigger to remove
     */
    public void removeTimerEvent(Trigger e) {
        eventPrinter.println("Simulator.removeTimerEvent()");
        eventQueue.remove(e);
    }

    //
    //  V I S I T   M E T H O D S
    // ------------------------------------------------------------------
    //
    // These methods implement the InstrVisitor interface and
    // accomplish the behavior of each instruction.
    //
    //

    public void visit(Instr.ADC i) { // add two registers and carry flag
        int r1 = state.getRegisterUnsigned(i.r1);
        int r2 = state.getRegisterUnsigned(i.r2);
        int result = performAddition(r1, r2, state.getFlag_C() ? 1 : 0);
        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.ADD i) { // add second register to first
        int r1 = state.getRegisterUnsigned(i.r1);
        int r2 = state.getRegisterUnsigned(i.r2);
        int result = performAddition(r1, r2, 0);
        state.setRegisterByte(i.r1, (byte) result);
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
        state.setFlag_S(xor(state.getFlag_N(), state.getFlag_V()));

        writeRegisterWord(i.r1, result);
    }

    public void visit(Instr.AND i) { // and first register with second
        int r1 = state.getRegisterByte(i.r1);
        int r2 = state.getRegisterByte(i.r2);
        int result = performAnd(r1, r2);
        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.ANDI i) { // and register with immediate
        int r1 = state.getRegisterByte(i.r1);
        int r2 = i.imm1;
        int result = performAnd(r1, r2);
        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.ASR i) { // arithmetic shift right by one bit
        int r1 = state.getRegisterByte(i.r1);
        int result = performRightShift(r1, (r1 & 0x80) != 0);
        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.BCLR i) { // clear bit in SREG
        state.setSREG_bit(i.imm1, false);
    }

    public void visit(Instr.BLD i) { // load bit from T flag into register
        boolean T = state.getFlag_T();
        byte val = state.getRegisterByte(i.r1);
        if (T)
            val = Arithmetic.setBit(val, i.imm1);
        else
            val = Arithmetic.clearBit(val, i.imm1);
        state.setRegisterByte(i.r1, val);
    }

    public void visit(Instr.BRBC i) { // branch if bit in SREG is clear
        byte val = state.getSREG();
        boolean f = Arithmetic.getBit(val, i.imm1);
        if (!f)
            relativeBranch(i.imm2);
    }

    public void visit(Instr.BRBS i) { // branch if bit in SREG is set
        byte val = state.getSREG();
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
        if (!state.getFlag_S())
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
        if (state.getFlag_S())
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
        state.setSREG_bit(i.imm1, true);
    }

    public void visit(Instr.BST i) { // store bit in register to T flag
        byte val = state.getRegisterByte(i.r1);
        boolean T = Arithmetic.getBit(val, i.imm1);
        state.setFlag_T(T);
    }

    public void visit(Instr.CALL i) { // call an absolute address
        pushPC(nextPC);
        nextPC = absolute(i.imm1);
    }

    public void visit(Instr.CBI i) { // clear bit in IO register
        state.getIOReg(i.imm1).clearBit(i.imm2);
    }

    public void visit(Instr.CBR i) { // clear bits in register
        int r1 = state.getRegisterByte(i.r1);
        int r2 = i.imm1;
        int result = performAnd(r1, ~r2);
        state.setRegisterByte(i.r1, (byte) result);
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
        state.setRegisterByte(i.r1, (byte) 0);
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
        int r1 = state.getRegisterByte(i.r1);
        int result = 0xff - r1;

        boolean C = true;
        boolean N = (result & 0x80) != 0;
        boolean Z = (result & 0xff) == 0;
        boolean V = false;
        boolean S = xor(N, V);
        setFlag_CNZVS(C, N, Z, V, S);

        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.CP i) { // compare registers
        int r1 = state.getRegisterByte(i.r1);
        int r2 = state.getRegisterByte(i.r2);
        // perform subtraction for flag side effects.
        performSubtraction(r1, r2, 0);
    }

    public void visit(Instr.CPC i) { // compare registers with carry
        int r1 = state.getRegisterByte(i.r1);
        int r2 = state.getRegisterByte(i.r2);
        // perform subtraction for flag side effects.
        performSubtractionPZ(r1, r2, (state.getFlag_C() ? 1 : 0));
    }

    public void visit(Instr.CPI i) { // compare register with immediate
        int r1 = state.getRegisterByte(i.r1);
        int r2 = i.imm1;
        // perform subtraction for flag side effects.
        performSubtraction(r1, r2, 0);
    }

    public void visit(Instr.CPSE i) { // compare and skip next instruction if equal
        int r1 = state.getRegisterByte(i.r1);
        int r2 = state.getRegisterByte(i.r2);
        // TODO: test this instruction more thoroughly!!!!
        performSubtraction(r1, r2, 0);
        if (r1 == r2) skip();
    }

    public void visit(Instr.DEC i) { // decrement register
        int r1 = state.getRegisterUnsigned(i.r1);
        int result = r1 - 1;

        boolean N = (result & 0x080) != 0;
        boolean Z = (result & 0xff) == 0;
        boolean V = r1 == 0x80;
        boolean S = xor(N, V);
        setFlag_NZVS(N, Z, V, S);

        state.setRegisterByte(i.r1, (byte) result);
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
        int extra = state.getIORegisterByte(RAMPZ);
        byte val = state.getProgramByte(address + (extra << 16));
        state.setRegisterByte(Register.R0, val);
    }

    public void visit(Instr.ELPMD i) { // extended load program memory with destination
        int address = readRegisterWord(Register.Z);
        int extra = state.getIORegisterByte(RAMPZ);
        byte val = state.getProgramByte(address + (extra << 16));
        state.setRegisterByte(i.r1, val);
    }

    public void visit(Instr.ELPMPI i) { // extends load program memory with post decrement
        int address = readRegisterWord(Register.Z);
        int extra = state.getIORegisterByte(RAMPZ);
        byte val = state.getProgramByte(address + (extra << 16));
        state.setRegisterByte(i.r1, val);
        writeRegisterWord(Register.Z, address + 1);
    }

    public void visit(Instr.EOR i) { // exclusive or first register with second
        int r1 = state.getRegisterByte(i.r1);
        int r2 = state.getRegisterByte(i.r2);
        int result = r1 ^ r2;

        boolean N = (result & 0x080) != 0;
        boolean Z = (result & 0xff) == 0;
        boolean V = false;
        boolean S = xor(N, V);
        setFlag_NZVS(N, Z, V, S);

        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.FMUL i) { // fractional multiply
        int r1 = state.getRegisterUnsigned(i.r1);
        int r2 = state.getRegisterUnsigned(i.r2);
        int result = (r1 * r2) << 1;
        state.setFlag_Z((result & 0xffff) == 0);
        state.setFlag_C(Arithmetic.getBit(result, 16));
        state.setRegisterWord(Register.R0, result);
    }

    public void visit(Instr.FMULS i) { // fractional multiply, signed
        int r1 = state.getRegisterByte(i.r1);
        int r2 = state.getRegisterByte(i.r2);
        int result = (r1 * r2) << 1;
        state.setFlag_Z((result & 0xffff) == 0);
        state.setFlag_C(Arithmetic.getBit(result, 16));
        state.setRegisterWord(Register.R0, result);
    }

    public void visit(Instr.FMULSU i) { // fractional multiply signed with unsigned
        int r1 = state.getRegisterByte(i.r1);
        int r2 = state.getRegisterUnsigned(i.r2);
        int result = (r1 * r2) << 1;
        state.setFlag_Z((result & 0xffff) == 0);
        state.setFlag_C(Arithmetic.getBit(result, 16));
        state.setRegisterWord(Register.R0, result);
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
        byte val = state.getIORegisterByte(i.imm1);
        state.setRegisterByte(i.r1, val);
    }

    public void visit(Instr.INC i) { // increment register
        int r1 = state.getRegisterUnsigned(i.r1);
        int result = r1 + 1;

        boolean N = (result & 0x080) != 0;
        boolean Z = (result & 0xff) == 0;
        boolean V = r1 == 0x7f;
        boolean S = xor(N, V);
        setFlag_NZVS(N, Z, V, S);

        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.JMP i) { // unconditional jump to absolute address
        nextPC = absolute(i.imm1);
    }

    public void visit(Instr.LD i) { // load from SRAM
        int address = readRegisterWord(i.r2);
        byte val = state.getDataByte(address);
        state.setRegisterByte(i.r1, val);
    }

    public void visit(Instr.LDD i) { // load with displacement from register Y or Z
        int address = readRegisterWord(i.r2) + i.imm1;
        byte val = state.getDataByte(address);
        state.setRegisterByte(i.r1, val);
    }

    public void visit(Instr.LDI i) { // load immediate
        state.setRegisterByte(i.r1, (byte) i.imm1);
    }

    public void visit(Instr.LDPD i) { // load from SRAM with pre-decrement
        int address = readRegisterWord(i.r2) - 1;
        byte val = state.getDataByte(address);
        state.setRegisterByte(i.r1, val);
        writeRegisterWord(i.r2, address);
    }

    public void visit(Instr.LDPI i) { // load from SRAM with post-increment
        int address = readRegisterWord(i.r2);
        byte val = state.getDataByte(address);
        state.setRegisterByte(i.r1, val);
        writeRegisterWord(i.r2, address + 1);
    }

    public void visit(Instr.LDS i) { // load from SRAM at absolute address
        byte val = state.getDataByte(i.imm1);
        state.setRegisterByte(i.r1, val);
    }

    public void visit(Instr.LPM i) { // load from program memory
        int address = readRegisterWord(Register.Z);
        byte val = state.getProgramByte(address);
        state.setRegisterByte(Register.R0, val);
    }

    public void visit(Instr.LPMD i) { // load from program memory with destination
        int address = readRegisterWord(Register.Z);
        byte val = state.getProgramByte(address);
        state.setRegisterByte(i.r1, val);
    }

    public void visit(Instr.LPMPI i) { // load from program memory with post-increment
        int address = readRegisterWord(Register.Z);
        byte val = state.getProgramByte(address);
        state.setRegisterByte(i.r1, val);
        writeRegisterWord(Register.Z, address + 1);
    }

    public void visit(Instr.LSL i) { // logical shift register left by one
        int r1 = state.getRegisterByte(i.r1);
        int result = performLeftShift(r1, 0);
        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.LSR i) { // logical shift register right by one
        int r1 = state.getRegisterByte(i.r1);
        int result = performRightShift(r1, false);
        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.MOV i) { // copy second register into first
        byte result = state.getRegisterByte(i.r2);
        state.setRegisterByte(i.r1, result);
    }

    public void visit(Instr.MOVW i) { // copy second register pair into first
        int result = readRegisterWord(i.r2);
        writeRegisterWord(i.r1, result);
    }

    public void visit(Instr.MUL i) { // multiply first register with second
        int r1 = state.getRegisterUnsigned(i.r1);
        int r2 = state.getRegisterUnsigned(i.r2);
        int result = r1 * r2;
        state.setFlag_C(Arithmetic.getBit(result, 15));
        state.setFlag_Z((result & 0xffff) == 0);
        writeRegisterWord(Register.R0, result);
    }

    public void visit(Instr.MULS i) { // multiply first register with second, signed
        int r1 = state.getRegisterByte(i.r1);
        int r2 = state.getRegisterByte(i.r2);
        int result = r1 * r2;
        state.setFlag_C(Arithmetic.getBit(result, 15));
        state.setFlag_Z((result & 0xffff) == 0);
        writeRegisterWord(Register.R0, result);
    }

    public void visit(Instr.MULSU i) { // multiply first register with second, signed and unsigned
        int r1 = state.getRegisterByte(i.r1);
        int r2 = state.getRegisterUnsigned(i.r2);
        int result = r1 * r2;
        state.setFlag_C(Arithmetic.getBit(result, 15));
        state.setFlag_Z((result & 0xffff) == 0);
        writeRegisterWord(Register.R0, result);
    }

    public void visit(Instr.NEG i) { // negate register
        int r1 = state.getRegisterByte(i.r1);
        int result = performSubtraction(0, r1, 0);
        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.NOP i) { // no-op operation
        // do nothing.
    }

    public void visit(Instr.OR i) { // or first register with second
        int r1 = state.getRegisterByte(i.r1);
        int r2 = state.getRegisterByte(i.r2);
        int result = performOr(r1, r2);

        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.ORI i) { // or register with immediate
        int r1 = state.getRegisterByte(i.r1);
        int r2 = i.imm1;
        int result = performOr(r1, r2);

        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.OUT i) { // write byte to IO register
        byte r1 = state.getRegisterByte(i.r1);
        state.setIORegisterByte(r1, i.imm1);
    }

    public void visit(Instr.POP i) { // pop a byte from the stack (SPL:SPH IO registers)
        byte val = state.popByte();
        state.setRegisterByte(i.r1, val);
    }

    public void visit(Instr.PUSH i) { // push a byte to the stack
        byte val = state.getRegisterByte(i.r1);
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
        int r1 = state.getRegisterUnsigned(i.r1);
        int result = performLeftShift(r1, (state.getFlag_C() ? 1 : 0));

        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.ROR i) { // rotate register right through carry flag
        int r1 = state.getRegisterByte(i.r1);
        int result = performRightShift(r1, state.getFlag_C());
        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.SBC i) { // subtract second register from first with carry
        int r1 = state.getRegisterByte(i.r1);
        int r2 = state.getRegisterByte(i.r2);
        int result = performSubtractionPZ(r1, r2, (state.getFlag_C() ? 1 : 0));
        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.SBCI i) { // subtract immediate from register with carry
        int r1 = state.getRegisterByte(i.r1);
        int r2 = i.imm1;
        int result = performSubtractionPZ(r1, r2, (state.getFlag_C() ? 1 : 0));
        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.SBI i) { // set bit in IO register
        state.getIOReg(i.imm1).setBit(i.imm2);
    }

    public void visit(Instr.SBIC i) { // skip if bit in IO register is clear
        byte val = state.getIORegisterByte(i.imm1);
        boolean f = Arithmetic.getBit(val, i.imm2);
        if (!f) skip();
    }

    public void visit(Instr.SBIS i) { // skip if bit in IO register is set
        byte val = state.getIORegisterByte(i.imm1);
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
        int r1 = state.getRegisterByte(i.r1);
        int r2 = i.imm1;
        int result = performOr(r1, r2);

        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.SBRC i) { // skip if bit in register cleared
        byte r1 = state.getRegisterByte(i.r1);
        boolean f = Arithmetic.getBit(r1, i.imm1);
        if (!f) skip();
    }

    public void visit(Instr.SBRS i) { // skip if bit in register set
        byte r1 = state.getRegisterByte(i.r1);
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
        state.setRegisterByte(i.r1, (byte) 0xff);
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
        sleeping = true;
    }

    public void visit(Instr.SPM i) { // store register to program memory
        // TODO: figure out how this instruction behaves on Atmega128L
        unimplemented(i);
    }

    public void visit(Instr.ST i) { // store register to data-seg[r1]
        int address = readRegisterWord(i.r1);
        byte val = state.getRegisterByte(i.r2);
        state.setDataByte(val, address);
    }

    public void visit(Instr.STD i) { // store to data space with displacement from Y or Z
        int address = readRegisterWord(i.r1) + i.imm1;
        byte val = state.getRegisterByte(i.r2);
        state.setDataByte(val, address);
    }

    public void visit(Instr.STPD i) { // decrement r2 and store register to data-seg(r2)
        int address = readRegisterWord(i.r1) - 1;
        byte val = state.getRegisterByte(i.r2);
        state.setDataByte(val, address);
        writeRegisterWord(i.r1, address);
    }

    public void visit(Instr.STPI i) { // store register to data-seg(r2) and post-inc
        int address = readRegisterWord(i.r1);
        byte val = state.getRegisterByte(i.r2);
        state.setDataByte(val, address);
        writeRegisterWord(i.r1, address + 1);
    }

    public void visit(Instr.STS i) { // store direct to data-seg(imm1)
        byte val = state.getRegisterByte(i.r1);
        state.setDataByte(val, i.imm1);
    }

    public void visit(Instr.SUB i) { // subtract second register from first
        int r1 = state.getRegisterByte(i.r1);
        int r2 = state.getRegisterByte(i.r2);
        int result = performSubtraction(r1, r2, 0);
        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.SUBI i) { // subtract immediate from register
        int r1 = state.getRegisterByte(i.r1);
        int r2 = i.imm1;
        int result = performSubtraction(r1, r2, 0);
        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.SWAP i) { // swap nibbles in register
        int result = state.getRegisterUnsigned(i.r1);
        result = (result >> 4) | (result << 4);
        state.setRegisterByte(i.r1, (byte) result);
    }

    public void visit(Instr.TST i) { // test for zero or minus
        int r1 = state.getRegisterByte(i.r1);
        state.setFlag_V(false);
        state.setFlag_Z((r1 & 0xff) == 0);
        state.setFlag_N(Arithmetic.getBit(r1, 7));
        state.setFlag_S(xor(state.getFlag_N(), state.getFlag_V()));
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
        int dist = state.getInstr(nextPC).getSize();
        if (dist == 2)
            cyclesConsumed++;
        else
            cyclesConsumed += 2;
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
        state.pushByte(Arithmetic.low(pc));
        state.pushByte(Arithmetic.high(pc));
    }

    private int popPC() {
        byte high = state.popByte();
        byte low = state.popByte();
        return Arithmetic.uword(low, high) * 2;
    }

    private int readRegisterWord(Register r1) {
        return state.getRegisterWord(r1);
    }

    private void writeRegisterWord(Register r1, int val) {
        state.setRegisterWord(r1, val);
    }

    private void unimplemented(Instr i) {
        throw Avrora.failure("unimplemented instruction: " + i.getVariant());
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
        boolean H = (!Rd3 && Rr3) || (Rr3 && R3) || (R3 && !Rd3);
        boolean C = (!Rd7 && Rr7) || (Rr7 && R7) || (R7 && !Rd7);
        boolean N = (result & 0x080) != 0;
        boolean Z = (result & 0xff) == 0 ? state.getFlag_Z() : false;
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

    /**
     * The <code>InstructionCountTimeout</code> class is a probe that
     * simply counts down and throws a <code>TimeoutException</code>
     * when the count reaches zero. It is useful for ensuring termination
     * of the simulator, for performance testing, or for profiling and
     * stopping after a specified number of invocations.
     *
     * @author Ben L. Titzer
     */
    public static class InstructionCountTimeout implements Probe {
        public final long timeout;
        protected long left;

        /**
         * The constructor for <code>InstructionCountTimeout</code> creates
         * with the specified initial value.
         *
         * @param t
         */
        public InstructionCountTimeout(long t) {
            timeout = t;
            left = t;
        }

        /**
         * The <code>fireBefore()</code> method is called before the probed instruction
         * executes. In the implementation of the timeout, it simply decrements the
         * timeout and and throws a TimeoutException when the count reaches zero.
         *
         * @param i       the instruction being probed
         * @param address the address at which this instruction resides
         * @param state   the state of the simulation
         */
        public void fireBefore(Instr i, int address, State state) {
            if (--left <= 0)
                throw new TimeoutException(i, address, state, timeout);
        }

        /**
         * The <code>fireAfter()</code> method is called after the probed instruction
         * executes. In the implementation of the timeout, it does nothing.
         *
         * @param i       the instruction being probed
         * @param address the address at which this instruction resides
         * @param state   the state of the simulation
         */
        public void fireAfter(Instr i, int address, State state) {
            // do nothing.
        }
    }

    /**
     * The <code>InstructionCountTimeout</code> class is a probe that
     * simply counts down and throws a <code>TimeoutException</code>
     * when the count reaches zero. It is useful for ensuring termination
     * of the simulator, for performance testing, or for profiling and
     * stopping after a specified number of invocations.
     *
     * @author Ben L. Titzer
     */
    public static class ClockCycleTimeout implements Trigger {
        public final long timeout;

        /**
         * The constructor for <code>InstructionCountTimeout</code> creates
         * with the specified initial value.
         *
         * @param t
         */
        public ClockCycleTimeout(long t) {
            timeout = t;
        }

        /**
         * The <code>fire()</code> method is called when the  timeout is up.
         *
         */
        public void fire() {
            // TODO: throw correct error
            throw new Error("Timeout reached after "+timeout+" clock cycles");
        }

    }

    abstract class IMRReg extends State.RWIOReg {

        protected final int baseVect;
        protected final boolean increasingVectors;

        IMRReg(boolean inc, int bv) {
            baseVect = bv;
            increasingVectors = inc;
        }

        public void update(IMRReg other) {
            int posted = this.value & other.value & 0xff;
            int shift = baseVect;
            if (!increasingVectors) {
                shift -= 8;
                posted = Arithmetic.reverseBits((byte) posted);
            }
            long previousPosted = state.getPostedInterrupts() & ~(0xff << shift);
            long newPosted = previousPosted | (posted << shift);
            state.setPostedInterrupts(newPosted);
        }

        public void update(int bit, IMRReg other) {
            int posted = this.value & other.value & (1 << bit);
            if (posted != 0)
                state.postInterrupt(getVectorNum(bit));
            else
                state.unpostInterrupt(getVectorNum(bit));
        }

        protected int getVectorNum(int bit) {
            if (increasingVectors)
                return baseVect + bit;
            else
                return baseVect - bit;
        }
    }

    public class FlagRegister extends IMRReg {

        public final MaskRegister maskRegister;

        public FlagRegister(boolean inc, int baseVect) {
            super(inc, baseVect);
            maskRegister = new MaskRegister(inc, baseVect, this);
        }

        public void write(byte val) {
            value = (byte) (value & ~val);
            update(maskRegister);
        }

        public void flagBit(int bit) {
            value = Arithmetic.setBit(value, bit);
            update(bit, maskRegister);
        }

        public void setBit(int bit) {
            value = Arithmetic.clearBit(value, bit);
            state.unpostInterrupt(getVectorNum(bit));
        }

        public void clearBit(int bit) {
            // clearing a bit does nothing.
        }
    }

    public class MaskRegister extends IMRReg {

        public final FlagRegister flagRegister;

        MaskRegister(boolean inc, int baseVect, FlagRegister fr) {
            super(inc, baseVect);
            flagRegister = fr;
        }

        public void write(byte val) {
            value = val;
            update(flagRegister);
        }

        public void setBit(int bit) {
            value = Arithmetic.setBit(value, bit);
            update(bit, flagRegister);
        }

        public void clearBit(int bit) {
            value = Arithmetic.clearBit(value, bit);
            state.unpostInterrupt(getVectorNum(bit));
        }
    }

}
