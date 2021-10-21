package atlantis;

import atlantis.units.AUnit;
import atlantis.units.Select;

public class OnEveryFrame {

    public static void update() {
        Atlantis.getInstance().getGameCommander().update();
    }

}
