package avrora.sim;

import avrora.core.Program;
import avrora.core.Instr;
import avrora.core.InstrPrototype;

/**
 * The <code>Microcontroller</code> interface corresponds to a hardware device
 * that implements the AVR instruction set. This interface contains methods that
 * get commonly needed information about the particular hardware device and
 * and can load programs onto this virtual device.
 * @author Ben L. Titzer
 */
public interface Microcontroller {

    /**
     * The <code>getRamSize()</code> method returns the number of bytes of
     * SRAM present on this hardware device. For example, on the Atmega128L,
     * this number is 4096. On the Atmega103, this number is 4000.
     * @return the number of bytes of SRAM on this hardware device
     */
    public int getRamSize();

    /**
     * The <code>getIORegSize()</code> method returns the number of IO registers
     * that are present on this hardware device. For example, on the Atmega128L,
     * this number is 224. On the Atmega103, this number is 64.
     * @return the number of IO registers supported on this hardware device
     */
    public int getIORegSize();

    /**
     * The <code<getFlashSize()</code> method returns the size in bytes of
     * the flash memory on this hardware device. The flash memory stores the
     * initialized data and the machine code instructions of the program. On
     * the Atmega128L, this number is 128K.
     * @return the size of the flash memory in bytes
     */
    public int getFlashSize();

    /**
     * The <code>getEEPromSize()</code> method returns the size in bytes of
     * the EEPROM on this hardware device. On the ATmega128L, this number is
     * 4096.
     * @return the size of the EEPROM in bytes
     */
    public int getEEPromSize();

    /**
     * The <code>getHZ()</code> method returns the number of cycles per second
     * at which this hardware device is designed to run. For example, the
     * Atmega128L runs at 16MHz, so this method will return 16,000,000.
     * @return the number of cycles per second on this device
     */
    public int getHz();

    /**
     * The <code>millisToCycles()</code> method converts the specified number
     * of milliseconds to a cycle count. The conversion factor used is the
     * number of cycles per second of this device. This method serves as a
     * utility so that clients need not do repeated work in converting
     * milliseconds to cycles and back.
     * @param ms a time quantity in milliseconds as a double
     * @return the same time quantity in clock cycles, rounded up to the nearest
     * integer
     */
    public long millisToCycles(double ms);

    /**
     * The <code>cyclesToMillis()</code> method converts the specified number
     * of cycles to a time quantity in milliseconds. The conversion factor used
     * is the number of cycles per second of this device. This method serves
     * as a utility so that clients need not do repeated work in converting
     * milliseconds to cycles and back.
     * @param cycles the number of cycles
     * @return the same time quantity in milliseconds
     */
    public double cyclesToMillis(long cycles);

    /**
     * The <code>isSupported()</code> method allows a client to query whether
     * a particular instruction is implemented on this hardware device. Older
     * implementations of the AVR instruction set preceded the introduction
     * of certain instructions, and therefore did not support the new
     * instructions.
     * @param i the instruction prototype of the instruction
     * @return true if the specified instruction is supported on this device;
     * false otherwise
     */
    public boolean isSupported(InstrPrototype i);

    /**
     * The <code>loadProgram()</code> method is used to instantiate a simulator
     * for the particular program. It will construct an instance of the
     * <code>Simulator</code> class that has all the properties of this hardware
     * device and has been initialized with the specified program.
     * @param p the program to load onto the simulator
     * @return a <code>Simulator</code> instance that is capable of simulating
     * the hardware device's behavior on the specified program
     */
    public Simulator loadProgram(Program p);
}
