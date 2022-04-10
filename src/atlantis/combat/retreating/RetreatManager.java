package atlantis.combat.retreating;

import atlantis.combat.eval.ACombatEvaluator;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.AMap;
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

            if (runAwayFrom == null && nearEnemies.notEmpty()) {
                System.err.println("Retreat runAwayFrom is NULL, despite:");
                nearEnemies.print("nearEnemies");
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
        if (unit.isMelee()) {
            if (We.protoss() && ProtossRetreating.shouldSmallScaleRetreat(unit, enemies)) {
                return true;
            }
            else if (We.zerg() && ZergRetreating.shouldSmallScaleRetreat(unit, enemies)) {
                return true;
            }
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

    private static boolean shouldLargeScaleRetreat(AUnit unit, Selection enemies) {
        if (shouldRetreatDueToSquadMostlyRetreating(unit)) {
            return true;
        }

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

    private static boolean shouldRetreatDueToSquadMostlyRetreating(AUnit unit) {
        Squad squad = unit.squad();
        if (squad == null || squad.size() <= 1 || unit.isMissionDefendOrSparta()) {
            return false;
        }

        if (unit.distToNearestChokeLessThan(5)) {
            return true;
        }

        int countRunning = squad.selection().countRetreating();
        return countRunning >= 0.5 * squad.size();
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
                () -> ACombatEvaluator.relativeAdvantage(unit) <= 1.8 && ArmyStrength.ourArmyRelativeStrength() <= 250
        );
    }

    // =========================================================

    private static Selection enemies(AUnit unit) {
        return ACombatEvaluator.opposingUnits(unit);
    }

    protected static boolean shouldNotConsiderRetreatingNow(AUnit unit) {
        if (terran_shouldNotRetreat(unit)) {
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

        if (A.seconds() <= 400 && OurStrategy.get().isRushOrCheese() && unit.enemiesNear().ranged().empty()) {
            return true;
        }

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

    private static boolean terran_shouldNotRetreat(AUnit unit) {
        if (unit.isTank() && unit.cooldownRemaining() <= 0) {
            return true;
        }

        if (unit.kitingUnit() && unit.isHealthy()) {
            return true;
        }

        if (unit.isStimmed()) {
            return true;
        }

        return false;
    }
}
