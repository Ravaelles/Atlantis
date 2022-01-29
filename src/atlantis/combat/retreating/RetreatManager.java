package atlantis.combat.retreating;

import atlantis.AGame;
import atlantis.combat.eval.ACombatEvaluator;
import atlantis.combat.missions.MissionChanger;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.Cache;

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
                3,
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
        if (unit.isHealthy()) {
            return false;
        }

        double radius = 1.2;
        Selection friends = unit.friendsNearby().inRadius(radius, unit);
        Selection veryCloseEnemies = enemies.inRadius(radius, unit);

        if (veryCloseEnemies.totalHp() > friends.totalHp()) {
            return true;
        }

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
//        if (unit.isHealthy()) {
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
