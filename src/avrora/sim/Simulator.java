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

    /**
     * The <code>microcontroller</code> field stores a reference to the microcontroller
     * being simulated.
     */
    protected final Microcontroller microcontroller;

    /**
     * The <code>LEGACY_INTERPRETER</code> field is used to turn on and off
     * the legacy interpreter. By default, the legacy interpreter is used.
     */
    public static boolean LEGACY_INTERPRETER = true;

    /**
     * The <code>interpreter</code> field stores a reference to the instruction
     * set interpreter.
     */
    protected BaseInterpreter interpreter;

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

    /**
     * The <code>MemoryProbe</code> interface represents a user probe that is
     * fired when a watchpoint detects an access to an address where this
     * memory probe has been inserted.
     */
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

    /**
     * The <code>getMicrocontroller()</code> method gets a reference to the
     * microcontroller being simulated.
     * @return a reference to the microcontroller being simulated
     */
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
        if ( LEGACY_INTERPRETER ) {
            interpreter = new LegacyInterpreter(this, program,
                    microcontroller.getFlashSize(),
                    microcontroller.getIORegSize(),
                    microcontroller.getRamSize());
        } else {
            interpreter = new GenInterpreter(this, program,
                    microcontroller.getFlashSize(),
                    microcontroller.getIORegSize(),
                    microcontroller.getRamSize());
        }
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
        interpreter.insertProbe(p);
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
        interpreter.removeProbe(b);
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
        if ( interruptPrinter.enabled )
            interruptPrinter.println("Simulator.forceInterrupt(" + num + ")");
        interrupts[num].force();
    }

    /**
     * The <code>triggerInterrupt()</code> method is used by device implementations
     * when they detect that an interrupt should be triggered. This method will
     * check whether this interrupt is enabled by consulting its own internal
     * table of interrupts that is kept consistent during writes to IO registers.
     * @param num the number of the interrupt to trigger
     */
    protected void triggerInterrupt(int num) {
        if ( interruptPrinter.enabled )
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
        if ( eventPrinter.enabled )
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
        if ( eventPrinter.enabled )
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
        if ( eventPrinter.enabled )
            eventPrinter.println("Simulator.removeTimerEvent()");
        eventQueue.remove(e);
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
