package atlantis.combat.squad.positioning.protoss.cluster.old;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.OurBuildingUnderAttack;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.util.We;

public class ProtossKeepUnitsClustered extends Manager {
    public ProtossKeepUnitsClustered(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.protoss()) return false;

        if (!unit.isMissionAttack()) return false;
        if (A.minerals() >= 1500) return false;
        if (Army.strength() >= 350 && Count.ourCombatUnits() >= 22) return false;
        if (OurBuildingUnderAttack.notNull()) return false;
        if (unit.squad() != null && unit.squad().hasMostlyOffensiveRole()) return false;
        if (unit.type().isTransport()) return false;

        return unit.lastActionMoreThanAgo(10, Actions.ATTACK_UNIT)
            && !unit.isRunning()
            && !unit.underAttackSecondsAgo(2)
            && unit.lastStartedRunningMoreThanAgo(30 * 3)
            && unit.lastStoppedRunningMoreThanAgo(30)
            && (unit.cooldown() == 0 || unit.cooldown() >= 12)
//            && (unit.cooldown() >= 12 || !unit.underAttackSecondsAgo(2))
            && (
                unit.distToLeader() >= 6 || unit.friendsInRadiusCount(2) <= 1
            )
            && (
                unit.distToLeader() <= 16 || unit.enemiesNear().combatUnits().inRadius(13, unit).empty()
            );
//            && (!unit.isMoving() || unit.lastActionMoreThanAgo(10, Actions.MOVE_FORMATION));
//            && (
//            (unit.distToLeader() >= 6 && unit.friendsInRadiusCount(2) <= 2)
//        );
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            ProtossForceUnitsCloserToLeader.class,
//            ProtossForceClusterDragoon.class,
//            ProtossForceClusterZealot.class,
//            ProtossTooFarFromSquadCenter.class,
        };
    }
}
