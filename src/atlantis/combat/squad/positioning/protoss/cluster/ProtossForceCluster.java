package atlantis.combat.squad.positioning.protoss.cluster;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.OurBuildingUnderAttack;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ProtossForceCluster extends Manager {
    public ProtossForceCluster(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.protoss()) return false;

//        if (!unit.isMissionAttack()) return false;
        if (A.supplyUsed(185)) return false;
        if (A.minerals() >= 1000) return false;
        if (unit.isRunningOrRetreating()) return false;
        if (unit.isDancingAway()) return false;
        if (Army.strength() >= 500 && Count.ourCombatUnits() >= 25) return false;
        if (OurBuildingUnderAttack.notNull()) return false;
        if (unit.squad() != null && !unit.squad().isAlpha()) return false;
        if (unit.type().isTransport()) return false;

        return true;
    }

    @Override
    public Manager handle() {
        AUnit leader = unit.squadLeader();
        if (leader == null) return null;

        // Go to LEADER
        if (unit.distTo(leader) > 10 && unit.moveToLeader(Actions.MOVE_FORMATION, "PCluster2L")) {
            return usedManager(this);
        }

        // Go to FRIEND
        if (!skipGoingToFriend()) {
            AUnit friend = friend();
            if (friend != null && unit.distTo(friend) > 0.7 && unit.move(friend, Actions.MOVE_FORMATION, "PCluster2F")) {
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean skipGoingToFriend() {
        if (A.supplyUsed() >= 170 && Army.strengthWithoutCB() >= 130) return true;
        if (EnemyInfo.noRanged()) return true;

        if (Count.ourCombatUnits() <= 10) return false;

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
