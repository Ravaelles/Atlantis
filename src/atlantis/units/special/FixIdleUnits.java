package atlantis.units.special;

import atlantis.architecture.Manager;
import atlantis.combat.generic.DoNothing;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class FixIdleUnits extends Manager {
    public FixIdleUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isCombatUnit()) return false;
        if (unit.isMoving()) return false;
        if (unit.hasCooldown()) return false;
        if (unit.enemiesNear().combatBuildingsAntiLand().notEmpty()) return false;

        if (unit.isActiveManager(DoNothing.class)) return true;

        if (unit.distToLeader() <= 6) return false;

        if (A.s > 2 && unit.isStopped() && moveToLeader()) return true;

//        if (unit.enemiesNear().inRadius(6, unit).notEmpty()) return false;
//        if (unit.lastPositionChangedLessThanAgo(30 * 4)) return false;

        if (unit.isStopped() && unit.noCooldown() && unit.lastAttackFrameMoreThanAgo(60)) return true;
//        if (A.fr <= 2 && unit.isStopped()) return true;

//        return false;

        return
//            && (unit.isStopped() || unit.isIdle())
//            && unit.noCooldown()
//            && unit.lastStoppedRunningMoreThanAgo(12)
//            unit.lastOrderWasFramesAgo() >= 30 * 4
            unit.lastActionMoreThanAgo(60)
                && A.now % 19 == 0;
//            && unit.isActiveManager(DoNothing.class)
//            && unit.enemiesNear().ranged().countInRadius(6, unit) == 0
//            && (!unit.isRanged() || unit.enemiesNear().inRadius(4, unit).empty());
    }

    @Override
    public Manager handle() {
//        if (!unit.isAttacking() && unit.woundPercent() <= 70 && unit.combatEvalRelative() >= 1.3) {
//            if (attackEnemies()) return usedManager(this, "FixIdleByAttack");
//        }

//        if ((new HandleUnitPositioningOnMap(unit)).invokeFrom(this) != null) return usedManager(this);

        moveToLeader(); // Move, but don't return that we used this manager.

//        if (moveToLeader()) return usedManager(this);
//        if ((new HandleUnitPositioningOnMap(unit)).invokeFrom(this) != null) return usedManager(this);
//        if (movedToFocusPoint()) return usedManager(this);

        return null;
    }

    private boolean moveToLeader() {
        AUnit leader = unit.squadLeader();
        if (leader == null) return false;

        if (unit.distTo(leader) >= 2.5 && unit.distTo(leader) <= 60 && leader.isWalkable()) {
            if (unit.move(leader, Actions.MOVE_UNFREEZE, "FixIdleByLeader")) {
//                System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - " + unit.targetPosition() +
//                    " / " + unit.action());
                return true;
            }
        }
        return false;
    }

    private boolean attackEnemies() {
        if (unit.enemiesNear().notEmpty()) {
            if ((new AttackNearbyEnemies(unit)).invokedFrom(this)) return true;
        }

        return false;
    }

    private boolean movedToFocusPoint() {
        AFocusPoint focusPoint = unit.focusPoint();
        if (focusPoint == null) return false;

        if (
            !unit.isMoving()
                && unit.distToFocusPoint() > 5
                && unit.move(focusPoint, Actions.MOVE_UNFREEZE, "FixIdleByFocus")
        ) {
//            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - FixIdleUnits");
            return true;
        }

        return false;
    }
}
