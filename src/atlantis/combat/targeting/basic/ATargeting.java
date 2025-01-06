package atlantis.combat.targeting.basic;

import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.targeting.*;
import atlantis.combat.targeting.air.DontAttackOverlords;
import atlantis.combat.targeting.tanks.ATankTargeting;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.UnitsArchive;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ATargeting extends HasUnit {
    //    protected static final boolean DEBUG = true;
    protected static final boolean DEBUG = false;

    protected Selection enemyBuildings;
    protected Selection enemyUnits;
    private Selection leaderEnemies = null;

    public ATargeting(AUnit unit) {
        super(unit);

        double maxDistFromEnemy = 678;

        defineEnemyUnits(unit, maxDistFromEnemy);
        defineEnemyBuildings(unit, maxDistFromEnemy);
    }

    private void defineEnemyUnits(AUnit unit, double maxDistFromEnemy) {
        enemyUnits = unit.enemiesNear()
            .visibleOnMap()
            .realUnitsAndBuildings()
            .nonBuildingsButAllowCombatBuildings()
            .inRadius(maxDistFromEnemy, unit)
            .maxGroundDist(maxDistFromEnemy, unit)
            .havingAtLeastHp(1)
            .havingPosition()
            .effVisibleOrFoggedWithKnownPosition()
            .canBeAttackedBy(unit, maxDistFromEnemy)
            .excludeOverlords();

//        if (Enemy.zerg() && DontAttackOverlords.forbidden(unit)) {
//            enemyUnits = enemyUnits.excludeOverlords();
//        }
    }

    private void defineEnemyBuildings(AUnit unit, double maxDistFromEnemy) {
        enemyBuildings = Select.enemyRealUnits(true, false, true)
            .buildings()
            .inRadius(maxDistFromEnemy, unit)
            .canBeAttackedBy(unit, maxDistFromEnemy);

//        enemyBuildings.print("F");

        // If early in the game, don't attack regular buildings, storm into the base and kill workers/bases
        if (shouldOnlyAttackBases(unit)) {
            enemyBuildings = enemyBuildings.bases();
        }
    }

//    public ATargeting(AUnit unit, Selection enemyUnits, Selection enemyBuildings) {
//        super(unit);
//        this.enemyUnits = enemyUnits;
//        this.enemyBuildings = enemyBuildings;
////        enemyUnits = possibleEnemyUnitsToAttack(unit, maxDistFromEnemy);
//    }

    // =========================================================

    public static AUnit defineBestEnemyToAttack(AUnit unit) {
        return (new ATargeting(unit)).defineBestEnemyToAttack(AttackNearbyEnemies.maxDistToAttack(unit));
    }

    /**
     * For given <b>unit</b> it defines the best close range target from enemy units. The target is not
     * necessarily in the shoot range. Will return <i>null</i> if no enemy can is visible.
     */
    protected AUnit defineBestEnemyToAttack(double maxDistFromEnemy) {
//        if (true) return null;
//        if (unit.hp() <= 18) return ClosestEnemyTargeting.closestUnitFallback(unit, maxDistFromEnemy);
//        if (true) return ClosestEnemyTargeting.fallbackTarget(unit, maxDistFromEnemy);

        AUnit enemy;

        // === Last squad target ===================================

        boolean longNotAttacked = unit.lastAttackFrameMoreThanAgo(30 * 6);
//        if (longNotAttacked && unit.friendsNear().combatUnits().inRadius(3, unit).notEmpty()) {
//        if (longNotAttacked && unit.shieldWound() <= 5) {
//            enemy = ASquadTargeting.useSquadTargetIfPossible(unit);
//            if (
//                enemy != null
//                    && enemy.effVisible()
//                    && enemy.isTargetInWeaponRangeAccordingToGame()
//                    && !enemy.isDeadMan()
//            ) return enemy;
//        }

//        System.err.println("Last=" + enemy + " / Can=" + (enemy != null ? unit.hasWeaponRangeToAttack(enemy, 4) : '-'));

        // =========================================================

//        enemyUnits.print("ALL");
//        enemyUnits.excludeOverlords().print("NO Overlords");

//        if (
//            unit.shotSecondsAgo() >= 7
//                || (longNotAttacked && A.s >= 30)
//        ) {
//            return (new ClosestEnemyTargeting(enemyUnits)).nearestTarget(unit, maxDistFromEnemy);
//        }

        enemy = defineTarget(unit, maxDistFromEnemy);

//        if (enemy != null) System.err.println("@@@@ " + unit.typeWithUnitId() + " / " + unit.hp());

//        if (DEBUG) A.println("A enemy = " + enemy);

//        if (enemy != null && enemy.isAlive() && !unit.canAttackTarget(enemy)) {
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Unit " + unit + " cannot attack " + enemy);
//        }

        if (enemy != null && enemy.isAlive() && unit.canAttackTarget(enemy)) {
//            APainter.paintTextCentered(unit.translateByPixels(0, 25), enemy.name(), Color.Green);
//            if (DEBUG) A.println("B enemy = " + enemy);

            return enemy;
//            if (
//                unit.hasCooldown()
//                    || !enemy.isABuilding()
//                    || unit.canAttackTargetWithBonus(enemy, 1)
//            ) return enemy;
        }

        // Used when something went wrong there ^
        AttackNearbyEnemies.reasonNotToAttack = null;
//        AUnit fallback = ClosestEnemyTargeting.fallbackTarget(unit, maxDistFromEnemy);
        AUnit fallback = null;
        if (DEBUG) A.println("C fallback = " + fallback);
        return fallback;
    }

    // =========================================================

    private AUnit defineTarget(AUnit unit, double maxDistFromEnemy) {
        TargetingWhenDefendingAroundFocus defendingAroundFocus = new TargetingWhenDefendingAroundFocus(unit);
        if (defendingAroundFocus.applies()) {
            unit.setManagerUsed(defendingAroundFocus);
            return defendingAroundFocus.targetToAttack();
        }

        if (unit.isTankSieged()) return (new ATankTargeting(unit)).targetForTank();

        AUnit enemy = selectUnitToAttackByType(unit, maxDistFromEnemy);
//        System.out.println("BASE enemy = " + enemy + " / " + maxDistFromEnemy);

        if (enemy == null && maxDistFromEnemy >= 8) {
            enemy = enemyUnits
                .canBeAttackedBy(unit, 0)
                .nearestTo(unit);
//            if (enemy != null && !unit.isAir()) {
//                ErrorLog.printMaxOncePerMinute(
//                    "DefineTarget fix for " + unit
//                        + ", (was null), chosen " + enemy
//                        + " maxDistFromEnemy = " + maxDistFromEnemy
//                );
//            }
        }


        if (enemy == null) {
            return null;
        }

        if (enemyUnits.inRadius(9, unit).empty()) return enemy;

//        A.errPrintln("BEFORE weakestEnemy = " + enemy + "\n");
        AUnit weakestEnemy = WeakestOfType.selectWeakestEnemyOfType(enemy.type(), unit);
//        A.errPrintln("AFTER weakestEnemy = " + weakestEnemy + "\n");

        return weakestEnemy != null ? weakestEnemy : enemy;
    }

    // =========================================================

    private AUnit selectUnitToAttackByType(AUnit unit, double maxDistFromEnemy) {
        AUnit target;

////        System.err.println("Aaaaaaaa " + Select.enemyRealUnits(true, false, true).size());
//        Select.enemyRealUnits(true, false, true)
////            .print("A")
//            .visibleOnMap()
////            .print("B")
//            .buildings()
////            .print("C")
//            .inRadius(maxDistFromEnemy, unit)
////            .print("D")
//            .canBeAttackedBy(unit, maxDistFromEnemy);
////            .print("E");

//        enemyBuildings.print("G");

//        System.err.println("enemyBuildings = " + enemyBuildings.size());
//        System.err.println("enemyUnits = " + enemyUnits.size());

        if (enemyUnits.empty() && enemyBuildings.empty()) return null;

        // =========================================================

//        if ((target = ATargetingForSpecificUnits.target()) != null) {
//            if (ATargeting.DEBUG) System.out.pri ntln("A = "+ target);
//            return target;
//        }

        // === AIR UNITS due to their mobility use different targeting logic ===

        if (unit.isAir() && unit.canAttackGroundUnits()) {
            target = (new AAirUnitsTargeting(unit)).targetForAirUnit();

//            A.errPrintln("Air target for " + unit + ": " + target);
//            if ((target = AAirUnitsTargeting.targetForAirUnits()) != null) {
//                debug("AirTarget = " + target);
//            }

            return target;
        }

        // === Crucial units =======================================

        if ((target = (new ATargetingCrucial(unit)).target()) != null) {
//            debug("B = "+ target);
            return target;
        }

        // === Important units =====================================

        if ((target = (new ATargetingImportant(unit)).target()) != null) {
//            debug("C = " + target);
            return target;
        }

        // === Standard targets ====================================

        if ((target = (new ATargetingStandard(unit)).target()) != null) {
//            debug("D = "+ target);
            return target;
        }

        // =====

        return target;
    }

    private static boolean shouldOnlyAttackBases(AUnit unit) {
        if (Env.isTesting()) return false;

        return unit.isMissionAttack()
            && A.seconds() <= 650
            && (EnemyUnits.discovered().workers().atMost(4) && UnitsArchive.enemyDestroyedWorkers() <= 5);
    }

    public static void debug(String message) {
        if (ATargeting.DEBUG) {
            A.println(message);
        }
    }

    protected Selection leaderEnemies() {
        if (leaderEnemies != null) return leaderEnemies;
        AUnit leader = unit.squadLeader();
        if (leader == null) return enemyUnits;

        return leaderEnemies = (new ATargeting(leader)).enemyUnits;
    }
}
