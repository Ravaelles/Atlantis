package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class SiegeHereDuringMissionDefend extends Manager {
    public SiegeHereDuringMissionDefend(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTank();
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