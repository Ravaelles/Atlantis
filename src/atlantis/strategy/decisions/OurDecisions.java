package atlantis.strategy.decisions;

import atlantis.enemy.EnemyInformation;
import atlantis.production.orders.CurrentProductionQueue;
import atlantis.production.orders.ProductionQueue;
import atlantis.strategy.GamePhase;
import atlantis.strategy.OurStrategy;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Cache;

public class OurDecisions {

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

                    return EnemyInformation.enemyStartedWithDefensiveBuilding && OurStrategy.get().goingBio();
                }
        );
    }

    public static boolean shouldBuildBio() {
        return cache.get(
                "buildBio",
                100,
                () -> OurStrategy.get().goingBio()
//                () -> (OurStrategy.get().goingBio() || Count.ourCombatUnits() <= 30)
//                        (!EnemyInformation.enemyStartedWithDefensiveBuilding || Select.ourTerranInfantry().atMost(13))
        );
    }

    public static boolean dontProduceVultures() {
        return cache.get(
                "dontProduceVultures",
                100,
                () -> focusOnTanks() || shouldBuildBio() || Select.ourTerranInfantry().atLeast(4)
        );
    }

    private static boolean focusOnTanks() {
        return cache.get(
                "focusOnTanksOnly",
                100,
                () -> EnemyInformation.enemyStartedWithDefensiveBuilding && GamePhase.isEarlyGame()
        );
    }
}
