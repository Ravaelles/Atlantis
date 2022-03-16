package atlantis.combat.retreating;

import atlantis.combat.eval.ACombatEvaluator;
import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.MissionChanger;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;
import atlantis.util.cache.Cache;
import atlantis.util.Enemy;
import bwapi.Position;

public class RetreatManager {

    public static int GLOBAL_RETREAT_COUNTER = 0;
    private static Cache<Boolean> cache = new Cache<>();

    // =========================================================

    public static boolean handleRetreat(AUnit unit) {
        if (shouldRetreat(unit)) {
            Selection nearEnemies = unit.enemiesNear().canAttack(unit, true, true, 5);
            HasPosition runAwayFrom = nearEnemies.center();
            if (runAwayFrom == null) {
                runAwayFrom = nearEnemies.first();
            }

            if (runAwayFrom != null && unit.runningManager().runFrom(runAwayFrom, 4, Actions.RUN_RETREAT)) {
                unit.addLog("HandledRetreat");
                return true;
            }
        }

        return false;
    }

    /**
     * If chances to win the skirmish with the Near enemy units aren't favorable, avoid fight and retreat.
     */
    public static boolean shouldRetreat(AUnit unit) {
        return cache.get(
                "shouldRetreat:" + unit.id(),
                11,
                () -> {
                    if (shouldNotConsiderRetreatingNow(unit)) {
                        return false;
                    }

                    Selection enemies = enemies(unit);

                    if (shouldSmallScaleRetreat(unit, enemies)) {
                        GLOBAL_RETREAT_COUNTER++;
                        return true;
                    }
                    if (shouldLargeScaleRetreat(unit, enemies)) {
                        GLOBAL_RETREAT_COUNTER++;
                        return true;
                    }

                    return false;
                }
        );
    }

    private static boolean shouldSmallScaleRetreat(AUnit unit, Selection enemies) {
        if (unit.isMelee() && formationMeleeUnits(unit, enemies)) {
            return true;
        }

        if (We.protoss()) {
            if (unit.isRanged() && unit.isHealthy()) {
                return false;
            }
        }

        if (We.terran()) {
            if (Enemy.terran()) {
                if (unit.isMarine()) {
                    if (unit.friendsNear().inRadius(5, unit).count() < unit.enemiesNear().inRadius(5, unit).count()) {
                        unit.addLog("MvM-SmRetreat");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean formationMeleeUnits(AUnit unit, Selection enemies) {
        double radius = 1.2;

        // =========================================================

        AUnit enemy = enemies.nearestTo(unit);
        int enemiesNear = enemies.inRadius(2, unit).count();
        int ourCount;
        if (enemy != null) {
            ourCount = enemy.enemiesNear().inRadius(radius, unit).count();
        } else {
            ourCount = unit.friendsNear().inRadius(0.6, unit).count();
        }

        if (ourCount == enemiesNear && unit.isZealot() && unit.hpMoreThan(36) && unit.isMissionDefend()) {
            unit.setTooltip("Homeland!", true);
            unit.addLog("Homeland!");
            return false;
        }

        if (ourCount <= enemiesNear && unit.friendsNear().inRadius(5, unit).atLeast(2)) {
//        Selection enemiesAroundEnemy = enemy.friendsNear().inRadius(radius, unit);
//        if (oursAroundEnemy.count() > enemiesAroundEnemy.count()) {
            if (unit.enemiesNear().inRadius(7, unit).onlyMelee()) {
                unit.setTooltip("RetreatingB", false);
                unit.addLog("RetreatingB");
                return true;
            }
        }

        if (Enemy.protoss() && applyZealotVsZealotFix(unit, enemies)) {
            unit.setTooltip("RetreatingZ", false);
            unit.addLog("RetreatingZ");
            return true;
        }

        // =========================================================

        Selection friends = unit.friendsNear().inRadius(radius, unit);
        Selection veryCloseEnemies = enemies.inRadius(radius, unit);

        if (veryCloseEnemies.totalHp() > friends.totalHp()) {
            unit.setTooltip("RetreatingA", false);
            unit.addLog("RetreatingA");
            return true;
        }

        // =========================================================

        return false;
    }

    private static boolean applyZealotVsZealotFix(AUnit unit, Selection enemies) {
        if (unit.friendsNear().ofType(AUnitType.Protoss_Photon_Cannon).inRadius(3.8, unit).notEmpty()) {
            return false;
        }

        int ourZealots = unit.friendsNear().ofType(AUnitType.Protoss_Zealot).inRadius(1.4, unit).count();
        int enemyZealots = enemies.ofType(AUnitType.Protoss_Zealot).inRadius(1.4, unit).count();

        if (ourZealots < enemyZealots) {
            return true;
        }

//        if (ourZealots < enemyZealots) {
//            return true;
//        }

        return false;
    }

    private static boolean shouldLargeScaleRetreat(AUnit unit, Selection enemies) {
        boolean isSituationFavorable = ACombatEvaluator.isSituationFavorable(unit);

        if (!isSituationFavorable) {
            unit._lastRetreat = AGame.now();
            unit.setTooltipTactical("Retreat");
            MissionChanger.notifyThatUnitRetreated(unit);
            APosition averageEnemyPosition = enemies.units().average();

            if (unit.position().equals(averageEnemyPosition)) {
                averageEnemyPosition = averageEnemyPosition.translateByPixels(1, 1);
            }

            if (averageEnemyPosition == null) {
                return false;
            }

            return unit.runningManager().runFrom(averageEnemyPosition, 5, Actions.RUN_RETREAT);
        }

        if ("Retreat".equals(unit.tooltip())) {
            unit.removeTooltip();
        }

        return false;
    }

    public static boolean getCachedShouldRetreat(AUnit unit) {
        return cache.has("shouldRetreat:" + unit.id()) && cache.get("shouldRetreat:" + unit.id());
    }

    /**
     * Calculated per unit squad, not per unit.
     */
    public static boolean shouldNotEngageCombatBuilding(AUnit unit) {
        if (unit.squad() == null) {
            return false;
        }

        return cache.get(
                "shouldNotEngageCombatBuilding:" + unit.squad().name(),
                10,
                () -> ACombatEvaluator.relativeAdvantage(unit) <= 1.8
        );
    }

    // =========================================================

    private static Selection enemies(AUnit unit) {
        return ACombatEvaluator.opposingUnits(unit);
    }

    protected static boolean shouldNotConsiderRetreatingNow(AUnit unit) {
        if (unit.kitingUnit() && unit.isHealthy()) {
            return true;
        }

        if (unit.isStimmed()) {
            return true;
        }

        if (unit.enemiesNear().tanks().inRadius(5, unit).notEmpty()) {
            unit.addLog("EngageTanks");
            return true;
        }

        if (unit.isMissionSparta()) {
//            if (unit.mission().allowsToRetreat(unit)) {
//                System.err.println("Sparta allowed " + unit + " to retreat (HP=" + unit.hp() + ")");
//            }
            return !unit.mission().allowsToRetreat(unit);
        }

//        if (unit.isMissionDefend() && (unit.isMelee() || unit.woundPercent() <= 10)) {
//            AFocusPoint focusPoint = unit.squad().mission().focusPoint();
//            return focusPoint != null && unit.distTo(focusPoint) <= 3;
//        }

//        if (unit.hpLessThan(Enemy.protoss() ? 33 : 16)) {
//            return false;
//        }


//        if (
//            unit.isAttacking()
////                && unit.hpMoreThan(32)
//                && unit.lastActionLessThanAgo(5)
//                && unit.lastAttackFrameMoreThanAgo(20)
//        ) {
//            return true;
//        }
//
//        if (!unit.isAttacking() && unit.lastAttackFrameMoreThanAgo(80) && unit.lastUnderAttackLessThanAgo(80)) {
//            return true;
//        }

        if (unit.isMissionDefend() &&
            (
                (Have.main() && unit.distToLessThan(Select.main(), 14))
                || Select.ourOfType(AUnitType.Zerg_Sunken_Colony).inRadius(4.9, unit).isNotEmpty()
            )
        ) {
            return unit.hp() >= 17;
        }

        if (unit.type().isReaver()) {
            return unit.enemiesNear().isEmpty() && unit.cooldownRemaining() <= 7;
        }

        return false;
    }
}
