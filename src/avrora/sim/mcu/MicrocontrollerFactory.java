package avrora.sim.mcu;

import avrora.core.InstrPrototype;
import avrora.core.Program;
import avrora.sim.Simulator;

/**
 * The <code>Microcontroller</code> interface corresponds to a hardware device
 * that implements the AVR instruction set. This interface contains methods that
 * get commonly needed information about the particular hardware device and
 * and can load programs onto this virtual device.
 * @author Ben L. Titzer
 */
public interface MicrocontrollerFactory extends MicrocontrollerProperties {

    /**
     * The <code>newMicrocontroller()</code> method is used to instantiate a
     * microcontroller instance for the particular program. It will construct
     * an instance of the <code>Simulator</code> class that has all the
     * properties of this hardware device and has been initialized with the
     * specified program.
     * @param p the program to load onto the microcontroller
     * @return a <code>Microcontroller</code> instance that represents the
     * specific hardware device with the program loaded onto it
     */
    public Microcontroller newMicrocontroller(Program p);

}
