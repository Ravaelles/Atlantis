package atlantis.production.dynamic.protoss;

import atlantis.AGame;
import atlantis.production.dynamic.ADynamicBuildingsManager;
import atlantis.strategy.EnemyStrategy;
import atlantis.strategy.GamePhase;
import atlantis.tech.ATechRequests;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import bwapi.TechType;

public class ProtossDynamicBuildingsManager extends ADynamicBuildingsManager {

    public static void update() {
        if (AGame.notNthGameFrame(7) || noSupply(25)) {
            return;
        }

        gateways();
        forge();
        stargate();
        arbiterTribunal();
    }

    // =========================================================

    private static void gateways() {
        if (
                GamePhase.isEarlyGame()
                && EnemyStrategy.get().isRushOrCheese()
                && Count.ourOfTypeIncludingUnfinished(AUnitType.Protoss_Gateway) < 2
        ) {
            buildIfCanAfford(AUnitType.Protoss_Gateway);
            return;
        }

        buildIfAllBusyButCanAfford(AUnitType.Protoss_Gateway, 70, 0);
    }

    private static void forge() {
        buildToHaveOne(30, AUnitType.Protoss_Forge);
    }

    private static void stargate() {
        buildToHaveOne(70, AUnitType.Protoss_Stargate);
    }

    private static void arbiterTribunal() {
        buildToHaveOne(90, AUnitType.Protoss_Arbiter_Tribunal);

        if (
                hasFree(AUnitType.Protoss_Arbiter_Tribunal)
                && has(AUnitType.Protoss_Arbiter)
        ) {
            ATechRequests.researchTech(TechType.Stasis_Field);
        }
    }
}
