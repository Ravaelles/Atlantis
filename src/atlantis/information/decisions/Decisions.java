package atlantis.information.decisions;

import atlantis.game.A;
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
import atlantis.util.Cache;
import atlantis.util.Enemy;

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
                () -> OurStrategy.get().goingBio() && (Count.infantry() <= 16 || A.hasMinerals(650))
//                () -> (OurStrategy.get().goingBio() || Count.ourCombatUnits() <= 30)
//                        (!EnemyInformation.enemyStartedWithCombatBuilding || Select.ourTerranInfantry().atMost(13))
        );
    }

    public static boolean dontProduceVultures() {
        return cache.get(
                "dontProduceVultures",
                100,
                () -> maxFocusOnTanks() || (shouldBuildBio() && !A.hasMinerals(120))
        );
    }

    private static boolean maxFocusOnTanks() {
        return cache.get(
                "maxFocusOnTanks",
                100,
                () -> EnemyInfo.startedWithCombatBuilding && GamePhase.isEarlyGame() && Have.factory() && Have.machineShop()
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

    public static boolean weHaveBunkerAndDefendingCanWeContainNow() {
        if (Enemy.zerg()) {
            if (GamePhase.isEarlyGame()) {
                if (EnemyUnits.selection().countOfType(AUnitType.Zerg_Zergling) >= 9) {
                    return false;
                }

                if (Count.medics() <= 2) {
                    return false;
                }
            }
        }

        return Count.ourCombatUnits() >= 8;
    }
}
