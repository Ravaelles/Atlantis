package atlantis.combat.squad.positioning.protoss.cohesion;

import atlantis.architecture.Manager;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

public class ProtossTooFarFromLeader extends Manager {

    private AUnit leader;

    public ProtossTooFarFromLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isCombatUnit()) return false;
        if (unit.isRunningOrRetreating()) return false;
        if (unit.isMissionDefendOrSparta()) return false;
//        if (unit.enemiesNear().ranged().empty()) return false;
        if (Count.ourCombatUnits() >= 16 && Army.strength() >= 800) return false;
        if (unit.lastStoppedRunningLessThanAgo(40)) return false;

        leader = unit.squadLeader();
        if (leader == null) return false;
        double dist = unit.distTo(leader);

//        if (unit.lastUnderAttackAgo() <= 80) return false;
        if (unit.lastPositioningActionLessThanAgo(10)) return false;
        if (unit.enemiesThatCanAttackMe(4.5 + (unit.hp() <= 40 ? 1 : 0)).notEmpty()) return false;
        if (unit.leaderIsRetreating() && !unit.isAttacking()) return false;
        if (unit.leaderIsRetreating() && !unit.isAttacking()) return false;
        if (unit.enemiesNear().havingAtLeastHp(1).countInRadius(4, unit) > 0) return false;

        if (dist >= 10) return true;

//        if (
//            unit.isMissionDefendOrSparta()
//                && unit.groundDistToMain() <= 20
//                && unit.enemiesICanAttack(0.2).notEmpty()
//        ) return false;

//        if (dist <= 8 && unit.shotSecondsAgo(2)) return false;

        if (unit.nearestChokeCenterDist() <= 4) return false;

        // Friends
        if (manyFriendsNear()) return false;

        if (dist >= 5) return true;

        return dist >= 2
            && unit.friendsNear().combatUnits().inRadius(2, unit).atMost(5);
    }

    private boolean manyFriendsNear() {
        return unit.friendsNear().combatUnits().inRadius(1, unit).atLeast(2);
    }

    @Override
    public Manager handle() {
        if (leader == null) leader = unit.squadLeader();
        if (leader == null) return null;

        if (leader.isWalkable() && unit.moveToLeader(Actions.MOVE_FORMATION)) {
            return usedManager(this);
        }

        return null;
    }
}
