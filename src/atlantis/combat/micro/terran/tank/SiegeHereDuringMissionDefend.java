package atlantis.combat.micro.terran.tank;

import atlantis.units.AUnit;
import atlantis.units.managers.Manager;
import atlantis.util.Enemy;

public class SiegeHereDuringMissionDefend extends Manager {

    public SiegeHereDuringMissionDefend(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (unit.isMissionDefendOrSparta() && unit.distToFocusPoint() <= minDist()) {
            if (unit.target() == null || unit.target().distTo(unit) < 12) {
                return usedManager(this);
            }
        }

        return null;
    }

    private double minDist() {
        return Enemy.terran() ? 3 : 5;
    }
}