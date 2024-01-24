package atlantis.units.fix;

import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.AvoidEnemiesIfNeeded;
import atlantis.units.AUnit;
import bwapi.Color;

public class DoPreventLogic {
    public static boolean handle(AUnit unit) {
//        if (unit.lastUnderAttackLessThanAgo(30 * 3)) {
//            unit.paintCircleFilled(22, Color.Orange);
//        }
        AvoidEnemiesIfNeeded avoidEnemies = new AvoidEnemiesIfNeeded(unit);
        avoidEnemies.forceAvoid = true;

        if (avoidEnemies.avoidEnemiesIfNeeded() != null) {
            unit.paintCircleFilled(22, Color.Orange);
            return true;
        }

        System.err.println("Interesting - fight!?");
        if ((new AttackNearbyEnemies(unit)).forceHandle() != null) return true;

        return false;
    }
}
