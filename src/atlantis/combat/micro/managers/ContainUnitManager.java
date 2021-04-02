package atlantis.combat.micro.managers;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.MissionUnitManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;

public class ContainUnitManager extends MissionUnitManager {
    private Mission mission;

    public boolean updateUnit(AUnit unit) {
        unit.setTooltip("#Contain");

        if (mission.focusPoint() == null) {
            return false;
        }

        if (handleComeCloserToChokepoint(unit)) {
            return true;
        }

        return false;
    }

    // =========================================================

    private boolean handleComeCloserToChokepoint(AUnit unit) {
        if (unit.distanceTo(mission.focusPoint()) > optimalDistance()) {
            return true;
        }

        return false;
    }

    private double optimalDistance() {
        return 6.9;
    }

}