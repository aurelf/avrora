package avrora.sim;

import avrora.core.InstrVisitor;
import avrora.core.Instr;
import avrora.core.Program;
import avrora.core.Register;
import avrora.Arithmetic;
import avrora.Operand;
import avrora.sim.util.MulticastProbe;
import avrora.sim.util.PeriodicTrigger;
import vpc.util.Terminal;
import vpc.util.StringUtil;
import vpc.VPCBase;

/**
 * The <code>Simulator</code> class implements a full processor simulator
 * for the AVR instruction set. It is the base class of specific implementations
 * that implement processor-specific behavior.
 *
 * @author Ben L. Titzer
 */
public abstract class Simulator extends VPCBase implements InstrVisitor, IORegisterConstants {

    public static final Probe TRACEPROBE = new Probe() {
        public void fireBefore(Instr i, int pc, State s) {
            Terminal.printBrightCyan(StringUtil.toHex(pc, 4) + ": ");
            Terminal.printBrightBlue(i.getVariant() + " ");
            Terminal.print(i.getOperands());
            Terminal.nextln();
        }
        public void fireAfter(Instr i, int pc, State s) {

        }
    };

    protected final Program program;
    protected State state;

    protected final MulticastProbe activeProbe;

    protected boolean shouldRun;
    protected boolean justReturnedFromInterrupt;
    protected int nextPC;
    protected Interrupt[] interrupts;

    protected DeltaQueue eventQueue;

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
    public Simulator(Program p) {
        program = p;
        interrupts = new Interrupt[MAX_INTERRUPTS];
        activeProbe = new MulticastProbe();

        // set all interrupts to ignore
        for ( int cntr = 0; cntr < MAX_INTERRUPTS; cntr++ )
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
         * @param i the instruction being probed
         * @param address the address at which this instruction resides
         * @param state the state of the simulation
         */
        public void fireBefore(Instr i, int address, State state);

        /**
         * The <code>fireAfter()</code> method is called after the probed instruction
         * executes.
         *
         * @param i the instruction being probed
         * @param address the address at which this instruction resides
         * @param state the state of the simulation
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
     * The <code>DeltaQueue</code> class implements an amortized constant time
     * delta-queue for processing of scheduled events. Events are put into the queue
     * that will fire at a given number of cycles in the future. An internal delta
     * list is maintained where each link in the list represents a set of triggers
     * to be fired some number of clock cycles after the previous link.
     *
     * Each delta between links is maintained to be non-zero. Thus, to insert a
     * trigger X cycles in the future, at most X nodes will be skipped over. Therefore
     * over X time steps, this cost is amortized to be constant.
     *
     * For each clock cycle, only the first node in the list must be checked, leading
     * to constant time work per clock cycle.
     *
     * This class allows the clock to be advanced multiple ticks at a time.
     *
     * Also, since this class is used heavily in the simulator, its performance is
     * important and maintains an internal cache of objects. Thus, it does not create
     * garbage over its execution and never uses more space than is required to store
     * the maximum encountered simultaneous events. It does not use standard libraries,
     * casts, virtual dispatch, etc.
     */
    public static class DeltaQueue {
        static final class TriggerLink {
            Trigger trigger;
            TriggerLink next;

            TriggerLink(Trigger t) {
                trigger = t;
            }

        }

        final class Link {
            TriggerLink head;
            TriggerLink tail;

            Link next;
            long delta;

            Link(Trigger t, long d) {
                tail = head = newList(t);
                delta = d;
            }

            void add(Trigger t) {
                if ( head == null ) {
                    head = tail = newList(t);
                } else {
                    tail.next = newList(t);
                    tail = tail.next;
                }
            }

            void remove(Trigger t) {
                TriggerLink prev = null;
                TriggerLink pos = head;
                while ( pos != null ) {
                    TriggerLink next = pos.next;

                    if ( pos.trigger == t ) {
                        // remove the whole thing.
                        if ( prev == null ) head = pos.next;
                        else prev.next = pos.next;

                        free(pos);
                    } else {
                        prev = pos;
                    }
                    pos = next;
                }
            }

            void fire() {
                for ( TriggerLink pos = head; pos != null; pos = pos.next )
                    pos.trigger.fire();
            }
        }

        Link head;
        Link freeLinks;
        TriggerLink freeTriggerLinks;

        /**
         * The <code>add</code> method adds a trigger to be executed in the future.
         * @param t the trigger to fire
         * @param cycles the number of clock cycles in the future
         */
        public void add(Trigger t, long cycles) {
            // degenerate case, nothing in the queue.
            if ( head == null ) {
                head = newLink(t, cycles, null);
                return;
            }

            // search for first link that is "after" this cycle delta
            Link prev = null;
            Link pos = head;
            while ( pos != null && cycles > pos.delta ) {
                cycles -= pos.delta;
                prev = pos;
                pos = pos.next;
            }

            if ( pos == null ) {
                // end of the head
                prev.next = newLink(t, cycles, null);
            } else if ( cycles == pos.delta ) {
                // exactly matched the delta of some other event
                pos.add(t);
            } else {
                // insert a new link in the chain
                prev.next = newLink(t, cycles, pos);
            }
        }

        /**
         * The <code>remove</code> method removes all occurrences of the specified
         * trigger within the delta queue.
         * @param e
         */
        void remove(Trigger e) {
            if ( head == null ) return;

            // search for first link that is "after" this cycle delta
            Link prev = null;
            Link pos = head;
            while ( pos != null ) {
                Link next = pos.next;
                pos.remove(e);

                if ( pos.head == null ) {
                    // remove the whole thing.
                    if ( prev == null ) head = pos.next;
                    else prev.next = pos.next;

                    free(pos);
                } else {
                    prev = pos;
                }
                pos = next;
            }
        }

        /**
         * The <code>advance</code> method advances timesteps through the queue by the
         * specified number of clock cycles, processing any triggers.
         * @param cycles the number of clock cycles to advance
         */
        void advance(long cycles) {
            while ( head != null && cycles >= 0 ) {

                Link pos = head;
                Link next = pos.next;

                long left = cycles - pos.delta;
                pos.delta = -left;

                // if haven't arrived yet, break
                if ( pos.delta > 0 ) break;

                // chop off head
                head = next;

                // fire all events at head
                pos.fire();

                // free the head
                free(pos);

                // consume the cycles
                cycles = left;
            }
        }

        void free(Link l) {
            l.next = freeLinks;
            freeLinks = l;

            l.tail.next = freeTriggerLinks;
            freeTriggerLinks = l.head;
        }

        void free(TriggerLink l) {
            l.next = freeTriggerLinks;
            freeTriggerLinks = l;
        }

        Link newLink(Trigger t, long cycles, Link next) {
            Link l;
            if ( freeLinks == null )
                // if none in the free list, allocate one
                l = new Link(t, cycles);
            else {
                // grab one from the free list
                l = freeLinks;
                freeLinks = freeLinks.next;
            }

            // adjust delta in the next link in the chain
            if ( next != null ) {
                next.delta -= cycles;
            }

            l.next = next;
            return l;
        }

        TriggerLink newList(Trigger t) {
            TriggerLink l;

            if ( freeTriggerLinks == null ) {
                l = new TriggerLink(t);
            } else {
                l = freeTriggerLinks;
                freeTriggerLinks = freeTriggerLinks.next;
                l.next = null;
            }

            return l;
        }
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
     *
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
            super("timeout @ " + StringUtil.toHex(a, 4) + " reached after "+t+" instructions");
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
            if ( !breakPoint ) breakFired = false;
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
            if ( v == Simulator.this ) {
                // fire the probe(s) before
                probe.fireBefore(instr, address, state);

                // breakpoint processing.
                if ( breakPoint ) {
                    if ( !breakFired ) {
                        breakFired = true;
                        throw new BreakPointException(instr, address, state);
                    }
                    else breakFired = false;
                }

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
        public void post();
        public void fire();
    }

    public class MaskableInterrupt implements Interrupt {
        protected final int interruptNumber;

        protected final int maskRegister;
        protected final int flagRegister;
        protected final int bit;

        protected final boolean sticky;

        public MaskableInterrupt(int num, int mr, int fr, int b, boolean e) {
            interruptNumber = num;
            maskRegister = mr;
            flagRegister = fr;
            bit = b;
            sticky = e;
        }

        public void post() {
            int flag = 1 << bit;
            int nfr = state.getIORegisterByte(flagRegister) | flag;
            state.setIORegisterByte((byte)nfr, flagRegister);
            int mask = state.getIORegisterByte(maskRegister);
            if ( (mask & flag) != 0 )
                state.postInterrupt(interruptNumber);
        }

        public void fire() {
            if ( !sticky ) {
                int nfr = state.getIORegisterByte(flagRegister) & ~(1 << bit);
                state.setIORegisterByte((byte)nfr, flagRegister);
                state.unpostInterrupt(interruptNumber);
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
        public void post() { }
        public void fire() { }
    };



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
     * simulator to enter a loop that executes instructions, firing probes
     * and triggers as it executes. The <code>start()</code> method returns
     * normally when the </code>break</code> AVR instruction is executed,
     * when a <code>BreakPointException</code> is thrown, when a <code>
     * TimeoutException</code> is thrown, or when the <code>stop()</code>
     * method on this simulator instance is called.
     */
    public void start() {
        shouldRun = true;
        loop();
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

    protected abstract State constructState();
    protected abstract State constructTracingState();

    private void loop() {

        nextPC = state.getPC();
        long oldCycles = state.getCycles();

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
                    int lowestbit = Arithmetic.lowestBit(interruptPostMask);

                    // fire the interrupt (update flag register(s) state)
                    interrupts[lowestbit].fire();

                    // store the return address
                    pushPC(nextPC);

                    // set PC to interrupt handler
                    nextPC = lowestbit * 4;
                    state.setPC(nextPC);

                    // disable interrupts
                    state.setFlag_I(false);

                    // process any timed events
                    state.consumeCycles(4);
                    eventQueue.advance(4);
                    oldCycles += 4;
                }
            }

            // get the current instruction
            int curPC = nextPC;
            Instr i = state.getInstr(nextPC);
            nextPC = nextPC + i.getSize();

            // visit the actual instruction (or probe)
            activeProbe.fireBefore(i, curPC, state);
            execute(i);
            activeProbe.fireAfter(i, curPC, state);

            // process any timed events
            long newCycles = state.getCycles();
            eventQueue.advance(newCycles - oldCycles);
            oldCycles = newCycles;
        }
    }

    private void execute(Instr i) {
        i.accept(this);
        state.setPC(nextPC);
        state.consumeCycles(i.getCycles());

    }

    /**
     * The <code>insertProbe()</code> method allows a probe to be inserted
     * that is executed before and after every instruction that is executed
     * by the simulator
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
     * @param p the probe to insert
     * @param addr the address at which to insert the probe
     */
    public void insertProbe(Probe p, int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if ( pi != null ) pi.add(p);
        else {
            pi = new ProbedInstr(state.getInstr(addr), addr, p);
            state.setInstr(pi, addr);
        }
    }

    /**
     * The <code>removeProbe()</code> method removes a probe from the global
     * probe table (the probes executed before and after every instruction).
     * The comparison used is reference equality, not <code>.equals()</code>.
     * @param b the probe to remove
     */
    public void removeProbe(Probe b) {
        activeProbe.remove(b);
    }

    /**
     * The <code>removeProbe()</code> method removes a probe from the
     * instruction at the specified the address. The comparison used is
     * reference equality, not <code>.equals()</code>.
     * @param p the probe to remove
     * @param addr the address from which to remove the probe
     */
    public void removeProbe(Probe p, int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if ( pi != null ) {
            pi.remove(p);
            if ( pi.isEmpty() )
                state.setInstr(pi.instr, pi.address);
        }
    }

    private ProbedInstr getProbedInstr(int addr) {
        Instr i = state.getInstr(addr);
        if ( i instanceof ProbedInstr )
            return ((ProbedInstr)i);
        else return null;
    }

    /**
     * The <code>insertBreakPoint()</code> method inserts a breakpoint
     * at the instruction at the specified address. At most one breakpoint
     * can be inserted at a particular instruction. Subsequent calls to
     * this method would then have no effect for the same address.
     * @param addr
     */
    public void insertBreakPoint(int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if ( pi != null ) pi.setBreakPoint();
        else {
            pi = new ProbedInstr(state.getInstr(addr), addr, null);
            state.setInstr(pi, addr);
            pi.setBreakPoint();
        }
    }

    /**
     * The <code>removeBreakPoint</code> method removes all breakpoints at
     * the specified instruction at the specified address.
     * @param addr
     */
    public void removeBreakPoint(int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if ( pi != null ) pi.unsetBreakPoint();
    }

    /**
     * The <code>setWatchPoint()</code> method allows a probe to be inserted
     * at a memory location. The probe will be executed before every read
     * or write to that memory location.
     * @param p the probe to insert
     * @param data_addr the data address at which to insert the probe
     */
    public void setWatchPoint(MemoryProbe p, int data_addr) {
        // TODO: implement watchpoints
    }

    /**
     * The <code>postInterrupt()</code> method causes the specified interrupt
     * to be posted to the simulator.
     * @param num the interrupt number to be posted
     */
    public void postInterrupt(int num) {
        interrupts[num].post();
    }

    protected void triggerInterrupt(int num) {
        interrupts[num].fire();
    }

    /**
     * The <code>addTimerEvent()</code> method inserts a trigger into the
     * event queue of the simulator with the specified delay in clock cycles.
     * The trigger will then be executed at the future time specified
     * @param e the trigger to be inserted
     * @param cycles the number of cycles in the future at which to trigger
     */
    public void addTimerEvent(Trigger e, long cycles) {
        eventQueue.add(e, cycles);
    }

    /**
     * The <code>addPeriodicTimerEvent()</code> method inserts a trigger into
     * the event queue of the simulator with the specified period. The <code>
     * PeriodicTrigger</code> instance created will continually reinsert the
     * trigger after each firing to achieve predictable periodic behavior.
     * @param e the trigger to insert
     * @param period the period in clock cycles
     * @return the <code>PeriodicTrigger</code> instance inserted
     */
    public PeriodicTrigger addPeriodicTimerEvent(Trigger e, long period) {
        PeriodicTrigger pt = new PeriodicTrigger(this, e, period);
        eventQueue.add(pt, period);
        return pt;
    }

    /**
     * The <code>removeTimerEvent()</code> method removes a trigger from
     * the event queue of the simulator. The comparison used is reference
     * equality, not <code>.equals()</code>.
     * @param e the trigger to remove
     */
    public void removeTimerEvent(Trigger e) {
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
        state.setRegisterByte(i.r1, (byte)0xff);
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
        state.consumeCycle();
    }

    private void skip() {
        // skip over next instruction
        int dist = state.getInstr(nextPC).getSize();
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
        throw failure("unimplemented instruction: "+i.getVariant());
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
     * @author Ben L. Titzer
     */
    public static class InstructionCountTimeout implements Probe {
        public final long timeout;
        protected long left;

        /**
         * The constructor for <code>InstructionCountTimeout</code> creates
         * with the specified initial value.
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
         * @param i the instruction being probed
         * @param address the address at which this instruction resides
         * @param state the state of the simulation
         */
        public void fireBefore(Instr i, int address, State state) {
            if ( --left <= 0 )
                throw new TimeoutException(i, address, state, timeout);
        }

        /**
         * The <code>fireAfter()</code> method is called after the probed instruction
         * executes. In the implementation of the timeout, it does nothing.
         *
         * @param i the instruction being probed
         * @param address the address at which this instruction resides
         * @param state the state of the simulation
         */
        public void fireAfter(Instr i, int address, State state) {
            // do nothing.
        }
    }

}
