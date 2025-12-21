package atlantis.production.dynamic.expansion.protoss;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.information.strategy.Strategy;
import atlantis.map.base.BaseLocations;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.dynamic.protoss.prioritize.PrioritizeGatewaysVsProtoss;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class ProtossShouldExpandToNaturalBase extends ProtossShouldExpand {
    protected static boolean forSecondBase() {
        int seconds = A.seconds();
        int armyStrength = Army.strength();

        if (delayNaturalVsProtoss()) return no("DelayNaturalVsProtoss");

        if (dontExpandWhenNaturalOverwhelmed()) return no("NaturalOverwhelmed");

        if (A.minerals() <= 550) {
            if (enemyWentHiddenUnitsAndWeHaveNoObservers()) return no("NoObservers");
            if (PrioritizeGatewaysVsProtoss.shouldPrioritizeOverExpanding()) return no("PrioritizeGates");
        }

        if (Count.basesWithUnfinished() <= 1 && seconds >= 10.5 * 60) return yes("GettingLate");
        if (enemyGoesCombatBuildingsEarly()) return yes("EnemyManyEarlyCB");

        if (cautionAgainstZealotRush()) return no("CautionZealots");
        if (cautionAgainstZergArmy()) return no("CautionZerg");

        if (delayNaturalVsZerg()) return no("DelayNaturalVsZerg");

        int workers = Count.workers();
        int basesWithUnfinished = Count.basesWithUnfinished();
        if (bases <= 1 && basesInProduction <= 0 && Count.gatewaysWithUnfinished() >= 4) {
            int minNeeded = (Count.ourCombatUnits() <= 14 ? 442 : 360)
                + (Army.strength() <= 150 ? 80 : 0);

            if (A.hasMinerals(minNeeded) && workers >= 18 && CountInQueue.bases() == 0) {
                Count.clearCache();
                if (basesWithUnfinished == 0) {
                    return yes("2ndManyMinerals(" + A.minerals() + ")");
                }
            }
        }

        if (notEnoughGateways()) return no("NotEnoughGates");
        if (tooFewArmy()) return no("TooFewUnits");
        if (mainChokeOverwhelmed()) return no("MainChokeOverwhelm");

        if (
            !A.hasMinerals(500) && !Have.existingOrUnfinished(AUnitType.Protoss_Cybernetics_Core)
        ) return no("CoreFirst");

        if (armyStrength <= 90) return no("TooWeak");
        if (seconds <= 500 && Strategy.get().isRushOrCheese()) no("RushPlan");
        if (enemyHasNoCombatBuilding()) return no("NoEnemyCB");
        if (basesWithUnfinished <= 2 && workers <= basesWithUnfinished * 21) return no("TooFewWorkers(" + workers + ")");

//        System.err.println(A.now() + " - armyStrength ok to expand = " + armyStrength);

        if ((Army.strength() >= 145 && Count.ourCombatUnits() >= 10 || A.hasMinerals(650))) {
            if (A.hasMinerals(350) && (workers >= 44 || workers * Count.basesWithUnfinished() >= 24)) {
                Count.clearCache();
                if (basesInProduction == 0) {
                    int gateways = Count.gatewaysWithUnfinished();
                    if ((workers / gateways) <= 7 || Count.freeGateways() <= 1) {
                        return yes("ManyWorkers(" + workers + ")");
                    }
                }
            }
            if (manyGateways()) return yes("ManyGateways");
        }

        boolean secondsAllow = (
            (seconds >= 400 && Count.ourCombatUnits() >= 20)
                || (seconds >= 520 && Count.ourCombatUnits() >= 8)
        );

        if (secondsAllow) return yes("StrongEnough");
        if (basesInProduction == 0 && A.canAfford(360, 0)) return yes("CanAfford");

        if (seconds <= 400 && armyStrength < 100) return no("Weak");

        return no("JustDont");
    }

    private static boolean delayNaturalVsZerg() {
        if (!Enemy.zerg()) return false;

        return Count.ourCombatUnits() <= 11;
    }

    private static boolean delayNaturalVsProtoss() {
        if (!Enemy.protoss()) return false;

        return EnemyInfo.combatBuildingsAntiLand() == 0 && Count.ourCombatUnits() <= 15;
    }

    private static boolean dontExpandWhenNaturalOverwhelmed() {
        APosition natural = BaseLocations.natural();
        if (natural == null) return false;

        return Army.strength() <= 160 && Select.enemy().countInRadius(9, natural) >= 2;
    }

    private static boolean enemyWentHiddenUnitsAndWeHaveNoObservers() {
        if (Count.observers() > 0) return false;

        return EnemyInfo.goesOrHasHiddenUnits();
    }

    private static boolean cautionAgainstZergArmy() {
        if (!Enemy.zerg()) return false;
        if (A.hasMinerals(500)) return false;

        if (Count.ourCombatUnits() <= 9 && EnemyUnits.combatUnits() >= 6) return true;

        return Army.strength() <= 140 && Count.ourCombatUnits() <= 13;
    }

    private static boolean notEnoughGateways() {
        int gateways = Count.gateways();

        if (Enemy.protoss()) {
            if (EnemyInfo.combatBuildingsAntiLand() == 0) {
                return gateways <= 6 || Army.strength() >= 140;
            }

            return gateways <= 1;
        }

        if (Enemy.zerg()) {
            return gateways <= 4 && Army.strength() < 170 && !A.hasMinerals(344);
        }

        return gateways <= 1;
    }

    private static boolean enemyGoesCombatBuildingsEarly() {
        int secondLimit = 420;

        if (Enemy.terran()) {
            return A.s <= secondLimit && EnemyInfo.combatBuildingsAntiLand() >= 1;
        }

        else if (Enemy.protoss()) {
            return A.s <= secondLimit
                && EnemyInfo.combatBuildingsAntiLand() >= 2
                && !EnemyInfo.goesOrHasHiddenUnits();
        }

        else if (Enemy.zerg()) {
            return A.s <= secondLimit
                && EnemyInfo.combatBuildingsAntiLand() >= 3
                && !EnemyInfo.goesOrHasHiddenUnits();
        }

        return false;
    }

    private static boolean tooFewArmy() {
        if (A.hasMinerals(550)) return false;

        return Count.zealots() <= 9 && Count.dragoons() <= 6;
    }

    private static boolean cautionAgainstZealotRush() {
        if (!Enemy.protoss()) return false;
        if (EnemyUnits.discovered().dragoons().notEmpty()) return false;
        if (A.hasMinerals(400)) return false;

        return Count.dragoons() <= 5;
    }

    private static boolean mainChokeOverwhelmed() {
        AChoke mainChoke = Chokes.mainChoke();
        if (mainChoke == null) return false;

        return EnemyUnits.discovered().inRadius(AUnit.NEAR_DIST, mainChoke).atLeast(4);
    }

    private static boolean enemyHasNoCombatBuilding() {
        return !A.hasMinerals(380) && EnemyUnits.discovered().combatBuildingsAntiLand().empty();
    }

    private static boolean manyGateways() {
        return A.hasMinerals(230 + (Strategy.get().isRushOrCheese() ? 120 : 0))
            && Count.gateways() >= 3;
    }
}
