package avrora.test;

import avrora.test.TestHarness;
import avrora.test.TestCase;
import avrora.test.TestResult;
import avrora.util.Terminal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class AutomatedTester {

    private final TestHarness harness;

    public AutomatedTester(TestHarness th) {
        harness = th;
    }

    private class TestPair {
        final TestCase testcase;
        final TestResult result;

        TestPair(TestCase tc, TestResult tr) {
            testcase = tc;
            result = tr;
        }
    }

    private static final int YELLOW = 0;
    private static final int RED = 1;
    private static final int CYAN = 2;
    private static final int GREEN = 3;

    public void runTests(String[] fnames) throws java.io.IOException {
        List slist = new LinkedList();
        List flist = new LinkedList();
        List ilist = new LinkedList();
        List ulist = new LinkedList();
        List mlist = new LinkedList();

        for (int cntr = 0; cntr < fnames.length; cntr++) {
            String fname = fnames[cntr];
            TestPair pair = runTest(fname);
            TestResult result = pair.result;

            if (result.isSuccess())
                slist.add(pair);
            else if (result.isInternalError())
                ilist.add(pair);
            else if (result.isUnexpectedException())
                ulist.add(pair);
            else if (result.isMalformed())
                mlist.add(pair);
            else
                flist.add(pair);
        }

        Terminal.printBrightGreen("Test successes");
        Terminal.println(": " + slist.size() + " of " + fnames.length);

        report(ilist, "Internal errors", YELLOW, fnames.length);
        report(ulist, "Unexpected exceptions", YELLOW, fnames.length);
        report(flist, "Failures", RED, fnames.length);
        report(mlist, "Malformed test cases", CYAN, fnames.length);

    }

    private static void report(List l, String c, int color, int total) {
        if (l.isEmpty()) return;

        switch (color) {
            case YELLOW:
                Terminal.printYellow(c);
                break;
            case RED:
                Terminal.printRed(c);
                break;
            case GREEN:
                Terminal.printBrightGreen(c);
                break;
            case CYAN:
                Terminal.printBrightCyan(c);
                break;
            default:
                Terminal.print(c);
        }

        Terminal.println(": " + l.size() + " of " + total);
        Iterator i = l.iterator();
        while (i.hasNext()) {
            TestPair p = (TestPair) i.next();
            report(p.testcase.getFileName(), p.result);
        }
    }

    public static void report(String fname, TestResult result) {
        Terminal.print("  ");
        Terminal.printRed(fname);
        Terminal.print(": ");
        result.shortReport();
        Terminal.print("\n");
    }

    public TestPair runTest(String fname) throws java.io.IOException {
        TestCase tc = readTestCase(fname);
        Throwable exception = null;

        try {
            tc.run();
        } catch (Throwable t) {
            exception = t;
        }

        return new TestPair(tc, tc.match(exception));
    }

    private TestCase readTestCase(String fname) throws java.io.IOException {
        BufferedReader r = new BufferedReader(new FileReader(fname));
        Properties vars = new Properties();

        while (true) {
            // TODO: could use some cleanup with StringUtil

            String buffer = r.readLine();
            if (buffer == null) break;

            int index = buffer.indexOf("@");
            if (index < 0) break;

            int index2 = buffer.indexOf(":");
            if (index2 < 0) break;

            String var = buffer.substring(index + 1, index2).trim();
            String val = buffer.substring(index2 + 1).trim();

            vars.put(var, val);
        }

        r.close();

        String expect = vars.getProperty("Result");

        if (expect == null)
            return new TestCase.Malformed(fname, "no result specified");
        else {
            try {
                return harness.newTestCase(fname, vars);
            } catch (Exception e) {
                e.printStackTrace();
                return new TestCase.Malformed(fname, "exception in test case initialization");
                // TODO: MalformedTestCase exception?
            }
        }
    }

}
