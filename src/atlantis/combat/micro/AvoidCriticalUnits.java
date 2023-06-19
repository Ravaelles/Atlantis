package atlantis.combat.micro;

import atlantis.units.AUnit;

public class AvoidCriticalUnits {

    public static boolean update(AUnit unit) {
        if (lurkers(unit)) {
            return true;
        }

        return false;
    }

    private static boolean lurkers(AUnit unit) {
        AUnit lurker = unit.enemiesNear().lurkers().effUndetected().inRadius(7.7, unit).nearestTo(unit);

        if (lurker == null) {
            return false;
        }

        unit.runningManager().runFromAndNotifyOthersToMove(lurker);
        unit.setTooltipTactical("AVOID-LURKER");
        return true;
    }
}
