package avrora.test;

import avrora.test.TestCase;

import java.util.Properties;

/**
 * The <code>TestHarness</code> interface encapsulates the notion of a testing
 * harness that is capable of creating the correct type of test cases given
 * the file and a list of properties extracted from the file by the automated
 * testing framework.
 *
 * @author Ben L. Titzer
 */
public interface TestHarness {

    /**
     * The <code>newTestCase()</code> method creates a new test case of the
     * right type given the file name and the properties already extracted
     * from the file by the testing framework.
     * @param fname the name of the file
     * @param props a list of properties extracted from the file
     * @return an instance of the <code>TestCase</code> class
     * @throws Exception if there is a problem creating the testcase or reading it
     */
    public TestCase newTestCase(String fname, Properties props) throws Exception;
}
