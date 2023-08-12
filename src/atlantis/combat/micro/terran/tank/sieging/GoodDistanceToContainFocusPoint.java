package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.TankDecisions;
import atlantis.combat.missions.Missions;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class GoodDistanceToContainFocusPoint extends Manager {
    public GoodDistanceToContainFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMissionContain() && !unit.isMissionDefend()) return false;

        APosition focusPoint = Missions.globalMission().focusPoint();
        if (
            focusPoint != null
                && unit.distTo(focusPoint) <= (Enemy.terran() ? 10 : (6 + unit.id() % 3))
                && TankDecisions.canSiegeHere(unit, true)
        ) return true;

        return false;
    }

    @Override
    protected Manager handle() {
        return usedManager(WantsToSiege.wantsToSiegeNow(this, "ContainSiege"));
    }
}
