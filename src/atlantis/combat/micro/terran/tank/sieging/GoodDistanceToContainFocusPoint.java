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

        if (unit.lastSiegedOrUnsiegedAgo() <= 30 * (6 + unit.id() % 3)) return false;

        APosition focusPoint = Missions.globalMission().focusPoint();
        if (
            focusPoint != null
                && goodDistance(focusPoint)
                && TankDecisions.canSiegeHere(unit, true)
        ) return true;

        return false;
    }

    private boolean goodDistance(APosition focusPoint) {
        if (Enemy.terran()) {
            return unit.distTo(focusPoint) <= 2 || unit.lastActionMoreThanAgo(30 * 2);
        }

        return unit.distTo(focusPoint) <= (Enemy.terran() ? 10 : (6 + unit.id() % 3));
    }

    @Override
    protected Manager handle() {
        if (WantsToSiege.wantsToSiegeNow(unit, this, "ContainSiege")) return usedManager(this);

        return null;
    }
}
