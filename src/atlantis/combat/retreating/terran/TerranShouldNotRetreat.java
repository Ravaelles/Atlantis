package atlantis.combat.retreating.terran;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.strategy.Strategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.HasUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;

import static atlantis.units.AUnitType.Zerg_Hydralisk;

public class TerranShouldNotRetreat extends HasUnit {
    private Decision decision;

    public TerranShouldNotRetreat(AUnit unit) {
        super(unit);
    }

//    @Override
//    public boolean applies() {
//        if (!We.terran()) return false;
//
//        return shouldNotRetreat();
////        return unit.enemiesNear().visibleOnMap().notEmpty();
//    }

//    @Override
//    protected Manager handle() {
//        if ((new AttackNearbyEnemies(unit)).forceHandle() != null) {
//            return this;
//        }
//
//        return null;
//    }

    public boolean shouldNotRetreat() {
        if ((decision = whenNoEnemyRanged()).notIndifferent()) {
            if (decision.isTrue()) {
                unit.addLog("NoEnemyRanged");
                return true;
            }
            return decision.toBoolean();
        }

        if ((decision = asTank()).notIndifferent()) {
            if (decision.isTrue()) unit.addLog("BraveTank");
            return decision.toBoolean();
        }

        if (doNotRetreatNearBase(unit)) {
            unit.addLog("FightNearBase");
            return false;
        }

        if (doNotRetreatNearBunker(unit)) {
            unit.addLog("FightNearBunker");
            return false;
        }

        if (dontAgainstHydras(unit)) {
            unit.addLog("FightHydras");
            return false;
        }

        if (shouldNotRunInMissionDefend(unit)) {
            unit.addLog("NoRunInDefend");
            return false;
        }

        if (unit.kitingUnit() && unit.isHealthy() && unit.meleeEnemiesNearCount(1.8) <= 0) {
            unit.addLog("BraveKite");
            return true;
        }

        if (unit.isStimmed() && (unit.hp() >= 17 || unit.noCooldown()) && unit.enemiesNearInRadius(1.8) <= 2) {
            unit.addLog("BraveStim");
            return true;
        }

//        if (unit.friendsInRadius(4).count() >= 8) {
//            unit.setTooltip("BraveRetard");
//            return true;
//        }

        return false;
    }

    private Decision whenNoEnemyRanged() {
        if (
            unit.eval() >= 0.6
                && unit.enemiesNear().ranged().inRadius(13.5, unit).isEmpty()
                && (!A.isUms() || Select.ourBuildings().notEmpty())
        ) {
            return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    private Decision asTank() {
        if (!unit.isTank()) return Decision.INDIFFERENT;

        if (
            unit.woundPercentMax(20)
//            && unit.cooldownRemaining() <= 2
                && unit.enemiesNear().inRadius(6.5, unit).atMost(1)
        ) return Decision.TRUE;

        return Decision.FALSE;
    }

    private boolean dontAgainstHydras(AUnit unit) {
        if (!unit.isInfantry()) return false;
        if (unit.hp() <= 27) return false;

        if (!unit.nearestEnemyIs(Zerg_Hydralisk)) return false;

        return unit.eval() >= 0.7 || (unit.hp() >= 23 && unit.hasMedicInRange());
    }

    private boolean doNotRetreatNearBunker(AUnit unit) {
        if (
//            unit.hp() >= 17
            unit.friendsNear().bunkers().inRadius(6, unit).notEmpty()
                && unit.cooldown() <= 7
                && unit.meleeEnemiesNearCount(3.2) <= 1
                && !unit.isAction(Actions.LOAD)
        ) {
            return true;
        }

        return false;
    }

    private static boolean doNotRetreatNearBase(AUnit unit) {
        if (
            unit.hp() >= 30
                && unit.friendsNear().bases().inRadius(4.5, unit).notEmpty()
                && unit.meleeEnemiesNearCount(3) <= 1
        ) {
            return true;
        }

        return false;
    }

    private static boolean shouldNotRunInMissionDefend(AUnit unit) {
        return unit.isMissionDefend()
            && unit.hp() >= 25
            && unit.cooldown() >= 2
            && unit.friendsNear().combatUnits().inRadius(5, unit).count() >= 1
            && unit.enemiesNear().melee().nearestToDistMore(unit, 2.1)
            && unit.friendsNear().buildings().nearestToDistLess(unit, 3);
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

        if (
            Strategy.get().isRushOrCheese()
                && A.seconds() <= 400
                && unit.isGroundUnit()
                && unit.enemiesNear().ranged().empty()
        ) {
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

    public boolean shouldRetreat() {
        if (unit.isTerranInfantry()) {
            if (!unit.mission().isMissionDefend()) {
                if (unit.enemiesNear().ranged().notEmpty() && unit.friendsNear().atMost(4) && unit.eval() <= 2) {
                    unit.setTooltipTactical("BewareRanged");
                    return true;
                }
            }
        }

        return false;
    }
}
