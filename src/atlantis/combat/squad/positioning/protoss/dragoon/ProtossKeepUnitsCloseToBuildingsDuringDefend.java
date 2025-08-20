package atlantis.combat.squad.positioning.protoss.dragoon;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.OurBuildingUnderAttack;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class ProtossKeepUnitsCloseToBuildingsDuringDefend extends Manager {
    private AFocusPoint focusPoint;
    private AUnit building;

    public ProtossKeepUnitsCloseToBuildingsDuringDefend(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMissionDefend()) return false;
        if (!unit.isCombatUnit()) return false;
        if (unit.isRunningOrRetreating()) return false;
        if ((building = building()) == null) return false;

        boolean distTooBig = unit.groundDist(building) >= (Enemy.zerg() ? 3 : 50);

        if (unit.distToFocusPoint() >= 7 && distTooBig && unit.eval() <= 10) return true;

        return distTooBig
            && unit.enemiesNear().combatUnits().havingWeapon().notEmpty()
            && unit.noCooldown()
            && unit.squadLeader().eval() <= 4
//            && unit.meleeEnemiesNearCount(2.7) == 0
            && (focusPoint = unit.focusPoint()) != null
            && noBuildingUnderSeriousAttack()
            && (!unit.squad().lastUnderAttackLessThanAgo(30) && unit.distToBase() >= 5)
            && unit.friendsNear().workers().attacking().empty();
//            && (asLeader() || asNonLeader());
    }

    private static AUnit building() {
        if (Count.cannonsWithUnfinished() > 0) {
            return Select.ourWithUnfinished().cannons().mostDistantTo(Select.mainOrAnyBuilding());
        }

        return Select.mainOrAnyBuilding();
    }

    private static boolean noBuildingUnderSeriousAttack() {
        return OurBuildingUnderAttack.noBuildingUnderSeriousAttack();
    }

//    private boolean asNonLeader() {
//        if (!unit.isLeader()) return false;
//
//        double distToLeader = unit.distToLeader();
//        if (
//            distToLeader >= 5
//                || (distToLeader >= 3 && unit.eval() <= 2)
//        ) return true;
//
//        return false;
//    }
//
//    private boolean asLeader() {
//        if (!unit.isLeader()) return false;
//
//        if (unit.distTo(focusPoint) >= 2) return true;
//
//        return false;
//    }

    @Override
    public Manager handle() {
        if (unit.distToLeader() > 2 && unit.enemiesThatCanAttackMe(3).count() == 0) {
            if (unit.moveToLeader(Actions.MOVE_FOCUS)) {
                return usedManager(this);
            }
        }

        if (unit.distTo(building) >= 4 && unit.move(building, Actions.MOVE_FOCUS)) {
            return usedManager(this);
        }

//        if (unit.move(building, Actions.MOVE_FOCUS)) {
//            return usedManager(this);
//        }

        return null;
    }
}
