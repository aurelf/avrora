package avrora.sim.platform;

import java.util.HashMap;

/**
 * @author Ben L. Titzer
 */
public class Platforms {

    private static final HashMap platforms = new HashMap();

    static {
        platforms.put("mica", new Mica());
    }

    public static PlatformFactory getPlatform(String name) {
        return (PlatformFactory) platforms.get(name);
    }
}
