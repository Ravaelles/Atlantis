package atlantis.terran.repair;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class CanAbandonUnitAssignedToRepair {
    public static boolean check(AUnit unit) {
        if (!unit.isAlive()) {
            return true;
        }

        AUnit target = ARepairAssignments.getUnitToRepairFor(unit);
        if (target == null) {
            target = ARepairAssignments.getUnitToProtectFor(unit);
        }

        if (target == null || target.isNeutral() || !target.isAlive()) {
//            System.err.println("unit = " + unit);
//            System.err.println("target = " + target);
//            A.printStackTrace("WTF, why here?");
            return true;
        }

        if (target.isWounded()) {
            return false;
        }

        if (unit.isProtector() && target.isBunker() && target.enemiesNear().count() <= 1) {
            return true;
        }

        Selection enemies = target.enemiesNear().canAttack(target, 14);
        int workersNearby = target.friendsNear().workers().inRadius(1.5, target).count();

        return enemies.isEmpty() || (workersNearby >= 3 && enemies.count() < workersNearby);
//        return target.enemiesNear().canAttack(target, 14).isEmpty();
    }
}
