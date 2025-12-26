package atlantis.combat.squad.positioning.protoss.cluster;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.OurBuildingUnderAttack;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.util.We;

public class ProtossForceUnitsCloserToLeader extends Manager {

    private AUnit leader;

    public ProtossForceUnitsCloserToLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.protoss()) return false;

        leader = unit.leader();
        if (leader == null) return false;

        if (!unit.isMissionAttack()) return false;
        if (OurBuildingUnderAttack.notNull()) return false;

        if (
            A.supplyUsed() >= 180
                || A.minerals() >= 900
                || (Army.strength() >= 400 && Count.ourCombatUnits() >= 20)
        ) return false;

        return unit.isCombatUnit()
            && unit.isGroundUnit()
            && !unit.isReaver()
            && !unit.isRunningOrRetreating()
            && !leader.isRunningOrRetreating()
            && unit.lastUnderAttackMoreThanAgo(50)
            && unit.distToLeader() >= 10
            && unit.hp() >= 20
            && unit.friendsInRadiusCount(6) <= 5
            && unit.enemiesThatCanAttackMe(4).empty()
//            && unit.meleeEnemiesNearCount(3) == 0
//            && unit.meleeEnemiesNearCount(5) <= 2
            && (unit.cooldown() >= 10 || unit.lastStartedAttackMoreThanAgo(20));
    }

    @Override
    public Manager handle() {
        if (leader.isWalkable() && unit.move(leader, Actions.MOVE_FORMATION, "ForceToLeader")) {
            return usedManager(this);
        }

        return null;
    }
}

