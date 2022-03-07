package atlantis.information.decisions;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Cache;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Protoss_Zealot;

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

    public static boolean shouldBuildBio() {
        return cache.get(
            "buildBio",
            100,
            () ->
                (EnemyInfo.isDoingEarlyGamePush())
                    ||
                    (
                        (
                            !maxFocusOnTanks()
                                &&
                                (
                                    (OurStrategy.get().goingBio() || EnemyStrategy.get().isAirUnits())
                                        && (Count.infantry() <= 18 || AGame.canAffordWithReserved(50, 0))
                                )
                        )
                            || !GamePhase.isEarlyGame()
                    )
            || (A.hasMinerals(290) && OurStrategy.get().goingBio() && Count.infantry() <= 12)
//                () -> (OurStrategy.get().goingBio() || Count.ourCombatUnits() <= 30)
//                        (!EnemyInformation.enemyStartedWithCombatBuilding || Select.ourTerranInfantry().atMost(13))
        );
    }

    public static boolean dontProduceVultures() {
//        return true;
        return cache.get(
            "dontProduceVultures",
            100,
            () -> {
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

    private static boolean maxFocusOnTanks() {
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
}
