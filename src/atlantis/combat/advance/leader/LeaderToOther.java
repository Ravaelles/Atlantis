package atlantis.combat.advance.leader;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.util.We;

import java.util.List;

public class LeaderToOther extends MissionManager {
    private AUnit otherFriend;

    public LeaderToOther(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (We.protoss() && Enemy.zerg() && Count.ourCombatUnits() <= 10) return false;

        if (
            We.protoss()
//                && !Enemy.protoss()
                && !EnemyInfo.hasRanged()
                && unit.enemiesNearInRadius(9) == 0
        ) return false;

        otherFriend = otherFriend();
        if (otherFriend == null) return false;
        double distToOther = otherFriend.distTo(unit);

        if (asProtossShouldBeCautiosAgainstProtossEarly(distToOther)) return true;

        if (We.terran()) {
            if (unit.enemiesNear().canAttack(unit, 5.1).notEmpty()) return false;
        }
        else if (We.protoss()) {
            if (unit.enemiesNear().canAttack(unit, unit.shieldWound() >= 9 ? 6.1 : 4.1).notEmpty()) return false;
        }

        return distToOther <= 20 && distToOther > dist();
    }

    private boolean asProtossShouldBeCautiosAgainstProtossEarly(double distToOther) {
        if (!Enemy.protoss()) return false;
        if (!We.protoss()) return false;

        int dragoons = Count.dragoons();

        return (dragoons <= 5 || AGame.killsLossesResourceBalance() < -50)
            && distToOther >= 4
            && dragoons <= 12;
    }

    private double dist() {
        if (unit.enemiesNear().combatUnits().havingWeapon().empty()) return 5;

        return 1.6 + unit.squadSize() / 5.0 + (Army.strength() >= 300 ? 1.5 : 0);
    }

    @Override
    protected Manager handle() {
        if (unit.isOvercrowded()) return null;
        if (!otherFriend.isWalkable()) return null;

        if (A.s % 3 <= 1) {
            if (unit.move(otherFriend, Actions.MOVE_FORMATION, "LeaderToOther")) return usedManager(this);
        }
        else {
            if (!unit.isHoldingPosition() && !unit.isAttacking()) {
                unit.holdPosition(Actions.HOLD_POSITION, "LeaderHold");
            }
            return usedManager(this);
        }

        return null;
    }

    private AUnit otherFriend() {
//        return unit.squad().units().groundUnits().exclude(unit).combatUnits().nearestTo(unit);
        Selection friends = unit.squad().units().groundUnits().exclude(unit).combatUnits();

        List<AUnit> sorted = friends.sortDataByGroundDistanceTo(unit, true);
        if (sorted.isEmpty()) return null;

        return sorted.get(sorted.size() / 2);
    }
}
