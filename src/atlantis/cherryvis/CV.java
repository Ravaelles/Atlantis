package atlantis.cherryvis;

import atlantis.units.AUnit;

public class CV {
    public static boolean globalLog(String message) {
        if (!ACherryVis.isEnabled()) return true;

        ACherryVis.logger().log(message);

        return true;
    }

    public static boolean unitManager(String message, AUnit unit) {
        if (!ACherryVis.isEnabled()) return true;

        ACherryVis.logger().unitManager(message, unit);

        return true;
    }
}
