package avrora.sim.radio;

import avrora.sim.mcu.Microcontroller;
import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;

/**
 * The <code>Radio</code> interface should be implemented by classes which would like to act as radios and access an
 * instance of the <code>RadioAir</code> interface.
 * @author Daniel Lee
 */
public interface Radio {

    /** Time in ATMega128L cycles it takes for one byte to be sent over the air.
     * Much of the implementation is derived from this constant, so generalizing in the
     * future may require some careful consideration. */
    public final static long TRANSFER_TIME = 6106;

    /* How 6106 was calculated:
        19.2 Manchester kBaud / (2 Baud/Bit)= 9.6 kBits/sec
        (9600 bits/sec) / (8 bits/byte) = 1150 bytes/sec
        (7327800 cycles/sec) / (1150 bytes/sec) = 6106 cycles/byte (rounded)
     */


    /** A <code>RadioPacket</code> is an object describing the data transmitted over <code>RadioAir</code> over some
     * period of time. */
    public class RadioPacket implements Comparable {

        public final byte data;
        public final long frequency;

        public final Long origination;
        public final Long delivery;


        public int strength = 0x3ff;

        public RadioPacket(byte data, long frequency, long origination) {
            this.data = data;
            this.frequency = frequency;
            this.origination = new Long(origination);
            this.delivery = new Long(origination + TRANSFER_TIME);
        }

        public int compareTo(Object o) {
            if (o instanceof RadioPacket) {
                RadioPacket p = (RadioPacket) o;
                return origination.compareTo(p.origination);
            } else {
                return -1;
            }
        }

    }

    /** A <code>RadioController</code> is an object installed into a Microcontroller. The recommended implementation
     * is to implement specialized IO registers as inner classes and install them into the Microcontroller. Changes to
     * these specialized registers should initiate appropriate behavior with the radio.  */
    public interface RadioController {

        /** Installs this Controller into a microcontroller. This should setup the pins, IO registers in such a way
         * that changes to CPU state will make corresponding changes to the RadioController state that will initiate
         * sends and receives if necessary. */
        public void install(Microcontroller mcu);

        /** Enable transfers. */
        public void enable();

        /** Disable transfers. */
        public void disable();
    }

    /** Receive a frame from the air. Should be called by the <code>RadioAir</code> and pass data into the
     * <code>RadioController</code>. */
    public void receive(RadioPacket f);


    /** Transmit a frame from the controller. Should be called by the <code>RadioController</code> and transmitted into
     * the <code>RadioAir</code>. */
    public void transmit(RadioPacket f);

    /** Get the <code>Simulator</code> on which this radio is running.*/
    public Simulator getSimulator();

    /** Get the <code>SimulatorThread</code> thread on which this radio is running. */
    public SimulatorThread getSimulatorThread();

    /** Set the <code>SimulatorThread</code> of this radio.*/
    public void setSimulatorThread(SimulatorThread thread);

}
