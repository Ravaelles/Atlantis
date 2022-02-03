package atlantis.combat.retreating;

import atlantis.combat.eval.ACombatEvaluator;
import atlantis.combat.missions.MissionChanger;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.Cache;
import atlantis.util.Enemy;

public class RetreatManager {

    public static int GLOBAL_RETREAT_COUNTER = 0;
    private static Cache<Boolean> cache = new Cache<>();

    // =========================================================

    /**
     * If chances to win the skirmish with the nearby enemy units aren't favorable, avoid fight and retreat.
     */
    public static boolean shouldRetreat(AUnit unit) {
        return cache.get(
                "shouldRetreat:" + unit.id(),
                35,
                () -> {
                    if (shouldNotConsiderRetreatingNow(unit)) {
                        return false;
                    }

                    Selection enemies = enemies(unit);

                    if (shouldSmallScaleRetreat(unit, enemies)) {
                        return true;
                    }
                    if (shouldLargeScaleRetreat(unit, enemies)) {
                        return true;
                    }

                    return false;
                }
        );
    }

    private static boolean shouldSmallScaleRetreat(AUnit unit, Selection enemies) {
        if (!unit.isTerran() && unit.isRanged() && unit.isHealthy()) {
            return false;
        }

        if (unit.isMelee() && formationMeleeUnits(unit, enemies)) {
            return true;
        }

        return false;
    }

    private static boolean formationMeleeUnits(AUnit unit, Selection enemies) {
        double radius = 1.2;

        // =========================================================

        AUnit enemy = enemies.nearestTo(unit);
        int enemiesNearby = enemies.inRadius(2, unit).count();
        int ourCount;
        if (enemy != null) {
            ourCount = enemy.enemiesNearby().inRadius(radius, unit).count();
        } else {
            ourCount = unit.friendsNearby().inRadius(0.6, unit).count();
        }

        if (ourCount == enemiesNearby && unit.isZealot() && unit.hpMoreThan(36) && unit.isMissionDefend()) {
            unit.setTooltip("Homeland!", true);
            unit.addLog("Homeland!");
            return false;
        }

        if (ourCount <= enemiesNearby && unit.friendsNearby().inRadius(5, unit).atLeast(2)) {
//        Selection enemiesAroundEnemy = enemy.friendsNearby().inRadius(radius, unit);
//        if (oursAroundEnemy.count() > enemiesAroundEnemy.count()) {
            unit.setTooltip("FormationB", false);
            unit.addLog("FormationB");
            return true;
        }

        if (Enemy.protoss() && applyZealotVsZealotFix(unit, enemies)) {
            unit.setTooltip("FormationZ", false);
            unit.addLog("FormationZ");
            return true;
        }

        // =========================================================

        Selection friends = unit.friendsNearby().inRadius(radius, unit);
        Selection veryCloseEnemies = enemies.inRadius(radius, unit);

        if (veryCloseEnemies.totalHp() > friends.totalHp()) {
            unit.setTooltip("FormationA", false);
            unit.addLog("FormationA");
            return true;
        }

        // =========================================================

        return false;
    }

    private static boolean applyZealotVsZealotFix(AUnit unit, Selection enemies) {
        int ourZealots = unit.friendsNearby().ofType(AUnitType.Protoss_Zealot).inRadius(1.4, unit).count();
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
            GLOBAL_RETREAT_COUNTER++;
            unit.setTooltipTactical("Retreat");
            MissionChanger.notifyThatUnitRetreated(unit);
            APosition averageEnemyPosition = enemies.units().average();

            if (unit.position().equals(averageEnemyPosition)) {
                averageEnemyPosition = averageEnemyPosition.translateByPixels(1, 1);
            }

            if (averageEnemyPosition == null) {
                return false;
            }

            return unit.runningManager().runFrom(averageEnemyPosition, 3.5, Actions.RUN_RETREAT);
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
                () -> ACombatEvaluator.relativeAdvantage(unit) <= 1.7
//                () -> ACombatEvaluator.relativeAdvantage(unit) <= 0.6
        );
    }

    // =========================================================

    private static Selection enemies(AUnit unit) {
        return ACombatEvaluator.opposingUnits(unit);
    }

    protected static boolean shouldNotConsiderRetreatingNow(AUnit unit) {
        if (unit.isRanged() && unit.isHealthy()) {
            return true;
        }

//        if (unit.mission() != null && unit.isMissionDefend()) {
//            return true;
//        }

//        if (!unit.woundPercent(15) && unit.isMissionDefend()) {
//            return true;
//        }

        if (unit.isStimmed()) {
            return true;
        }

        if (unit.type().isReaver()) {
            return unit.enemiesNearby().isEmpty() && unit.cooldownRemaining() <= 7;
        }

        return false;
    }

}
