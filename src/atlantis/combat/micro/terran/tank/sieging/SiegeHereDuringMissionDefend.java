package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.TankDecisions;
import atlantis.combat.missions.Missions;
import atlantis.information.enemy.EnemyUnitBreachedBase;
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
        if (preventNotAttackingEnemyWhoBreachedBase()) return usedManager(this);

        if (unit.isMissionDefendOrSparta() && goodDistanceToFocusOrBunker()) {
            if (unit.target() == null || unit.target().distTo(unit) < 12) {
                if (TankDecisions.canSiegeHere(unit, false)) {
                    unit.siege();
                    unit.setTooltipAndLog("SiegeHereDuringMissionDefend");
                    return usedManager(this);
                }
            }
        }

        return null;
    }

    private boolean goodDistanceToFocusOrBunker() {
        return unit.distToFocusPoint() <= minDist()
            || (!Enemy.terran() && unit.friendsNear().bunkers().countInRadius(3, unit) > 0);
    }

    private double minDist() {
        return Enemy.terran() ? 3 : 5;
    }

    private boolean preventNotAttackingEnemyWhoBreachedBase() {
        AUnit enemy = EnemyUnitBreachedBase.get();
        if (enemy == null) return false;

        if (enemy.distTo(unit) > 12) {
            if (unit.enemiesNear().canBeAttackedBy(unit, 0).notEmpty()) return false;

            if (unit.isSieged()) unit.unsiege();

            return true;
        }

        return false;
    }
}
