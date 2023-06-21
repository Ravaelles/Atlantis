package atlantis.combat.micro.avoid.special;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;

public class AvoidCriticalUnits {

    public static boolean update(AUnit unit) {
        if (SuicideAgainstScarabs.update(unit)) {
            return true;
        }

        if (avoidLurkers(unit)) {
            return true;
        }

        if (avoidReavers(unit)) {
            return true;
        }

        if (avoidDT(unit)) {
            return true;
        }

        return false;
    }

    private static boolean avoidReavers(AUnit unit) {
        if (unit.isAir() || unit.isBuilding()) {
            return false;
        }

        AUnit reaver = unit.enemiesNear().reavers().effUndetected().inRadius(9.4, unit).nearestTo(unit);
        if (reaver == null) {
            return false;
        }

        Selection friendsNear = unit.friendsNear().combatUnits();
        if (
            friendsNear.inRadius(4, unit).atLeast(5) && friendsNear.inRadius(6, unit).atLeast(8)
        ) {
            return false;
        }

        unit.runningManager().runFromAndNotifyOthersToMove(reaver, "REAVER!");
        return true;
    }

    private static boolean avoidDT(AUnit unit) {
        if (unit.isAir() || unit.isBuilding()) {
            return false;
        }

        AUnit dt = unit.enemiesNear().ofType(AUnitType.Protoss_Dark_Templar).effUndetected()
            .inRadius(2.5, unit).nearestTo(unit);
        if (dt == null) {
            return false;
        }

        unit.runningManager().runFromAndNotifyOthersToMove(dt, "DT!");
        return true;
    }

    private static boolean avoidLurkers(AUnit unit) {
        if (unit.isAir() || unit.isBuilding()) {
            return false;
        }

        AUnit lurker = unit.enemiesNear().lurkers().effUndetected().inRadius(7.7, unit).nearestTo(unit);
        if (lurker == null) {
            return false;
        }

        unit.runningManager().runFromAndNotifyOthersToMove(lurker, "LURKER!");
        return true;
    }

}
