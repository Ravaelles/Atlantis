package atlantis.combat.targeting.generic;

import atlantis.units.AUnit;
import atlantis.util.We;

public class ShouldTargetClosestEnemy {
    public static boolean check(AUnit unit) {
        if (We.protoss() && unit.hp() <= 22) return true;

        return unit.hp() <= 20;
    }
}
