package atlantis.information.decisions;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.terran.ShouldMakeTerranBio;
import atlantis.information.decisions.zerg.ShouldMakeZerglings;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.cache.Cache;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.*;

public class Decisions {

    private static Cache<Boolean> cache = new Cache<>();

    // =========================================================

    public static boolean haveFactories() {
        return cache.get(
            "haveFactories",
            100,
            () -> {
                return OurStrategy.get().goingBio();
            }
        );
    }

    public static boolean wantsToBeAbleToProduceTanksSoon() {
        return cache.get(
            "beAbleToProduceTanks",
            100,
            () -> {
                if (
                    !ProductionQueue.isAtTheTopOfQueue(AUnitType.Terran_Siege_Tank_Tank_Mode, 5)
                        && !ProductionQueue.isAtTheTopOfQueue(AUnitType.Terran_Machine_Shop, 5)
                ) {
                    return false;
                }

                return EnemyInfo.startedWithCombatBuilding && OurStrategy.get().goingBio();
            }
        );
    }

    public static boolean shouldMakeTerranBio() {
        return cache.get(
            "shouldMakeTerranBio",
            100,
            () -> ShouldMakeTerranBio.should()
        );
    }

    public static boolean shouldMakeZerglings() {
        return cache.get(
            "shouldMakeTerranBio",
            100,
            () -> ShouldMakeZerglings.should()
        );
    }

    public static boolean dontProduceVultures() {
//        return true;
        return cache.get(
            "dontProduceVultures",
            100,
            () -> {
                if (true) return true;

                if (
                    GamePhase.isEarlyGame()
                        && Count.vultures() <= 3
                        && EnemyUnits.discovered().ofType(Protoss_Zealot).atLeast(5)
                ) {
                    return false;
                }

                return (maxFocusOnTanks() && Count.vultures() >= 1)
                    || (Enemy.terran() && Count.vultures() >= 1)
                    || (Count.vultures() >= 2 && Count.tanks() < 2)
                    || Count.vultures() >= 15;
            }
//                () -> Count.vultures() >= 1
//                () -> maxFocusOnTanks() || (shouldBuildBio() && Count.vultures() <= 1)
        );
    }

    public static boolean maxFocusOnTanks() {
        return cache.get(
            "maxFocusOnTanks",
            100,
            () ->
//                    GamePhase.isEarlyGame()
                (
                    EnemyInfo.startedWithCombatBuilding
                        || EnemyUnits.discovered().combatBuildings(true).atLeast(2)
                )
//                        && Have.factory() && Have.machineShop()
        );
    }

    public static boolean isEnemyGoingAirAndWeAreNotPreparedEnough() {
        return cache.get(
            "isEnemyGoingAirAndWeAreNotPreparedEnough",
            100,
            () -> {
                if (EnemyStrategy.get().isAirUnits()) {
                    if (Count.ourStrictlyAntiAir() <= 10 || ArmyStrength.weAreWeaker()) {
                        return true;
                    }
                }
                return false;
            }
        );
    }

    public static boolean weHaveBunkerAndDontHaveToDefendAnyLonger() {
        if (Enemy.zerg()) {
            if (GamePhase.isEarlyGame()) {
                if (EnemyUnits.discovered().countOfType(AUnitType.Zerg_Zergling) >= 9) {
                    return Count.ourCombatUnits() >= 13;
                }

                if (Count.medics() <= 3) {
                    return false;
                }
            }
        }

        return Count.ourCombatUnits() >= 8;
    }

    public static boolean buildRoboticsFacility() {
        if (Have.roboticsFacility() || Have.no(Protoss_Forge)) {
            return false;
        }

        if (EnemyInfo.hasHiddenUnits()) {
//            System.err.println("roboticsFacility because hasHiddenUnits");
            return true;
        }
        if (A.supplyUsed() <= 44 && enemyStrategyIsRushOrCheese()) {
            return false;
        }
        if (A.supplyUsed() <= 46 && Have.cannon()) {
            return false;
        }

//        System.err.println("----- buildRoboticsFacility OK" );
//        System.err.println("EnemyStrategy.get().isRushOrCheese() = " + EnemyStrategy.get().isRushOrCheese());
//        System.err.println("EnemyStrategy.get().isRush() = " + EnemyStrategy.get().isRush());

        return true;
    }

    public static int minZealotsAgainstEnemyRush() {
        if (Enemy.protoss()) return enemyStrategyIsRushOrCheese() ? 4 : 3;
        if (Enemy.terran()) return 1;
        return 5;
    }

    public static boolean needToProduceZealotsNow() {
        int zealots = Count.zealots();

        // Early game
        if (GamePhase.isEarlyGame()) {
            if (
                enemyStrategyIsRushOrCheese()
                && zealots < minZealotsAgainstEnemyRush()
            ) {
                return true;
            }

            if (zealots <= 1 || (A.hasMinerals(225) && Have.free(Protoss_Gateway))) {
                return true;
            }
        }

        // Mid + Late game
        else {
            if (zealots <= 1) {
                return true;
            }

            if (AGame.canAffordWithReserved(130, 0)) {
                return true;
            }
        }

        return false;
    }

    protected static boolean enemyStrategyIsRushOrCheese() {
        return EnemyStrategy.get().isRushOrCheese();
    }
}
