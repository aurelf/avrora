package avrora.sim.mcu;

import java.util.HashMap;

/**
 * The <code>Microcontrollers</code> class represents a static, known mapping between
 * names and implementations of microcontroller models. For example, "atmega128l" is
 * mapped to an instance of the <code>ATMega128L</code> class.
 *
 * @author Ben L. Titzer
 */
public class Microcontrollers {

    private static final HashMap mcus = new HashMap();

    static {
        mcus.put("atmega128l", new ATMega128L());
    }

    /**
     * The <code>getMicrocontroller</code> method retrieves an instance of
     * the <code>Microcontroller</code> interface that represents the
     * named microcontroller.
     *
     * @param name the name of the microcontroller
     * @return an instance of the <code>Microcontroller</code> interface
     *         representing the hardware device if implemented; null otherwise
     */
    public static MicrocontrollerFactory getMicrocontroller(String name) {
        return (MicrocontrollerFactory) mcus.get(name.toLowerCase());
    }
}
