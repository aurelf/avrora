package avrora.sim.platform;

import avrora.core.Program;

/**
 * @author Ben L. Titzer
 */
public interface PlatformFactory {

    public Platform newPlatform(Program p);

}
