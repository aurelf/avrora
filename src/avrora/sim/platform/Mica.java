package avrora.sim.platform;

import avrora.sim.mcu.Microcontroller;
import avrora.sim.mcu.ATMega128L;
import avrora.core.Program;
import avrora.util.Terminal;

/**
 * @author Ben L. Titzer
 */
public class Mica implements Platform, PlatformFactory {

    protected final Microcontroller mcu;

    public Mica() {
        mcu = null;
    }

    private Mica(Microcontroller m) {
        mcu = m;
        addDevices();
    }

    public Microcontroller getMicrocontroller() {
        return mcu;
    }

    public Platform newPlatform(Program p) {
        return new Mica(new ATMega128L(true).newMicrocontroller(p));
    }

    protected class LED implements Microcontroller.Pin.Output {
        protected boolean initialized;
        protected boolean on;

        protected final int colornum;
        protected final String color;

        protected LED(int n, String c) {
            colornum = n;
            color = c;
        }

        public void write(boolean level) {
            if (!initialized) {
                initialized = true;
                on = level;
                print();
            } else {
                if (level != on) {
                    on = level;
                    print();
                }
            }
        }

        public void print() {
            Terminal.print(colornum, color);
            Terminal.println(": " + (on ? "on" : "off"));
        }

        public void enableOutput() {
            // do nothing
        }

        public void disableOutput() {
            // do nothing
        }
    }

    protected void addDevices() {
        mcu.getPin("PA0").connect(new LED(Terminal.COLOR_YELLOW, "Yellow"));
        mcu.getPin("PA1").connect(new LED(Terminal.COLOR_GREEN, "Green"));
        mcu.getPin("PA2").connect(new LED(Terminal.COLOR_RED, "Red"));
    }
}
