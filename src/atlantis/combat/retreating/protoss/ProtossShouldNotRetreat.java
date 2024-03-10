package atlantis.combat.retreating.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ProtossShouldNotRetreat extends Manager {
    public ProtossShouldNotRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.terran()) return false;
        if (unit.isTank() && (unit.isWounded() || unit.enemiesNear().inRadius(4, unit).notEmpty())) return false;

        return unit.enemiesNear().visibleOnMap().notEmpty();
    }

    @Override
    protected Manager handle() {
        if (shouldNotRetreat()) {
            if ((new AttackNearbyEnemies(unit)).invoke(this) != null) {
                return this;
            }
        }

        return null;
    }

    public boolean shouldNotRetreat() {
        if (shouldNotRunInMissionDefend(unit)) {
            unit.addLog("NoRunInDefend");
            return false;
        }

        if (unit.isTank() && unit.woundPercentMax(20) && unit.cooldownRemaining() <= 0) {
            unit.addLog("BraveTank");
            return true;
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

        if (
            unit.isMissionAttack()
                && unit.enemiesNear().combatBuildingsAntiLand().inRadius(2, unit).notEmpty()
        ) {
            unit.addLog("AttackCBuilding");
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
            OurStrategy.get().isRushOrCheese()
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
        if (!unit.mission().isMissionDefend()) {
            if (
                unit.enemiesNear().ranged().notEmpty()
                    && unit.friendsNear().atMost(4)
                    && unit.combatEvalRelative() <= 2
            ) {
                unit.setTooltipTactical("BewareRanged");
                return true;
            }
        }

        return false;
    }
}
