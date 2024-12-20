package atlantis.combat.squad.positioning.protoss.dragoon;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;

public class ProtossDragoonSeparateFromMeleeEnemies extends Manager {
    private Selection enemiesNear;

    public ProtossDragoonSeparateFromMeleeEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.protoss()) return false;
        if (!unit.isDragoon()) return false;
        double shotSecondsAgo = unit.shotSecondsAgo();
        if (shotSecondsAgo >= 7) return false;
//        if (unit.lastStartedRunningLessThanAgo(5)) return false;
        if (unit.isMoving()) return false;
//        if (unit.isMoving() && unit.lastActionLessThanAgo(30, Actions.RUN_ENEMY)) return false;

        if (unit.hp() >= 50) {
            if (unit.lastStoppedRunningLessThanAgo(20)) return false;
            if (unit.lastActionLessThanAgo(20, Actions.ATTACK_UNIT)) return false;
        }

        boolean barelyWounded = unit.shieldWound() <= 9;

        if (unit.meleeEnemiesNearCount(barelyWounded && Enemy.zerg() ? 2.4 : 3.6) <= 0) return false;
        if (barelyWounded && unit.cooldown() <= 3) return false;

        int veryNearEnemiesCount = unit.meleeEnemiesNearCount(1.8);
        if (veryNearEnemiesCount >= 3) return true;

        if (!Enemy.protoss()) {
            if (shotSecondsAgo >= 3 && unit.shieldWound() <= 30) return false;
            if (veryNearEnemiesCount <= 1 && unit.shieldWound() <= 3) return false;
        }

        if (unit.hp() >= 37 && shotSecondsAgo >= 4) return false;

        if (unit.meleeEnemiesNearCount(2.1) >= 4) return true;

        if (shotSecondsAgo >= 5) return false;
        if (fewEnemiesSuperNearAndHaveNotShotInSomeTime()) return false;

//        if (unit.cooldown() > 0) System.out.println("Cooldown: " + unit.cooldown());

        return unit.cooldown() >= (unit.shieldWound() <= 20 ? 9 : 0)
//            && unit.cooldown() <= 18
//            && !unit.isMoving()
            && (unit.lastAttackFrameLessThanAgo(unit.shields() >= 20 ? 30 * 4 : 30 * 9) || unit.meleeEnemiesNearCount(3) >= 2)
            && (unit.shieldWounded() || unit.meleeEnemiesNearCount(3) >= 2);
//            && unit.hp() <= 35
//            && unit.shieldWounded()
//            && unit.friendsNear().inRadius(5, unit).atMost(1);
    }

    private boolean fewEnemiesSuperNearAndHaveNotShotInSomeTime() {
        return unit.shields() >= 30
            && unit.shotSecondsAgo() >= 1.9;
//            && unit.meleeEnemiesNearCount(1.4) <= (Enemy.zerg() ? 2 : 1);
    }

    @Override
    protected Manager handle() {
        if ((enemiesNear = defineEnemies()).empty()) return null;

        if (enemiesNear.notEmpty()) {
//            System.out.println("--- @" + A.fr);
//            System.out.println(unit.action());
//            System.out.println(unit.target() + " / " + unit.targetPosition());
//            System.out.println(unit.lastCommandName());
            if (movedAway()) {
//                System.out.println("Moved away");
                return usedManager(this);
            }
//            System.out.println("__ NOT Moved ___");
        }

        return null;
    }

    private boolean movedAway() {
        HasPosition centerOfEnemies = unit.enemiesNear().inRadius(OurDragoonRange.range() - 0.4, unit).nearestTo(unit);
//        if (centerOfEnemies == null) centerOfEnemies = unit.enemiesNear().nearestTo(unit);

        if (centerOfEnemies == null) return false;

        double moveDist = unit.distTo(centerOfEnemies) <= 3.2 ? 5 : 0.3;

//        return unit.moveAwayFrom(centerOfEnemies, moveDist, Actions.RUN_ENEMY, "GoonSeparate")
        return unit.runningManager().runFrom(centerOfEnemies, moveDist, Actions.RUN_ENEMY, A.supplyUsed() <= 70)
            || unit.moveToSafety(Actions.RUN_ENEMY, "GoonSeparateB");
    }

    private Selection defineEnemies() {
        return unit.enemiesNear()
            .melee()
            .havingPosition()
            .havingAntiGroundWeapon()
            .notDeadMan()
            .canAttack(unit, 3.2);
    }
}
