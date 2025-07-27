package atlantis.combat.micro.avoid.buildings;

import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ShouldAvoidBunkerAsProtoss {
    private static AUnit bunker;
    private static double dist;

    public static Decision shouldAvoid(AUnit unit, AUnit bunker) {
        ShouldAvoidBunkerAsProtoss.bunker = bunker;
        dist = unit.distTo(bunker);

        if (veryPowerfulLetsBetterEngage(unit)) {
            return Decision.FALSE("veryPowerfulLetsBetterEngage");
        }

        if (needToAvoidDueToSecondBunkerNearby(unit, bunker)) {
            return Decision.TRUE("needToAvoidDueToSecondBunkerNearby");
        }

        if (dontAvoidGoodToAttack(unit)) {
            return Decision.FALSE("dontAvoidGoodToAttack");
        }

        return dist >= minDist(unit) ? Decision.FALSE("tooFar") : Decision.TRUE("tooClose");
    }

    private static double minDist(AUnit unit) {
        return 8.6
            + (unit.woundPercent() / 30.0)
            + enemyTanksDistancePenalty(unit);
    }

    private static double enemyTanksDistancePenalty(AUnit unit) {
        Selection tanks = unit.enemiesNear().tanks();
        if (tanks.empty()) return 0;

        return tanks.count() / 2.0;
    }

    private static boolean veryPowerfulLetsBetterEngage(AUnit unit) {
        return A.supplyUsed() >= 190 || A.minerals() >= 1500 || unit.friendsNear().combatUnits().atLeast(25);
    }

    public static boolean dontAvoidGoodToAttack(AUnit unit) {
        double eval = unit.eval();

        if (eval <= 1.5) return false;

        if (eval >= 2.5 || unit.lastUnderAttackMoreThanAgo(30 * 9) || unit.shieldWound() <= 8) {
//            if (unit.lastUnderAttackLessThanAgo(110)) return false;
//            if (unit.shieldWound() >= 5) return false;
            return true;
        }

        return false;
    }


    private static boolean needToAvoidDueToSecondBunkerNearby(AUnit unit, AUnit bunker) {
        if (bunker.friendsNear().bunkers().countInRadius(6, bunker) == 0) return false;

        return A.supplyUsed() >= 170 || unit.friendsNear().combatUnits().atLeast(30);
    }
}
