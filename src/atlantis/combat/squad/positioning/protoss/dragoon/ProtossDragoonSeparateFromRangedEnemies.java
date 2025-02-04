package atlantis.combat.squad.positioning.protoss.dragoon;

//public class ProtossDragoonSeparateFromRangedEnemies extends Manager {
//    private Selection enemiesNear;
//    private boolean underAttackRecently;
//
//    public ProtossDragoonSeparateFromRangedEnemies(AUnit unit) {
//        super(unit);
//    }
//
//    @Override
//    public boolean applies() {
////        if (true) return false;
//
//        if (!We.protoss()) return false;
//        if (!unit.isDragoon()) return false;
//        if (unit.isMoving() && unit.lastActionLessThanAgo(15, Actions.RUN_ENEMY)) return false;
//
//        if (asWoundedAlwaysSeparate()) return true;
//
////        return unit.cooldown() <= 24
////            && unit.cooldown() >= (unit.shieldWound() <= 7 ? 9 : 0)
//        underAttackRecently = unit.lastUnderAttackLessThanAgo(45);
//
//        return
//            unit.cooldown() >= (unit.shieldWound() <= 7 ? 6 : 0)
////            && !unit.isAttackFrame()
////            && !unit.isStartingAttack()
////            && unit.lastAttackFrameLessThanAgo((unit.shieldWound() <= 7 && unit.isMissionAttack() ? 90 : 30 * 5))
////            && unit.lastAttackFrameAgo() < unit.lastStartedRunningAgo()
////            && unit.lastAttackFrameAgo() <= 21
//                && (unit.isWounded() || unit.combatEvalRelative() <= 1.6 || underAttackRecently)
//                && appliesAgainstProtoss()
//                && appliesAgainstZerg();
//    }
//
//    private boolean asWoundedAlwaysSeparate() {
//        if (unit.shields() >= 20) return false;
//        double shotSecondsAgo = unit.shotSecondsAgo();
//
//        int enemies = unit.hp() <= 120 ? 1 : 2;
//
//        return unit.cooldown() >= 1
//            || (shotSecondsAgo <= 6 && unit.enemiesNearInRadius(OurDragoonRange.range() - 0.4) >= enemies);
//    }
//
//    private boolean appliesAgainstProtoss() {
//        if (!Enemy.protoss()) return true;
//
//        if (underAttackRecently) return true;
//
//        return unit.cooldown() >= 8;
////        return unit.cooldown() <= 24 && unit.cooldown() >= 5;
//
////        return (unit.hp() <= 130 || unit.combatEvalRelative() <= 0.9)
////            && (unit.hp() <= 80 || unit.shotSecondsAgo() <= 3);
//    }
//
//    private boolean appliesAgainstZerg() {
//        if (!Enemy.zerg()) return true;
//
//        if (unit.shieldWounded()) return true;
//        if (underAttackRecently) return true;
//
//        return unit.shotSecondsAgo() <= 6;
//    }
//
//    @Override
//    protected Manager handle() {
//        if ((enemiesNear = rangedEnemiesNear()).empty()) return null;
//
////        unit.paintCircleFilled(10, Color.Green);
//
////        System.out.println(A.fr + " Goon " + unit.idWithHash() + " separating from ranged");
////        if (unit.isLeader()) {
////            System.err.println("unit.lastAttackFrameAgo() = " + unit.lastAttackFrameAgo());
////            for (AUnit enemy : enemiesNear.list()) {
////                enemy.paintCircle(10, Color.Red);
////                enemy.paintCircle(12, Color.Red);
////                enemy.paintCircle(14, Color.Red);
////            }
////        }
//
//        if (enemiesNear.notEmpty()) {
//            if (movedAway()) {
//                return usedManager(this);
//            }
//        }
//
//        return null;
//    }
//
//    private boolean movedAway() {
//        HasPosition centerOfEnemies = enemiesNear.nearestTo(unit);
//        if (centerOfEnemies == null) return false;
//
//        double moveDist = unit.shieldWounded() && unit.distTo(centerOfEnemies) <= distToEnemies()
//            ? 3
//            : 0.5;
//
////        if (unit.hp() <= 60) moveDist = 5;
//
//        return standardRunFrom(centerOfEnemies, moveDist)
//            || unit.moveAwayFrom(centerOfEnemies, moveDist, Actions.RUN_ENEMY, "GoonSeparate");
//    }
//
//    private boolean standardRunFrom(HasPosition centerOfEnemies, double moveDist) {
//        return unit.runningManager().runFrom(
//            centerOfEnemies, moveDist, Actions.RUN_ENEMY, true
//        );
//    }
//
//    private static double distToEnemies() {
//        return Math.max(3.93, (OurDragoonRange.range() - 0.15));
//    }
//
//    private Selection rangedEnemiesNear() {
//        double healthRadiusBonus = unit.woundPercent() / 23.0;
//        double lastAttackedBonus = unit.lastUnderAttackLessThanAgo(50) ? 1.3 : 0.0;
//
//        return unit.enemiesNear()
//            .ranged()
//            .havingPosition()
//            .havingAntiGroundWeapon()
//            .excludeTanks()
////            .havingSmallerRange(unit)
////            .facing(unit)
//            .notShowingBackToUs(unit)
//            .inRadius(OurDragoonRange.range() - 0.1 + healthRadiusBonus + lastAttackedBonus, unit);
////            .canAttack(unit, 0.8 + unit.woundPercent() / 80.0);
//    }
//}
