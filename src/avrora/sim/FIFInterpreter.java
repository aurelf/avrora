package avrora.sim;

import avrora.core.Instr;
import avrora.core.Program;
import avrora.Avrora;
import avrora.util.Arithmetic;

/**
 * @author Ben L. Titzer
 */
public class FIFInterpreter extends BaseInterpreter {

    protected static class FIFInstr {

        public final int pc;
        public final int instr_num;
        public final Instr instr;
        public int r1;
        public int r2;
        public int imm1;
        public int imm2;

        private boolean breakPoint;
        private boolean breakFired;

        public Simulator.Probe probe;

        public FIFInstr next;
        public FIFInstr other;

        public FIFInstr(Instr i, int pc_, int inum_) {
            instr = i;
            pc = pc_;
            instr_num = inum_;
        }
    }

    protected FIFInstr fifMap[];

    protected FIFInstr curInstr;
    protected FIFInstr nextInstr;

    public FIFInterpreter(Simulator s, Program p, int fs, int is, int ss) {
        super(s, p, fs, is, ss);
        fifMap = new FIFInstr[p.program_end];
        buildFIFMap();
    }

    public void insertProbe(Simulator.Probe p) {
        throw Avrora.unimplemented();
    }

    public void insertProbe(Simulator.Probe p, int prog_addr) {
        throw Avrora.unimplemented();
    }

    public void removeProbe(Simulator.Probe p) {
        throw Avrora.unimplemented();
    }

    public void removeProbe(Simulator.Probe p, int prog_addr) {
        throw Avrora.unimplemented();
    }

    public void insertBreakPoint(int addr) {
        throw Avrora.unimplemented();
    }

    public void removeBreakPoint(int addr) {
        throw Avrora.unimplemented();
    }

    protected void runLoop() {

        nextInstr = curInstr;
        cyclesConsumed = 0;

        while (shouldRun) {

            if (justReturnedFromInterrupt) {
                // don't process the interrupt if we just returned from
                // an interrupt handler, because the hardware manual says
                // that at least one instruction is executed after
                // returning from an interrupt.
                justReturnedFromInterrupt = false;
            } else if (I) {

                // check if there are any pending (posted) interrupts
                if (postedInterrupts != 0) {
                    // the lowest set bit is the highest priority posted interrupt
                    int lowestbit = Arithmetic.lowestBit(postedInterrupts);

                    // fire the interrupt (update flag register(s) state)
                    simulator.triggerInterrupt(lowestbit);

                    // store the return address
                    pushPC(nextInstr.pc);

                    // set PC to interrupt handler
                    nextInstr = fifMap[simulator.getInterruptVectorAddress(lowestbit)];
                    curInstr = nextInstr;

                    // disable interrupts
                    I = false;

                    // process any timed events
                    advanceCycles(4);
                    sleeping = false;

                }
            }

            if (sleeping)
                sleepLoop();
            else {
                if (activeProbe.isEmpty())
                    fastLoop();
                else
                    instrumentedLoop();
            }
        }
    }

    private void sleepLoop() {
        innerLoop = true;
        while (innerLoop) {
            long delta = eventQueue.getHeadDelta();
            if (delta <= 0) delta = 1;
            advanceCycles(delta);
        }
    }

    private void fastLoop() {
        innerLoop = true;
        while (innerLoop) {
            // visit the actual instruction (or probe)
            execute(curInstr);
            curInstr = nextInstr;
            advanceCycles(cyclesConsumed);
        }
    }

    private void instrumentedLoop() {
        innerLoop = true;
        while (innerLoop) {
            // get the current instruction
            int curPC = nextInstr.pc; // at this point pc == nextPC
            Instr i = nextInstr.instr;

            // visit the actual instruction (or probe)
            activeProbe.fireBefore(i, curPC, this);
            execute(nextInstr);
            curInstr = nextInstr;
            advanceCycles(cyclesConsumed);
            activeProbe.fireAfter(i, curPC, this);
        }
    }

    public int getPC() {
        return curInstr.pc;
    }

    private void buildFIFMap() {
        FIFInstr last = null;
        FIFBuilder builder = new FIFBuilder();

        for ( int cntr = 0; cntr < simulator.program.program_end; cntr += 2) {
            Instr i = impression.readInstr(cntr);
            FIFInstr cur = builder.build(cntr, i);
            if ( last != null ) {
                if ( last.pc + last.instr.getSize() == cntr )
                    last.next = cur;
            }
            last = cur;
            fifMap[cntr] = cur;
        }
    }

//--BEGIN FIF GENERATOR

    private class FIFBuilder {

        int pc;
        FIFInstr instr;

        public FIFInstr build(int pc_, Instr i) {
            this.pc = pc_;
            return instr;
        }

    }

    private void execute(FIFInstr i) {

    }
//--END FIF GENERATOR

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

    private boolean xor(boolean a, boolean b) {
        return (a && !b) || (b && !a);
    }

    private byte low(int val) {
        return (byte) val;
    }

    private byte high(int val) {
        return (byte) (val >> 8);
    }

    private byte bit(boolean val) {
        if (val) return 1;
        return 0;
    }

    private int uword(byte low, byte high) {
        return Arithmetic.uword(low, high);
    }

    private void enterSleepMode() {
        sleeping = true;
        innerLoop = false;
    }


}
