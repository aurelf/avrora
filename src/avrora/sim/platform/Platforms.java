package avrora.sim.platform;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: titzer
 * Date: Apr 5, 2004
 * Time: 8:54:21 PM
 * To change this template use Options | File Templates.
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
