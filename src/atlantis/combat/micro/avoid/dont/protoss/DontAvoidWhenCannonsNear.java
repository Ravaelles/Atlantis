package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class DontAvoidWhenCannonsNear {
    public static boolean check(AUnit unit) {
        if (Count.cannons() > 0) {
            if (unit.isRanged() && unit.cooldown() >= 6) return false;

            AUnit cannon = unit.friendsNear().cannons().inRadius(maxDistToCannon(unit), unit).nearestTo(unit);
            if (cannon != null) {
                if (cannon.enemiesNear().havingAntiGroundWeapon().countInRadius(7, cannon) >= 1) {
                    if ((unit.hp() >= 40 || unit.cooldown() <= 5)) {
                        return true;
                    }
//                    else {
//                        return unit.cooldown() <= 6 && unit.combatEvalRelative() >= 1.1
//                            && (!unit.isMelee() || A.s >= 60 * 7);
//                    }
                }
            }
        }

        return false;
    }

    private static double maxDistToCannon(AUnit unit) {
        return unit.enemiesNear().ranged().inRadius(6.6, unit).notEmpty() ? 2.82 : 11;
    }
}
