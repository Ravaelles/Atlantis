package atlantis.combat.retreating;

import atlantis.combat.eval.OldUnusedCombatEvaluator;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;
import atlantis.util.cache.Cache;
import jfap.CombatEvaluator;

public class ShouldRetreat {

    private static Cache<Boolean> cache = new Cache<>();

    /**
     * If chances to win the skirmish with the Near enemy units aren't favorable, avoid fight and retreat.
     */
    public static boolean shouldRetreat(AUnit u) {
        return cache.get(
                "shouldRetreat:" + u.id(),
                11,
                () -> {
                    // Change unit context to unit
                    AUnit unit = u.squad() != null ? u.squad().centerUnit() : null;
                    if (unit == null) {
                        unit = u;
                    }

                    if (shouldNotConsiderRetreatingNow(unit)) {
                        return false;
                    }

                    if (CombatEvaluator.wouldLose(unit)) {
                        System.out.println(
                            u + " would lose.  REL="
                                + CombatEvaluator.eval(unit, true)
                                + ", ABS=" + CombatEvaluator.eval(unit, false)
                        );
                        RetreatManager.GLOBAL_RETREAT_COUNTER++;
                        return true;
                    }

                    Selection enemies = enemies(unit);

                    if (shouldSmallScaleRetreat(unit, enemies)) {
                        RetreatManager.GLOBAL_RETREAT_COUNTER++;
                        return true;
                    }
//                    if (shouldLargeScaleRetreat(unit, enemies)) {
//                        RetreatManager.GLOBAL_RETREAT_COUNTER++;
//                        return true;
//                    }

//                    if (shouldRetreatDueToSquadMostlyRetreating(unit)) {
//                        unit.addLog("SquadMostlyRetreating");
//                        return true;
//                    }

                    if ("Retreat".equals(unit.tooltip())) {
                        unit.removeTooltip();
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
            if (unit.isRanged() && unit.shieldDamageAtMost(13)) {
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

//    private static boolean shouldLargeScaleRetreat(AUnit unit, Selection enemies) {
//        if (shouldRetreatDueToSquadMostlyRetreating(unit)) {
//            unit.addLog("SquadMostlyRetreating");
//            return true;
//        }
//
////        boolean isSituationFavorable = OldUnusedCombatEvaluator.isSituationFavorable(unit);
////
////        if (!isSituationFavorable) {
////            unit._lastRetreat = AGame.now();
////            unit.setTooltipTactical("Retreat");
////            MissionChanger.notifyThatUnitRetreated(unit);
////            APosition averageEnemyPosition = enemies.units().average();
////
////            if (unit.position().equals(averageEnemyPosition)) {
////                averageEnemyPosition = averageEnemyPosition.translateByPixels(1, 1);
////            }
////
////            if (averageEnemyPosition == null) {
////                return false;
////            }
////
////            return unit.runningManager().runFrom(averageEnemyPosition, 5, Actions.RUN_RETREAT);
////        }
//
//        return false;
//    }

    private static boolean shouldRetreatDueToSquadMostlyRetreating(AUnit unit) {
        Squad squad = unit.squad();
        if (squad == null || squad.size() <= 1 || unit.isMissionDefendOrSparta()) {
            return false;
        }

        if (unit.distToNearestChokeLessThan(5) && unit.combatEvalRelative() <= 4) {
            unit.addLog("ChokeDanger");
            return true;
        }

        int countRunning = squad.selection().countRetreating();
        return countRunning >= 0.5 * squad.size();
    }

    protected static boolean shouldNotConsiderRetreatingNow(AUnit unit) {
        if (unit.isMissionSparta()) {
//            if (unit.mission().allowsToRetreat(unit)) {
//                System.err.println("Sparta allowed " + unit + " to retreat (HP=" + unit.hp() + ")");
//            }
            return !unit.mission().allowsToRetreat(unit);
        }

        if (terran_shouldNotRetreat(unit)) {
            return true;
        }

        if (unit.enemiesNear().tanks().inRadius(5, unit).notEmpty()) {
            unit.addLog("EngageTanks");
            return true;
        }

        AUnit main = Select.main();
        if (main != null) {
            if (main.distTo(unit) <= 8) {
                return true;
            }
        }

        if (A.seconds() <= 400 && OurStrategy.get().isRushOrCheese() && unit.enemiesNear().ranged().empty()) {
            return true;
        }

        if (unit.isMissionDefend() &&
            (
                (Have.main() && unit.distToLessThan(main, 14))
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

    // =========================================================

    private static Selection enemies(AUnit unit) {
        return OldUnusedCombatEvaluator.opposingUnits(unit);
    }

}
