package atlantis.production.dynamic.protoss.tech;

import atlantis.game.A;
import atlantis.information.generic.OurArmy;

public class TooWeakToTech {
    public static boolean check() {
        return OurArmy.strength() < 90
            && A.seconds() <= 600
            && A.minerals() <= 700;
    }
}
