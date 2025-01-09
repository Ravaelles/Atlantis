package atlantis.combat.squad.positioning.protoss.dragoon;

//public class ProtossDragoonSeparateFromMeleeEnemies extends Manager {
//    private Selection enemiesNear;
//    private Selection enemiesVeryNear;
//    private double shotSecondsAgo;
//
//    public ProtossDragoonSeparateFromMeleeEnemies(AUnit unit) {
//        super(unit);
//    }
//
//    @Override
//    public boolean applies() {
//        if (!We.protoss()) return false;
//
////        AUnit enemy = unit.enemiesNear().nearestTo(unit);
////        if (enemy != null) System.err.println("Dist = " + unit.distTo(enemy));
//
////        if (unit.isMoving()) return false;
//
////        shotSecondsAgo = unit.shotSecondsAgo();
////        if (shotSecondsAgo >= 7) return false;
////        if (unit.lastStartedRunningLessThanAgo(5)) return false;
////        if (unit.isMoving() && unit.lastActionLessThanAgo(30, Actions.RUN_ENEMY)) return false;
//
//        double shotAgo = unit.shotSecondsAgo();
//        if (shotAgo >= 5 && unit.hp() >= 35) return false;
//
//        if (unit.hp() >= 60) {
//            if (unit.lastStoppedRunningLessThanAgo(20)) return false;
//            if (unit.isUnitActionAttack() && unit.lastActionLessThanAgo(20, Actions.ATTACK_UNIT)) return false;
//        }
//
//        enemiesNear = unit.meleeEnemiesNear().inRadius(3.5, unit);
//        enemiesVeryNear = unit.meleeEnemiesNear().inRadius(2.5, unit);
//
//        if (enemiesVeryNear.count() >= 2) return true;
//
//        if (enemiesNear.count() == 0) return false;
//        if (enemyStandingOrNotLooking()) return false;
//        if (shotAgo >= 2 && unit.shieldHealthy()) return false;
//
//        if (unit.hp() <= 40 && enemiesNear.count() > 0) return true;
//        if (separateAgainstProtoss()) return true;
//        if (separateAgainstZerg()) return true;
//
//        boolean barelyWounded = unit.shieldWound() <= 9;
//
//        if (unit.meleeEnemiesNearCount(barelyWounded && Enemy.zerg() ? 2.4 : 3.6) <= 0) return false;
//        if (barelyWounded && unit.cooldown() <= 3) return false;
//
//        int veryNearEnemiesCount = unit.meleeEnemiesNearCount(2.4);
//        if (veryNearEnemiesCount >= 3) return true;
//
//        if (!Enemy.protoss()) {
//            if (shotSecondsAgo >= 3 && unit.shieldWound() <= 30) return false;
//            if (veryNearEnemiesCount <= 1 && unit.shieldWound() <= 3) return false;
//        }
//
////        if (unit.hp() >= 47 && shotSecondsAgo >= 4) return false;
//
//        if (unit.meleeEnemiesNearCount(2.6) >= 3) return true;
//
//        if (shotSecondsAgo >= 5) return false;
//        if (fewEnemiesSuperNearAndHaveNotShotInSomeTime()) return false;
//
////        if (unit.cooldown() > 0) System.out.println("Cooldown: " + unit.cooldown());
//
//        return unit.cooldown() >= (unit.shieldWound() <= 20 ? 9 : 0)
////            && unit.cooldown() <= 18
////            && !unit.isMoving()
//            && (unit.lastAttackFrameLessThanAgo(unit.shields() >= 20 ? 30 * 4 : 30 * 9) || unit.meleeEnemiesNearCount(3) >= 2)
//            && (unit.shieldWounded() || unit.meleeEnemiesNearCount(3) >= 2);
////            && unit.hp() <= 35
////            && unit.shieldWounded()
////            && unit.friendsNear().inRadius(5, unit).atMost(1);
//    }
//
//    private boolean enemyStandingOrNotLooking() {
//        if (enemiesNear.count() >= 2) return false;
//
//        AUnit enemy = enemiesNear.nearestTo(unit);
//        if (enemy == null) return false;
//
////        if ((!enemy.isMoving() && !enemy.isAttacking())) {
////            enemy.paintCircleFilled(10, Color.Red);
//////            GameSpeed.changeSpeedTo(30);
////            System.err.println(A.now() + " - Enemy not moving " + enemy);
////        }
//
//        return (!enemy.isMoving() && !enemy.isAttacking())
//            || !unit.isOtherUnitFacingThisUnit(enemy);
//    }
//
//    private boolean separateAgainstProtoss() {
//        if (!Enemy.protoss()) return false;
//        if (unit.shieldHealthy()) return false;
//
//        if (unit.meleeEnemiesNearCount(2.2) >= 2) return true;
////        if (unit.shields() <= 20 && unit.meleeEnemiesNearCount(3.85) >= 1) return true;
//
//        int enemies = unit.meleeEnemiesNearCount(3.75);
//
//        if (enemies >= 4) return true;
//        if (enemies >= 2 && unit.shotSecondsAgo() <= 2) return true;
//        if (enemies >= 1 && unit.hp() <= 36 && unit.shotSecondsAgo() <= 8) return true;
//        if (unit.shieldWound() >= 13 && enemies >= 1 && unit.cooldown() >= 8) return true;
//
//        return false;
//    }
//
//    private boolean separateAgainstZerg() {
//        if (!Enemy.zerg()) return false;
//
//        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(2.5);
//
//        if (unit.enemiesNear().ranged().inRadius(6, unit).atLeast(2)) return true;
//        if (meleeEnemiesNearCount >= 2 && unit.shotSecondsAgo() <= 2.5) return true;
//
//        if (meleeEnemiesNearCount <= 1 && unit.shieldWound() <= 20 && unit.combatEvalRelative() > 0.8) return false;
//
//        if (unit.shields() <= 40 && meleeEnemiesNearCount >= 1 && unit.shotSecondsAgo() <= 6) return true;
//
//        return shotSecondsAgo <= 1
//            || (shotSecondsAgo <= 3 && unit.enemiesNearInRadius(3) >= 3);
//    }
//
//    private boolean fewEnemiesSuperNearAndHaveNotShotInSomeTime() {
//        return unit.shields() >= 30
//            && unit.shotSecondsAgo() >= 1.9;
////            && unit.meleeEnemiesNearCount(1.4) <= (Enemy.zerg() ? 2 : 1);
//    }
//
//    @Override
//    protected Manager handle() {
//        if ((enemiesNear = defineEnemies()).empty()) return null;
//
//        if (enemiesNear.notEmpty()) {
////            System.out.println("--- @" + A.fr);
////            System.out.println(unit.action());
////            System.out.println(unit.target() + " / " + unit.targetPosition());
////            System.out.println(unit.lastCommandName());
//            if (movedAway()) {
////                System.out.println("Moved away");
//                return usedManager(this);
//            }
////            System.out.println("__ NOT Moved ___");
//        }
//
//        return null;
//    }
//
//    private boolean movedAway() {
//        HasPosition centerOfEnemies = unit.enemiesNear().inRadius(OurDragoonRange.range() - 0.4, unit).nearestTo(unit);
////        if (centerOfEnemies == null) centerOfEnemies = unit.enemiesNear().nearestTo(unit);
//
//        if (centerOfEnemies == null) return false;
//
//        double moveDist = unit.distTo(centerOfEnemies) <= 2.8 ? 5 : 2;
//
////        return unit.moveAwayFrom(centerOfEnemies, moveDist, Actions.RUN_ENEMY, "GoonSeparate")
//        return unit.runningManager().runFrom(centerOfEnemies, moveDist, Actions.RUN_ENEMY, canNotifyOthersToMove())
//            || unit.runningManager().runFrom(centerOfEnemies, 3.5, Actions.RUN_ENEMY, canNotifyOthersToMove())
//            || unit.moveToSafety(Actions.RUN_ENEMY, "GoonSeparateB");
//    }
//
//    private boolean canNotifyOthersToMove() {
//        return unit.shields() <= 50 || enemiesVeryNear.atLeast(2) || A.supplyUsed() <= 80;
//    }
//
//    private Selection defineEnemies() {
//        return unit.enemiesNear()
//            .melee()
//            .havingPosition()
//            .havingAntiGroundWeapon()
//            .notDeadMan()
//            .canAttack(unit, 3.2);
//    }
//}
