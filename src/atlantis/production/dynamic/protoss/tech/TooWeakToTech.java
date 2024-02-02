package atlantis.production.dynamic.protoss.tech;

import atlantis.game.A;
import atlantis.information.generic.OurArmyStrength;

public class TooWeakToTech {
    public static boolean check() {
        return OurArmyStrength.relative() < 90
            && A.seconds() <= 600
            && A.minerals() <= 700;
    }
}
