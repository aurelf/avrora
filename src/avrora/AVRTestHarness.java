package avrora;

import vpc.test.AutomatedTester;
import vpc.test.TestCase;
import vpc.test.TestResult;
import vpc.test.TestHarness;
import avrora.core.Program;
import avrora.core.Register;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.sim.ATMega128L;
import avrora.syntax.atmel.AtmelParser;
import vpc.VPCBase;
import vpc.VPCError;
import vpc.VPCInternalError;
import vpc.util.StringUtil;
import vpc.util.ColorTerminal;

import java.util.*;
import java.io.FileInputStream;
import java.io.File;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * The <code>AVRTestHarness</code> implements a test harness that interfaces the
 * <code>vpc.test.AutomatedTester</code> in order to automate testing of the
 * AVR parser and simulator.
 *
 * @author Ben L. Titzer
 */
public class AVRTestHarness extends VPCBase implements TestHarness {

    private static AtmelParser parser;

    class SimplifierTest extends TestCase.ExpectCompilationError {

        Module module;
        Program program;

        SimplifierTest(String fname, Properties props) {
            super(fname, props);
        }

        public void run() throws Exception {
            FileInputStream fis = new FileInputStream(new File(filename));
            Module module = new Module();
            AtmelParser parser = new AtmelParser(fis, module, filename);

            // let all exceptions fall through to AutomatedTester
            parser.Module();
            program = module.build();
        }
    }

    private static HashMap known = initKnowns();

    private static HashMap initKnowns() {
        HashMap map = new HashMap();
        map.put("r0", new ReadRegister("r0"));
        map.put("r1", new ReadRegister("r1"));
        map.put("r2", new ReadRegister("r2"));
        map.put("r3", new ReadRegister("r3"));
        map.put("r4", new ReadRegister("r4"));
        map.put("r5", new ReadRegister("r5"));
        map.put("r6", new ReadRegister("r6"));
        map.put("r7", new ReadRegister("r7"));
        map.put("r8", new ReadRegister("r8"));
        map.put("r9", new ReadRegister("r9"));
        map.put("r10", new ReadRegister("r10"));
        map.put("r11", new ReadRegister("r11"));
        map.put("r12", new ReadRegister("r12"));
        map.put("r13", new ReadRegister("r13"));
        map.put("r14", new ReadRegister("r14"));
        map.put("r15", new ReadRegister("r15"));
        map.put("r16", new ReadRegister("r16"));
        map.put("r17", new ReadRegister("r17"));
        map.put("r18", new ReadRegister("r18"));
        map.put("r19", new ReadRegister("r19"));
        map.put("r20", new ReadRegister("r20"));
        map.put("r21", new ReadRegister("r21"));
        map.put("r22", new ReadRegister("r22"));
        map.put("r23", new ReadRegister("r23"));
        map.put("r24", new ReadRegister("r24"));
        map.put("r25", new ReadRegister("r25"));
        map.put("r26", new ReadRegister("r26"));
        map.put("r27", new ReadRegister("r27"));
        map.put("r28", new ReadRegister("r28"));
        map.put("r29", new ReadRegister("r29"));
        map.put("r30", new ReadRegister("r30"));
        map.put("r31", new ReadRegister("r31"));
        map.put("pc", new ProcessorState("pc") {
            public int evaluate(Program p, State s) {
                return s.getPC();
            }
        });
        map.put("cc", new ProcessorState("cc") {
            public int evaluate(Program p, State s) {
                return (int) s.getCycles();
            }
        });
        map.put("sp", new ProcessorState("sp") {
            public int evaluate(Program p, State s) {
                return s.readSP();
            }
        });
        map.put("sreg", new ProcessorState("sreg") {
            public int evaluate(Program p, State s) {
                return s.readSREG();
            }
        });
        map.put("x", new ReadRegisterWord("x"));
        map.put("y", new ReadRegisterWord("y"));
        map.put("z", new ReadRegisterWord("z"));
        map.put("flags.i", new ReadFlag("flags.i", 7));
        map.put("flags.t", new ReadFlag("flags.t", 6));
        map.put("flags.h", new ReadFlag("flags.h", 5));
        map.put("flags.s", new ReadFlag("flags.s", 4));
        map.put("flags.v", new ReadFlag("flags.v", 3));
        map.put("flags.n", new ReadFlag("flags.n", 2));
        map.put("flags.z", new ReadFlag("flags.z", 1));
        map.put("flags.c", new ReadFlag("flags.c", 0));

        return map;
    }

    abstract static class Expr {
        public abstract int evaluate(Program p, State s);
    }

    abstract static class ProcessorState extends Expr {

        String register;

        ProcessorState(String s) {
            register = s;
        }

        public String toString() {
            return register;
        }
    }

    static class ReadRegister extends ProcessorState {
        Register register;

        ReadRegister(String n) {
            super(n);
            register = Register.getRegisterByName(n);
        }

        public int evaluate(Program p, State s) {
            return s.readRegister(register);
        }
    }

    static class ReadFlag extends ProcessorState {
        int flag;

        ReadFlag(String n, int f) {
            super(n);
            flag = f;
        }

        public int evaluate(Program p, State s) {
            return Arithmetic.getBit(s.readSREG(), flag) ? 1 : 0;
        }
    }

    static class ReadRegisterWord extends ProcessorState {
        Register register;

        ReadRegisterWord(String n) {
            super(n);
            register = Register.getRegisterByName(n);
        }

        public int evaluate(Program p, State s) {
            return s.readRegisterWord(register);
        }
    }

    static class Label extends Expr {
        String name;

        Label(String n) {
            name = n;
        }

        public int evaluate(Program p, State s) {
            Program.Label l = p.getLabel(name);
            if (l == null)
                throw new UnknownLabel(name);
            return l.address;
        }

        public String toString() {
            return name;
        }
    }

    static class Const extends Expr {
        int value;

        Const(int v) {
            value = v;
        }

        public int evaluate(Program p, State s) {
            return value;
        }

        public String toString() {
            return Integer.toString(value);
        }
    }

    static class Memory extends Expr {
        Expr expr;

        Memory(Expr e) {
            expr = e;
        }

        public int evaluate(Program p, State s) {
            return s.readDataByte(expr.evaluate(p, s));
        }

        public String toString() {
            return "$(" + expr + ")";
        }
    }

    abstract static class BinOp extends Expr {
        Expr left, right;
        String op;

        BinOp(Expr l, Expr r, String o) {
            left = l;
            right = r;
            op = o;
        }

        public String toString() {
            return left + op + right;
        }
    }

    static class Add extends BinOp {

        Add(Expr l, Expr r) {
            super(l, r, "+");
        }

        public int evaluate(Program p, State s) {
            int lval = left.evaluate(p, s);
            int rval = right.evaluate(p, s);
            return lval + rval;
        }
    }

    static class Subtract extends BinOp {

        Subtract(Expr l, Expr r) {
            super(l, r, "-");
        }

        public int evaluate(Program p, State s) {
            int lval = left.evaluate(p, s);
            int rval = right.evaluate(p, s);
            return lval - rval;
        }
    }

    static class UnknownLabel extends RuntimeException {
        String name;

        UnknownLabel(String n) {
            name = n;
        }
    }

    static class SimulatorTest extends TestCase {

        Module module;
        Program program;
        Simulator simulator;
        List predicates;

        SimulatorTest(String fname, Properties props) throws Exception {
            super(fname);
            String result = trimquotes(props.getProperty("Result").trim());
            predicates = new LinkedList();
            parseResult(result);
        }

        public void run() throws Exception {
            FileInputStream fis = new FileInputStream(new File(filename));
            Module module = new Module();
            AtmelParser parser = new AtmelParser(fis, module, filename);

            // let all exceptions fall through to AutomatedTester
            parser.Module();
            program = module.build();
            simulator = new ATMega128L().loadProgram(program);
            simulator.start();
        }

        public TestResult match(Throwable t) {
            if (t != null) return super.match(t);

            State state = simulator.getState();
            Iterator i = predicates.iterator();

            try {
                while (i.hasNext()) {
                    StatePredicate p = (StatePredicate) i.next();
                    if (!p.check(program, state))
                        return new StateMismatch(p, state);
                }
            } catch (UnknownLabel l) {
                return new TestResult.TestFailure("unknown label specified in predicate: " + l.name);
            }
            return new TestResult.TestSuccess();
        }

        private void parseResult(String result) throws Exception {
            CharacterIterator i = new StringCharacterIterator(result);
            while (true) {
                StatePredicate s = readPredicate(i);
                // verboseln("parsed: "+s.left+" = "+s.right);
                StringUtil.skipWhiteSpace(i);
                predicates.add(s);
                if (i.current() == i.DONE)
                    break;
                else
                    expectChar(i, ',');
            }
        }

        // expr = expr
        private StatePredicate readPredicate(CharacterIterator i) throws Exception {
            // verboseln("Predicate @ "+i.getIndex()+" > '"+i.current()+"'");
            Expr left = readExpr(i);
            StringUtil.skipWhiteSpace(i);
            expectChar(i, '=');
            Expr right = readExpr(i);

            return new StatePredicate(left, right);
        }

        // term (+/- term)*
        private Expr readExpr(CharacterIterator i) throws Exception {
            // verboseln("Expr @ "+i.getIndex()+" > '"+i.current()+"'");
            StringUtil.skipWhiteSpace(i);
            Expr t = readTerm(i);

            StringUtil.skipWhiteSpace(i);
            if (StringUtil.peekAndEat(i, '+'))
                t = new Add(t, readExpr(i));
            else if (StringUtil.peekAndEat(i, '-')) t = new Subtract(t, readExpr(i));

            return t;
        }

        // $(expr)
        // ident
        // const
        private Expr readTerm(CharacterIterator i) throws Exception {
            // verboseln("Term @ "+i.getIndex()+" > '"+i.current()+"'");
            Expr e = null;
            char c = i.current();

            if (c == '$') {
                i.next();
                e = new Memory(embedExpr(i));
            } else if (Character.isLetter(c))
                e = readIdent(i);
            else if (Character.isDigit(c))
                e = readConst(i);
            else if (c == '-')
                e = readConst(i);
            else
                throw new Exception("invalid start of term @ " + i.getIndex());

            return e;
        }

        // ( expr )
        private Expr embedExpr(CharacterIterator i) throws Exception {
            // verboseln("Embed @ "+i.getIndex()+" > '"+i.current()+"'");
            expectChar(i, '(');
            Expr e = readExpr(i);
            expectChar(i, ')');

            return e;
        }

        // ident
        private Expr readIdent(CharacterIterator i) {
            StringBuffer buf = new StringBuffer();

            while (true) {
                char c = i.current();
                // verboseln("Ident @ "+i.getIndex()+" > "+squote(c));

                if (!Character.isLetterOrDigit(c) && c != '.') break;

                buf.append(c);
                i.next();
            }

            String name = buf.toString();
            Expr e = (Expr) known.get(name.toLowerCase());
            if (e != null) return e;
            return new Label(name);
        }

        // number
        private Expr readConst(CharacterIterator i) {
            int value = StringUtil.readDecimalValue(i, 9);
            return new Const(value);
        }

        private void expectChar(CharacterIterator i, char c) throws Exception {
            char r = i.current();
            i.next();
            if (r != c) throw new Exception("expected " + squote(c) + " @ " + (i.getIndex() - 1) + ", found " + squote(r));
        }

    }

    static class StateMismatch extends TestResult.TestFailure {
        StatePredicate predicate;
        State state;

        StateMismatch(StatePredicate pred, State st) {
            super("incorrect result: (" + pred.left + " -> " + pred.leftvalue + ") != ("
                    + pred.right + " -> " + pred.rightvalue + ")");
            state = st;
            predicate = pred;
        }

        public void shortReport() {
            ColorTerminal.print(message);
        }

        public void longReport() {
            ColorTerminal.println(message);
            state.dump();
        }
    }


    public TestCase newTestCase(String fname, Properties props) throws Exception {

        String target = props.getProperty("Target");

        if (target == null)
            return new TestCase.Malformed(fname, "must specify phase being tested");

        if (target.equals("avr-simplify"))
            return new SimplifierTest(fname, props);
        else if (target.equals("avr-sim"))
            return new SimulatorTest(fname, props);

        return new TestCase.Malformed(fname, "invalid target specification " + VPCBase.quote(target));
    }

    static class StatePredicate {
        Expr left, right;
        int leftvalue;
        int rightvalue;

        StatePredicate(Expr l, Expr r) {
            left = l;
            right = r;
        }

        public boolean check(Program p, State s) {
            verboseln("checking: " + left + " ?= " + right);
            leftvalue = left.evaluate(p, s);
            rightvalue = right.evaluate(p, s);
            return leftvalue == rightvalue;
        }
    }

}
