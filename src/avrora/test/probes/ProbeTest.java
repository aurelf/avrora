package avrora.test.probes;

import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.sim.util.DeltaQueue;
import avrora.core.Instr;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

/**
 * @author Ben L. Titzer
 */
public class ProbeTest {

    HashMap entities;

    Simulator simulator;
    DeltaQueue eventqueue;

    abstract class TestEntity {
        final String name;

        TestEntity(String n) {
            name = n;
        }

        abstract void insert(int value);
        abstract void remove(int value);
    }

    public class TestProbe extends TestEntity implements Simulator.Probe {

        List beforeStmts;
        List afterStmts;

        TestProbe(String name, List b, List a) {
            super(name);
            beforeStmts = b;
            afterStmts = a;
        }

        public void fireBefore(Instr i, int addr, State s) {
            execute(beforeStmts);
        }

        public void fireAfter(Instr i, int addr, State s) {
            execute(afterStmts);
        }

        void insert(int value) {
            simulator.insertProbe(this, value);
        }

        void remove(int value) {
            simulator.removeProbe(this, value);
        }
    }

    public class TestWatch extends TestEntity implements Simulator.Watch {
        List beforeReadStmts;
        List afterReadStmts;
        List beforeWriteStmts;
        List afterWriteStmts;

        TestWatch(String name, List b1, List a1, List b2, List a2) {
            super(name);
            beforeReadStmts = b1;
            afterReadStmts = a1;
            beforeWriteStmts = b2;
            afterWriteStmts = a2;
        }

        public void fireBeforeRead(Instr i, int address, State state, int data_addr, byte value) {
            execute(beforeReadStmts);
        }

        public void fireBeforeWrite(Instr i, int address, State state, int data_addr, byte value) {
            execute(beforeWriteStmts);
        }

        public void fireAfterRead(Instr i, int address, State state, int data_addr, byte value) {
            execute(afterReadStmts);
        }

        public void fireAfterWrite(Instr i, int address, State state, int data_addr, byte value) {
            execute(afterWriteStmts);
        }

        void insert(int value) {
            simulator.insertWatch(this, value);
        }

        void remove(int value) {
            simulator.removeWatch(this, value);
        }
    }

    public class TestEvent extends TestEntity implements Simulator.Event {
        List fireStmts;

        TestEvent(String name, List b) {
            super(name);
            fireStmts = b;
        }


        public void fire() {
            execute(fireStmts);
        }

        void insert(int value) {
            if ( simulator != null ) simulator.insertEvent(this, value);
            else eventqueue.add(this, value);
        }

        void remove(int value) {
            if ( simulator != null ) simulator.removeEvent(this);
            else eventqueue.remove(this);
        }
    }

    abstract class Stmt {
        abstract void execute();
    }

    class InsertStmt extends Stmt {
        String name;
        int value;

        void execute() {
            TestEntity en = (TestEntity)entities.get(name);
            en.insert(value);
        }
    }

    class RemoveStmt extends Stmt {
        String name;
        int value;

        void execute() {
            TestEntity en = (TestEntity)entities.get(name);
            en.remove(value);
        }
    }

    class AdvanceStmt extends Stmt {
        int value;

        void execute() {
            eventqueue.advance(value);
        }
    }

    class RunStmt extends Stmt {
        void execute() {
            simulator.start();
        }
    }

    protected void execute(List l) {
        Iterator i = l.iterator();
        while ( i.hasNext() ) {
            Stmt s = (Stmt)i.next();
            s.execute();
        }
    }

    public void newProbe(String name, List b, List a) {
        TestEntity e = new TestProbe(name, b, a);
        entities.put(name, e);
    }

    public void newWatch(String name, List b1, List a1, List b2, List a2) {
        TestEntity e = new TestWatch(name, b1, a1, b2, a2);
        entities.put(name, e);
    }

    public void newEvent(String name, List b) {
        TestEntity e = new TestEvent(name, b);
        entities.put(name, e);
    }
}
