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
public abstract class Simulator implements IORegisterConstants {

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
     * The <code>activeProbe</code> field stores a reference to a
     * <code>MulticastProbe</code> that contains all of the probes to be fired
     * before and after the main execution runLoop--i.e. before and after
     * every instruction.
     */
    protected final MulticastProbe activeProbe;

    protected Interpreter interpreter;

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
        protected final int address;
        protected final Instr instr;
        protected final MulticastProbe probe;

        private boolean breakPoint;
        private boolean breakFired;

        ProbedInstr(Instr i, int a, Probe p) {
            super(new InstrProperties(i.properties.name, i.properties.variant, i.properties.size, 0));
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

        public void accept(InstrVisitor v) {

            // if the simulator is visiting us, execute the instruction instead of accept(v).
            if (v == interpreter ) {
                // breakpoint processing.
                if (breakPoint) {
                    if (!breakFired) {
                        breakFired = true;
                        throw new BreakPointException(instr, address, interpreter);
                    } else
                        breakFired = false;
                }

                interpreter.executeProbed(instr, address, probe);
            } else {
                instr.accept(v);
            }
        }

        public Instr build(int address, Operand[] ops) {
            return instr.build(address, ops);
        }

        public String getOperands() {
            return instr.getOperands();
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
        return interpreter;
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
        interpreter.shouldRun = true;
        interpreter.runLoop();
    }

    /**
     * The <code>stop()</code> method stops the simulation if it is running.
     * This method can be called from within a probe or trigger or from another
     * thread.
     */
    public void stop() {
        interpreter.shouldRun = false;
    }

    /**
     * The <code>reset()</code> method stops the simulation and resets its
     * state to the default initial state. Probes inserted in the program
     * are retained. All triggers are removed.
     */
    public void reset() {
        eventQueue = new DeltaQueue();
        interpreter = new Interpreter(program,
                microcontroller.getFlashSize(),
                microcontroller.getIORegSize(),
                microcontroller.getRamSize());
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
        interpreter.insertProbe(p, addr);
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
        interpreter.removeProbe(p, addr);
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
        interpreter.insertBreakPoint(addr);
    }

    /**
     * The <code>removeBreakPoint</code> method removes all breakpoints at
     * the specified instruction at the specified address.
     *
     * @param addr
     */
    public void removeBreakPoint(int addr) {
        interpreter.removeBreakPoint(addr);
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


    protected class Interpreter implements InstrVisitor, State {
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
        protected Interpreter(Program p, int flash_size, int ioreg_size, int sram_size) {

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
                        triggerInterrupt(lowestbit);

                        // store the return address
                        pushPC(nextPC);

                        // set PC to interrupt handler
                        nextPC = getInterruptVectorAddress(lowestbit);
                        pc = nextPC;

                        // disable interrupts
                        I = false;

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
                    Instr i = impression.readInstr(nextPC);
                    nextPC = nextPC + i.properties.size;

                    // visit the actual instruction (or probe)
                    // OPTIMIZATION OPPORTUNITY: common case of no active global probes
                    // could be approximately 18% of loop overhead
                    activeProbe.fireBefore(i, curPC, this);
                    execute(i);
                    activeProbe.fireAfter(i, curPC, this);
                }
            }
        }

        protected void insertProbe(Probe p, int addr) {
            ProbedInstr pi = getProbedInstr(addr);
            if (pi != null)
                pi.add(p);
            else {
                pi = new ProbedInstr(impression.readInstr(addr), addr, p);
                impression.writeInstr(pi, addr);
            }
        }

        protected void removeProbe(Probe p, int addr) {
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


        private void execute(Instr i) {
            i.accept(this);
            pc = nextPC;
            // process any timed events and advance state clock
            advanceCycles(cyclesConsumed + i.properties.cycles);
        }

        private void executeProbed(Instr instr, int address, Probe probe) {
            // fire the probe(s) before
            probe.fireBefore(instr, address, this);

            // execute actual instruction
            instr.accept(this);
            pc = nextPC;
            advanceCycles(cyclesConsumed + instr.properties.cycles);

            // fire the probe(s) after
            probe.fireAfter(instr, address, this);

        }

        protected void initializeIORegs() {
            for (int cntr = 0; cntr < ioregs.length; cntr++)
                ioregs[cntr] = new State.RWIOReg();
            SREG_reg = ioregs[SREG] = new SREG_reg();
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
            stop();
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
            setIORegisterByte(r1, i.imm1);
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
            setDataByte(val, address);
        }

        public void visit(Instr.STD i) { // store to data space with displacement from Y or Z
            int address = getRegisterWord(i.r1) + i.imm1;
            byte val = getRegisterByte(i.r2);
            setDataByte(val, address);
        }

        public void visit(Instr.STPD i) { // decrement r2 and store register to data-seg(r2)
            int address = getRegisterWord(i.r1) - 1;
            byte val = getRegisterByte(i.r2);
            setDataByte(val, address);
            setRegisterWord(i.r1, address);
        }

        public void visit(Instr.STPI i) { // store register to data-seg(r2) and post-inc
            int address = getRegisterWord(i.r1);
            byte val = getRegisterByte(i.r2);
            setDataByte(val, address);
            setRegisterWord(i.r1, address + 1);
        }

        public void visit(Instr.STS i) { // store direct to data-seg(imm1)
            byte val = getRegisterByte(i.r1);
            setDataByte(val, i.imm1);
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

        private void unimplemented(Instr i) {
            throw Avrora.failure("unimplemented instruction: " + i.getVariant());
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


        private void advanceCycles(long delta) {
            cycles += delta;
            eventQueue.advance(delta);
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
            return program.readInstr(npc).getSize();
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
            // do nothing
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
            if (--left <= 0)
                throw new TimeoutException(i, address, state, timeout);
        }
    }

    /**
     * The <code>InstructionCountTimeout</code> class is a probe that
     * simply counts down and throws an exception when the count reaches
     * zero. It is useful for ensuring termination of the simulator, for
     * performance testing, or for profiling and stopping after a
     * specified number of invocations.
     *
     * @author Ben L. Titzer
     */
    public static class ClockCycleTimeout implements Trigger {
        public final long timeout;

        /**
         * The constructor for <code>InstructionCountTimeout</code> creates a
         * timeout trigger with the specified initial value.
         *
         * @param t the number of cycles in the future
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
            long previousPosted = interpreter.postedInterrupts & ~(0xff << shift);
            long newPosted = previousPosted | (posted << shift);
            interpreter.postedInterrupts = (newPosted);
        }

        public void update(int bit, IMRReg other) {
            int posted = this.value & other.value & (1 << bit);
            if (posted != 0)
                interpreter.postInterrupt(getVectorNum(bit));
            else
                interpreter.unpostInterrupt(getVectorNum(bit));
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
            interpreter.unpostInterrupt(getVectorNum(bit));
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
            interpreter.unpostInterrupt(getVectorNum(bit));
        }
    }

}
