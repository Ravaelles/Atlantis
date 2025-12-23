package atlantis.combat.squad.positioning.protoss.cluster;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.OurBuildingUnderAttack;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ProtossForceCluster extends Manager {

    private double distToLeader;

    public ProtossForceCluster(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.protoss()) return false;

        if (!Enemy.zerg()) return false;

//        if (!unit.isMissionAttack()) return false;
        if (A.supplyUsed(196) && unit.eval() >= 1.5) return false;
        if (A.minerals() >= 1000) return false;
        if (unit.cooldown() > 20) return false;
        if (unit.cooldown() <= 8 && unit.eval() >= 1.5 && unit.meleeEnemiesNearCount(3.8) == 0) return true;

        if (Army.strength() >= 700 && Count.ourCombatUnits() >= 25) return false;
        if (unit.isRunningOrRetreating()) return false;
        if (unit.isDancingAway() && unit.cooldown() >= 1) return false;
        if (Count.ourCombatUnits() >= 25 && unit.eval() >= 10) return false;
        if (unit.isRanged() && unit.cooldown() <= 8 && unit.hp() >= 80 && unit.eval() >= 2) return false;
        if (OurBuildingUnderAttack.notNull()) return false;
        if (unit.squad() != null && !unit.squad().isAlpha()) return false;
        if (unit.type().isTransport()) return false;
        if (unit.cooldown() <= 5 && unit.meleeEnemiesNearCount(3.7) >= 1) return false;
        if (unit.cooldown() <= 5 && unit.enemiesICanAttack(2).notEmpty()) return false;
        if (unit.cooldown() <= 5 && unit.enemiesThatCanAttackMe(0.3).notEmpty()) return false;

        return true;
    }

    @Override
    public Manager handle() {
        AUnit leader = unit.squadLeader();
        if (leader == null) return null;

        // Go to LEADER
        distToLeader = unit.distTo(leader);
        if (
            distToLeader > 9
                && !unit.isLeader()
                && unit.moveToLeader(Actions.MOVE_FORMATION, "PCluster2L")
        ) {
            return usedManager(this);
        }

        // Go to FRIEND
        if (!skipGoingToFriend()) {
            AUnit friend = friend();
            if (friend != null && unit.distTo(friend) > minDist() && unit.move(friend, Actions.MOVE_FORMATION, "PCluster2F")) {
                return usedManager(this);
            }
        }

        return null;
    }

    private double minDist() {
        if (Enemy.zerg()) {
            if (unit.isRanged()) return 1.7;
            return 0.6;
        }

        if (Enemy.protoss()) {
            if (unit.isRanged()) return 1;
            return 0.8;
        }

        return 1.5;
    }

    private boolean skipGoingToFriend() {
        if (A.supplyUsed() >= 170 && Army.strengthWithoutCB() >= 130) return true;
        if (EnemyInfo.noRanged()) return true;

        if (Count.ourCombatUnits() <= 10) return false;

        if (Enemy.zerg() && distToLeader <= 6) return true;
        if (Enemy.protoss() && distToLeader <= 4) return true;
        if (Enemy.terran() && distToLeader <= 4) return true;

        if (unit.enemiesNear().combatUnits().empty()) return true;

        if (Army.strengthWithoutCB() <= 160) return false;

        return true;
    }

    private AUnit friend() {
        AUnit friend = unit.friendsNear().combatUnits().nearestTo(unit);
        if (friend != null) return friend;

        return Select.ourCombatUnits().exclude(unit).nearestTo(unit);
    }
}
