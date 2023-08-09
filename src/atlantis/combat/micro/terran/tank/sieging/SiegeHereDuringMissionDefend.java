package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class SiegeHereDuringMissionDefend extends Manager {
    public SiegeHereDuringMissionDefend(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return Missions.isGlobalMissionDefend() && unit.isTank();
    }

    protected Manager handle() {
        if (unit.isMissionDefendOrSparta() && unit.distToFocusPoint() <= minDist()) {
            if (unit.target() == null || unit.target().distTo(unit) < 12) {
                unit.siege();
                unit.setTooltipAndLog("SiegeHereDuringMissionDefend");
                return usedManager(this);
            }
        }

        return null;
    }

    private double minDist() {
        return Enemy.terran() ? 3 : 5;
    }
}
