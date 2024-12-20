package atlantis.combat.squad.positioning.protoss.dragoon;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.DistFromFocus;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;
import bwapi.Color;

public class ProtossDragoonSeparateFromRangedEnemies extends Manager {
    private Selection enemiesNear;

    public ProtossDragoonSeparateFromRangedEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!We.protoss()) return false;
        if (!unit.isDragoon()) return false;
        if (unit.isMoving() && unit.lastActionLessThanAgo(15, Actions.RUN_ENEMY)) return false;

//        return unit.cooldown() <= 24
//            && unit.cooldown() >= (unit.shieldWound() <= 7 ? 9 : 0)
        return
            unit.cooldown() >= (unit.shieldWound() <= 7 ? 4 : 0)
//            && !unit.isAttackFrame()
//            && !unit.isStartingAttack()
//            && unit.lastAttackFrameLessThanAgo((unit.shieldWound() <= 7 && unit.isMissionAttack() ? 90 : 30 * 5))
//            && unit.lastAttackFrameAgo() < unit.lastStartedRunningAgo()
//            && unit.lastAttackFrameAgo() <= 21
                && (unit.isWounded() || unit.combatEvalRelative() <= 1.6)
                && appliesAgainstProtoss()
                && appliesAgainstZerg()
//            && (unit.woundPercent() >= 20 ||
                && (enemiesNear = rangedEnemiesNear()).notEmpty();
    }

    private boolean appliesAgainstProtoss() {
        if (!Enemy.protoss()) return true;

        return unit.cooldown() >= 5;
//        return unit.cooldown() <= 24 && unit.cooldown() >= 5;

//        return (unit.hp() <= 130 || unit.combatEvalRelative() <= 0.9)
//            && (unit.hp() <= 80 || unit.shotSecondsAgo() <= 3);
    }

    private boolean appliesAgainstZerg() {
        if (!Enemy.zerg()) return true;

        if (unit.shieldWounded()) return true;

        return unit.shotSecondsAgo() <= 6;
    }

    @Override
    protected Manager handle() {
//        unit.paintCircleFilled(10, Color.Green);

//        System.out.println(A.fr + " Goon " + unit.idWithHash() + " separating from ranged");
//        if (unit.isLeader()) {
//            System.err.println("unit.lastAttackFrameAgo() = " + unit.lastAttackFrameAgo());
//            for (AUnit enemy : enemiesNear.list()) {
//                enemy.paintCircle(10, Color.Red);
//                enemy.paintCircle(12, Color.Red);
//                enemy.paintCircle(14, Color.Red);
//            }
//        }

        if (enemiesNear.notEmpty()) {
            if (movedAway()) {
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean movedAway() {
        HasPosition centerOfEnemies = enemiesNear.nearestTo(unit);
        if (centerOfEnemies == null) return false;

        double moveDist = unit.shieldWounded() && unit.distTo(centerOfEnemies) <= distToEnemies()
            ? 3
            : 0.5;

//        if (unit.hp() <= 60) moveDist = 5;

        return standardRunFrom(centerOfEnemies, moveDist)
            || unit.moveAwayFrom(centerOfEnemies, moveDist, Actions.RUN_ENEMY, "GoonSeparate");
    }

    private boolean standardRunFrom(HasPosition centerOfEnemies, double moveDist) {
        return unit.runningManager().runFrom(
            centerOfEnemies, moveDist, Actions.RUN_ENEMY, unit.shieldWound() >= 7
        );
    }

    private static double distToEnemies() {
        return Math.max(3.93, (OurDragoonRange.range() - 0.15));
    }

    private Selection rangedEnemiesNear() {
        double healthRadiusBonus = unit.woundPercent() / (unit.combatEvalRelative() <= 1.6 ? 45.0 : 90.0);

        return unit.enemiesNear()
            .ranged()
            .havingPosition()
            .havingAntiGroundWeapon()
            .excludeTanks()
//            .havingSmallerRange(unit)
//            .facing(unit)
            .notShowingBackToUs(unit)
            .inRadius(OurDragoonRange.range() - 0.05 + healthRadiusBonus, unit);
//            .canAttack(unit, 0.8 + unit.woundPercent() / 80.0);
    }
}
