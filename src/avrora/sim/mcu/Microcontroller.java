package avrora.sim.mcu;

import avrora.core.InstrPrototype;
import avrora.core.Program;
import avrora.sim.Simulator;

/**
 * The <code>Microcontroller</code> interface corresponds to a hardware device
 * that implements the AVR instruction set. This interface contains methods that
 * get commonly needed information about the particular hardware device and
 * and can load programs onto this virtual device.
 *
 * @author Ben L. Titzer
 */
public interface Microcontroller extends MicrocontrollerProperties {

    /**
     * The <code>Pin</code> interface encapsulates the notion of a physical
     * pin on the microcontroller chip. It is generally used in wiring up
     * external devices to the microcontroller.
     *
     * @author Ben L. Titzer
     */
    public interface Pin {
        /**
         * The <code>Input</code> interface represents an input pin. When the
         * pin is configured to be an input and the microcontroller attempts
         * to read from this pin, the installed instance of this interface
         * will be called.
         */
        public interface Input {
            public void enableInput();

            public void disableInput();

            public boolean read();
        }

        /**
         * The <code>Output</code> interface represents an output pin. When the
         * pin is configured to be an output and the microcontroller attempts
         * to wrote to this pin, the installed instance of this interface
         * will be called.
         */
        public interface Output {
            public void enableOutput();

            public void disableOutput();

            public void write(boolean level);
        }

        /**
         * The <code>connect()</code> method will connect this pin to the
         * specified input. Attempts by the microcontroller to read from this
         * pin when it is configured as an input will then call this instance's
         * <code>read()</code> method.
         *
         * @param i the <code>Input</code> instance to connect to
         */
        public void connect(Input i);

        /**
         * The <code>connect()</code> method will connect this pin to the
         * specified output. Attempts by the microcontroller to write to this
         * pin when it is configured as an output will then call this instance's
         * <code>read()</code> method.
         *
         * @param o the <code>Output</code> instance to connect to
         */
        public void connect(Output o);
    }

    /**
     * The <code>getSimulator()</code> method gets a simulator instance that is
     * capable of emulating this hardware device.
     *
     * @return a <code>Simulator</code> instance corresponding to this
     *         device
     */
    public Simulator getSimulator();

    /**
     * The <code>getPin()</code> method looks up the named pin and returns a
     * reference to that pin. Names of pins should be UPPERCASE. The intended
     * users of this method are external device implementors which connect
     * their devices to the microcontroller through the pins.
     *
     * @param name the name of the pin; for example "PA0" or "OC1A"
     * @return a reference to the <code>Pin</code> object corresponding to
     *         the named pin if it exists; null otherwise
     */
    public Pin getPin(String name);

    /**
     * The <code>getPin()</code> method looks up the specified pin by its number
     * and returns a reference to that pin. The intended
     * users of this method are external device implementors which connect
     * their devices to the microcontroller through the pins.
     *
     * @param num the pin number to look up
     * @return a reference to the <code>Pin</code> object corresponding to
     *         the named pin if it exists; null otherwise
     */
    public Pin getPin(int num);
}
