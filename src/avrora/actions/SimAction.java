package avrora.actions;

import avrora.sim.mcu.MicrocontrollerFactory;
import avrora.sim.mcu.Microcontrollers;
import avrora.sim.platform.PlatformFactory;
import avrora.sim.platform.Platforms;
import avrora.Avrora;
import avrora.util.StringUtil;
import avrora.util.Option;

/**
 * @author Ben L. Titzer
 */
public abstract class SimAction extends Action {
    public final Option.Long ICOUNT = newOption("icount", 0,
            "This option is used to terminate the " +
            "simulation after the specified number of instructions have been executed. " +
            "It is useful for non-terminating programs.");
    public final Option.Long TIMEOUT = newOption("timeout", 0,
            "This option is used to terminate the " +
            "simulation after the specified number of clock cycles have passed. " +
            "It is useful for non-terminating programs and benchmarks.");
    public final Option.Str CHIP = newOption("chip", "atmega128l",
            "This option selects the microcontroller from a library of supported " +
            "microcontroller models.");
    public final Option.Str PLATFORM = newOption("platform", "",
            "This option selects the platform on which the microcontroller is built, " +
            "including the external devices such as LEDs and radio. If the platform " +
            "option is not set, the default platform is the microcontroller specified " +
            "in the \"chip\" option, with no external devices.");

    protected SimAction(String sn, String h) {
        super(sn, h);
    }

    /**
     * The <code>getMicrocontroller()</code> method is used to get the current
     * microcontroller from the library of implemented ones, based on the
     * command line option that was specified (-chip=xyz).
     * @return an instance of <code>MicrocontrollerFactory</code> for the
     * microcontroller specified on the command line.
     */
    protected MicrocontrollerFactory getMicrocontroller() {
        MicrocontrollerFactory mcu = Microcontrollers.getMicrocontroller(CHIP.get());
        if (mcu == null)
            Avrora.userError("Unknown microcontroller", StringUtil.quote(CHIP.get()));
        return mcu;
    }

    /**
     * The <code>getPlatform()</code> method is used to get the current
     * platform from the library of implemented ones, based on the command
     * line option that was specified (-platform=xyz).
     * @return an instance of <code>PlatformFactory</code> for the
     * platform specified on the command line
     */
    protected PlatformFactory getPlatform() {
        String pf = PLATFORM.get();
        if (pf.equals("")) return null;
        PlatformFactory pff = Platforms.getPlatform(pf);
        if (pff == null)
            Avrora.userError("Unknown platform", StringUtil.quote(pf));
        return pff;
    }
}
