package avrora.sim.mcu;

import avrora.core.InstrPrototype;

/**
 * @author Ben L. Titzer
 */
public interface MicrocontrollerProperties {
    /**
     * The <code>getRamSize()</code> method returns the number of bytes of
     * SRAM present on this hardware device. For example, on the Atmega128L,
     * this number is 4096. On the Atmega103, this number is 4000.
     *
     * @return the number of bytes of SRAM on this hardware device
     */
    int getRamSize();

    /**
     * The <code>getIORegSize()</code> method returns the number of IO registers
     * that are present on this hardware device. For example, on the Atmega128L,
     * this number is 224. On the Atmega103, this number is 64.
     *
     * @return the number of IO registers supported on this hardware device
     */
    int getIORegSize();

    /**
     * The <code<getFlashSize()</code> method returns the size in bytes of
     * the flash memory on this hardware device. The flash memory stores the
     * initialized data and the machine code instructions of the program. On
     * the Atmega128L, this number is 128K.
     *
     * @return the size of the flash memory in bytes
     */
    int getFlashSize();

    /**
     * The <code>getEEPromSize()</code> method returns the size in bytes of
     * the EEPROM on this hardware device. On the ATmega128L, this number is
     * 4096.
     *
     * @return the size of the EEPROM in bytes
     */
    int getEEPromSize();

    /**
     * The <code>getHZ()</code> method returns the number of cycles per second
     * at which this hardware device is designed to run.
     *
     * @return the number of cycles per second on this device
     */
    int getHz();

    /**
     * The <code>millisToCycles()</code> method converts the specified number
     * of milliseconds to a cycle count. The conversion factor used is the
     * number of cycles per second of this device. This method serves as a
     * utility so that clients need not do repeated work in converting
     * milliseconds to cycles and back.
     *
     * @param ms a time quantity in milliseconds as a double
     * @return the same time quantity in clock cycles, rounded up to the nearest
     *         integer
     */
    long millisToCycles(double ms);

    /**
     * The <code>cyclesToMillis()</code> method converts the specified number
     * of cycles to a time quantity in milliseconds. The conversion factor used
     * is the number of cycles per second of this device. This method serves
     * as a utility so that clients need not do repeated work in converting
     * milliseconds to cycles and back.
     *
     * @param cycles the number of cycles
     * @return the same time quantity in milliseconds
     */
    double cyclesToMillis(long cycles);

    /**
     * The <code>isSupported()</code> method allows a client to query whether
     * a particular instruction is implemented on this hardware device. Older
     * implementations of the AVR instruction set preceded the introduction
     * of certain instructions, and therefore did not support the new
     * instructions.
     *
     * @param i the instruction prototype of the instruction
     * @return true if the specified instruction is supported on this device;
     *         false otherwise
     */
    boolean isSupported(InstrPrototype i);

    /**
     * The <code>getPinNumber()</code> method looks up the named pin and returns
     * its number. Names of pins should be UPPERCASE. The intended
     * users of this method are external device implementors which connect
     * their devices to the microcontroller through the pins.
     *
     * @param name the name of the pin; for example "PA0" or "OC1A"
     * @return the number of the pin if it exists; -1 otherwise
     */
    int getPinNumber(String name);
}