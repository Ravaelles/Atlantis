package atlantis.combat.squad.positioning.protoss.cohesion;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class ProtossAsLeaderTooFarFromOthers extends Manager {

    private HasPosition nearestFriend;

    public ProtossAsLeaderTooFarFromOthers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isLeader()) return false;
        if (unit.isMissionDefendOrSparta()) return false;
        if (unit.enemiesThatCanAttackMe(3 + (unit.hp() <= 80 ? 2 : 0)).notEmpty()) return false;
        if (unit.enemiesICanAttack(1).notEmpty()) return false;
        if ((A.isUms() || Count.ourCombatUnits() >= 15) && EnemyUnits.discovered().groundUnits().empty()) return false;

        nearestFriend = nearestFriend();
        if (nearestFriend == null) return false;

        return unit.distTo(nearestFriend) > maxDist();
    }

    private double maxDist() {
        if (Count.ourCombatUnits() >= 10 && Army.strengthWithoutCB() >= 170) return 4;

        if (unit.distToBase() <= 40) return 3;

        return 2.5;
    }

    private HasPosition nearestFriend() {
        AUnit friend = unit.friendsNear().combatUnits().nearestTo(unit);
        if (friend != null) return friend;

        return Select.ourCombatUnits().exclude(unit).nearestTo(unit);
    }

    @Override
    protected Manager handle() {
        if (nearestFriend == null || !nearestFriend.isWalkable()) return null;

        if (squad.size() >= 3 && squad.cohesionPercent() <= 74) {
            AUnit median = squad.medianUnit();
            if (median != null && median.position().isWalkable()) {
                if (
                    (!unit.isMoving() || A.everyNthGameFrame(9))
                        && unit.move(median, Actions.MOVE_FORMATION, "Leader2Median")
                ) {
                    return usedManager(this);
                }
            }
        }

        if (unit.move(nearestFriend, Actions.MOVE_FORMATION, "LeaderBack")) return usedManager(this);

        return null;
    }
}
