package atlantis.combat.micro.avoid.buildings;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.We;

public class DontAvoidBunker {
    public static boolean dontAvoid(AUnit unit, AUnit bunker, double dist) {
        if (!bunker.isBunker()) return false;

        if (We.protoss()) {
            if (needToAvoidDueToSecondBunkerNearby(unit, bunker)) return false;

            if (unit.isRanged() || unit.lastUnderAttackMoreThanAgo(30 * 9)) {
                if (unit.lastUnderAttackLessThanAgo(110)) return false;
                if (unit.shieldWound() >= 5) return false;
                return true;
            }
        }

        return dist >= (7.1 + unit.woundPercent() / 30.0);
    }

    private static boolean needToAvoidDueToSecondBunkerNearby(AUnit unit, AUnit bunker) {
        if (bunker.friendsNear().bunkers().countInRadius(6, bunker) == 0) return false;

        return A.supplyUsed() >= 170 || unit.friendsNear().combatUnits().atLeast(30);
    }
}
