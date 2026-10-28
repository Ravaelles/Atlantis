package atlantis.production.dynamic.protoss.tech;

import atlantis.game.A;
import atlantis.information.generic.Army;

public class TooWeakToTech {
    public static boolean check() {
        return Army.strength() < 90
            && A.seconds() <= 600
            && A.minerals() <= 700;
    }
}
