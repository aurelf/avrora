package avrora.sim.radio;

import avrora.sim.mcu.Microcontroller;

/**
 * Interface for the <code>RadioAir</code>. An implementation of this interface should
 * provide the policies through which radio transmission is handled. Radios should transmit
 * via the transmit method. The air should deliver packets to the radio through the receive()
 * method in the <code>Radio</code> interface.
 * @author Daniel Lee
 */
public interface RadioAir {

    /** Add a radio to the environment. */
    public void addRadio(Radio r);

    /** Remove a radio from the environment. */
    public void removeRadio(Radio r);

    /** Transmits frame <code>f</code> into the radio environment.  */
    public void transmit(Radio r, Radio.RadioPacket f);

    /** Determines whether this is a message currently in the air. */
    public boolean messageInAir();
}
