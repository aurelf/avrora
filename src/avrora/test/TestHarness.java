package avrora.test;

import avrora.test.TestCase;

import java.util.Properties;

/**
 * @author Ben L. Titzer
 */
public interface TestHarness {
    public TestCase newTestCase(String fname, Properties props) throws Exception;
}
