package atlantis.units.interrupt;

import atlantis.units.AUnit;

public class PreventAttacksDuringRetreat {
    public static boolean prevent(AUnit unit) {
        if (!unit.isRetreating()) return false;

        return !isAttackingCrucialDelicateUnit(unit.target());
    }

    private static boolean isAttackingCrucialDelicateUnit(AUnit target) {
        if (target == null) return false;
        if (target.isGroundUnit()) return false;

        return target.isScourge()
                || target.isScienceVessel()
                || target.isWraith()
                || target.isObserver();
    }
}
