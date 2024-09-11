package atlantis.units.detected;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.cache.Cache;

public class IsOurUnitUndetected {
    private static Cache<Boolean> cache = new Cache<>();

    public static boolean check(AUnit unit) {
        if (!unit.isCloaked() && !unit.isBurrowed()) return false;
        if (unit.lastUnderAttackLessThanAgo(30 * 4)) return false;

        return cache.get(unit.idWithHash(), 7, () -> isUndetected(unit));
    }

    private static boolean isUndetected(AUnit unit) {
        int lastCloakedAgo = Math.min(unit.lastActionAgo(Actions.CLOAK), unit.lastActionAgo(Actions.BURROW)) + 30;

        if (unit.lastUnderAttackLessThanAgo(lastCloakedAgo)) {
//            System.err.println("Our unit " + unit + " is under attack, so it's not undetected. / cloaked = " + unit.isCloaked());
            return false;
        }

        double range = 11.8;
        if (unit.enemiesNear().detectors().visibleOnMap().inRadius(range, unit).notEmpty()) return false;

        return true;
    }
}
