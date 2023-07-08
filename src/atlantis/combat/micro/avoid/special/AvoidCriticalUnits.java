package atlantis.combat.micro.avoid.special;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;

public class AvoidCriticalUnits {

    public  boolean update() {
        if (SuicideAgainstScarabs.update()) {
            return true;
        }

        if (avoidLurkers()) {
            return true;
        }

        if (avoidReavers()) {
            return true;
        }

        if (avoidDT()) {
            return true;
        }

        if (avoidGuardian()) {
            return true;
        }

        return false;
    }

    private  boolean avoidReavers() {
        if (unit.isAir() || unit.isBuilding()) {
            return false;
        }

        AUnit reaver = unit.enemiesNear().reavers().effUndetected().inRadius(9.4, unit).nearestTo();
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

    private  boolean avoidDT() {
        if (unit.isAir() || unit.isBuilding()) {
            return false;
        }

        AUnit dt = unit.enemiesNear().ofType(AUnitType.Protoss_Dark_Templar).effUndetected()
            .inRadius(2.5, unit).nearestTo();
        if (dt == null) {
            return false;
        }

        unit.runningManager().runFromAndNotifyOthersToMove(dt, "DT!");
        return true;
    }

    private  boolean avoidLurkers() {
        if (unit.isAir() || unit.isBuilding()) {
            return false;
        }

        AUnit lurker = unit.enemiesNear().lurkers().effUndetected().inRadius(7.7, unit).nearestTo();
        if (lurker == null) {
            return false;
        }

        unit.runningManager().runFromAndNotifyOthersToMove(lurker, "LURKER!");
        return true;
    }

    private  boolean avoidGuardian() {
        if (unit.isAir() || unit.isBuilding() || unit.canAttackAirUnits()) {
            return false;
        }

        AUnit lurker = unit.enemiesNear().lurkers().effUndetected().inRadius(7.7, unit).nearestTo();
        if (lurker == null) {
            return false;
        }

        unit.runningManager().runFromAndNotifyOthersToMove(lurker, "LURKER!");
        return true;
    }

}
