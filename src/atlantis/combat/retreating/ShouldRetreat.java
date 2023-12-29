package atlantis.combat.retreating;

//import atlantis.combat.eval.HeuristicCombatEvaluator;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class ShouldRetreat extends Manager {
    private static Cache<Boolean> cache = new Cache<>();
    private static TerranShouldNotRetreat terranShouldNotRetreat;
    private static TerranInfantryShouldRetreat terranShouldRetreat;

    public ShouldRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return true;
    }

    @Override
    protected Manager handle() {
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

//                if (unit.isRunning()) return false;
                if (shouldNotRunInMissionDefend(unit)) {
                    unit.addLog("NoRunInDefend");
                    return false;
                }

//                if (TempDontRetreat.temporarilyDontRetreat()) {
//                    return false;
//                }

                // Change unit context to unit
//                Squad squad = unit.squad();
//                unit = squad != null ? squad.leader() : null;

                terranShouldRetreat = new TerranInfantryShouldRetreat(unit);
                terranShouldNotRetreat = new TerranShouldNotRetreat(unit);

                if (terranShouldRetreat.shouldRetreat() != null) return true;
                if (terranShouldNotRetreat.shouldNotRetreat()) return false;

                if (shouldNotConsiderRetreatingNow(unit)) return false;

//                    if (CombatEvaluator.wouldLose()) {
//                if (situationNotFavorable()) {
////                        System.out.pri ntln(
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

    private static boolean shouldNotRunInMissionDefend(AUnit unit) {
        return unit.isMissionDefend()
            && unit.hp() >= 25
            && unit.cooldown() >= 2
            && unit.enemiesNear().melee().nearestToDistMore(unit, 2.1)
            && unit.friendsNear().buildings().nearestToDistLess(unit, 4);
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
            if (unit.isRanged() && unit.shieldDamageAtMost(13)) return false;
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

    protected static boolean shouldNotConsiderRetreatingNow(AUnit unit) {
        if (A.supplyUsed() >= 182) return true;

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
                unit.addLog("ProtectMain");
                return true;
            }
        }

        if (A.seconds() <= 400 && OurStrategy.get().isRushOrCheese() && unit.enemiesNear().ranged().empty()) {
            unit.addLog("Rush");
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
