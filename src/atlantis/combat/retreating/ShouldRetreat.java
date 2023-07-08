package atlantis.combat.retreating;

//import atlantis.combat.eval.HeuristicCombatEvaluator;

import atlantis.game.A;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.managers.Manager;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class ShouldRetreat extends Manager {

    private static Cache<Boolean> cache = new Cache<>();
    private static TerranShouldNotRetreat terranShouldNotRetreat;
    private static TerranShouldRetreat terranShouldRetreat;

    public ShouldRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public Manager handle() {
        if (shouldRetreat(unit)) {
            return usedManager(this);
        }

        return null;
    }

    /**
     * If chances to win the skirmish with the Near enemy units aren't favorable, avoid fight and retreat.
     */
    public static boolean shouldRetreat(final AUnit unit) {
        return cache.get(
            "shouldRetreat:" + unit.id(),
            17,
            () -> {
                if (A.isUms() && A.supplyUsed() <= 30) return false;

                if (unit.isRunning()) return false;

//                if (TempDontRetreat.temporarilyDontRetreat()) {
//                    return false;
//                }

                // Change unit context to unit
//                Squad squad = unit.squad();
//                unit = squad != null ? squad.leader() : null;

                terranShouldRetreat = new TerranShouldRetreat(unit);
                terranShouldNotRetreat = new TerranShouldNotRetreat(unit);

                if (terranShouldRetreat.shouldRetreat() != null) return true;

                if (terranShouldNotRetreat.shouldNotRetreat() != null) return true;

                if (shouldNotConsiderRetreatingNow(unit)) return false;

//                    if (CombatEvaluator.wouldLose()) {
//                if (situationNotFavorable()) {
////                        System.out.println(
////                            u + " would lose.  REL="
////                                + CombatEvaluator.eval(true)
////                                + ", ABS=" + CombatEvaluator.eval(false)
////                        );
//                    RetreatManager.GLOBAL_RETREAT_COUNTER++;
//                    return usedManager(this, "situationNotFavorable");
//                }

                Selection enemies = enemies(unit);

                if (shouldSmallScaleRetreat(unit, enemies)) {
                    RetreatManager.GLOBAL_RETREAT_COUNTER++;
                    return true;
                }

//                    if (shouldLargeScaleRetreat(enemies)) {
//                        RetreatManager.GLOBAL_RETREAT_COUNTER++;
//                        return true;
//                    }

//                    if (shouldRetreatDueToSquadMostlyRetreating()) {
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
//        if (unit.isMelee()) {
//            if (We.protoss() && ProtossRetreating.shouldSmallScaleRetreat(enemies)) {
//                return true;
//            }
//            else if (We.zerg() && ZergRetreating.shouldSmallScaleRetreat(enemies)) {
//                return true;
//            }
//        }

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

//    private boolean shouldLargeScaleRetreat(, Selection enemies) {
//        if (shouldRetreatDueToSquadMostlyRetreating()) {
//            unit.addLog("SquadMostlyRetreating");
//            return true;
//        }
//
////        boolean isSituationFavorable = OldUnusedCombatEvaluator.isSituationFavorable();
////
////        if (!isSituationFavorable) {
////            unit._lastRetreat = AGame.now();
////            unit.setTooltipTactical("Retreat");
////            MissionChanger.notifyThatUnitRetreated();
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

//    private boolean shouldRetreatDueToSquadMostlyRetreating() {
//        Squad squad = unit.squad();
//        if (squad == null || squad.size() <= 1 || unit.isMissionDefendOrSparta()) {
//            return false;
//        }
//
//        if (unit.distToNearestChokeLessThan(5) && unit.combatEvalRelative() <= 4) {
//            unit.addLog("ChokeDanger");
//            return true;
//        }
//
//        int countRunning = squad.selection().countRetreating();
//        return countRunning >= 0.5 * squad.size();
//    }

    protected static boolean shouldNotConsiderRetreatingNow(AUnit unit) {
        if (A.supplyUsed() >= 182) {
            return true;
        }

        if (unit.isMissionSparta()) {
//            if (unit.mission().allowsToRetreat()) {
//                System.err.println("Sparta allowed " + unit + " to retreat (HP=" + unit.hp() + ")");
//            }
            return !unit.mission().allowsToRetreat(unit);
        }

        if (unit.enemiesNear().tanks().inRadius(5, unit).notEmpty()) {
            unit.addLog("EngageTanks");
            return true;
        }

        AUnit main = Select.main();
        if (main != null) {
            if (main.distTo(unit) <= 8 && unit.hp() >= 19 && unit.noCooldown()) {
                unit.setTooltip("ProtectMain");
                return true;
            }
        }

        if (A.seconds() <= 400 && OurStrategy.get().isRushOrCheese() && unit.enemiesNear().ranged().empty()) {
            unit.setTooltip("Rush");
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
            if (unit.enemiesNear().isEmpty() && unit.cooldownRemaining() <= 7) return true;
        }

        return false;
    }

    // =========================================================

    private static Selection enemies(AUnit unit) {
        return unit.enemiesNear()
            .ranged()
            .canAttack(unit, 6);
    }

}
