package atlantis.production.dynamic.protoss;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.tech.ATechRequests;
import atlantis.production.dynamic.ADynamicBuildingsManager;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import bwapi.TechType;

public class ProtossDynamicBuildingsManager extends ADynamicBuildingsManager {

    public static void update() {
        if (AGame.notNthGameFrame(7) || noSupply(25)) {
            return;
        }

        arbiterTribunal();
        stargate();
        observatory();
        roboticsSupportBay();
        roboticsFacility();
        forge();
        gateways();
    }

    // =========================================================

    private static void roboticsSupportBay() {
        if (!A.supplyUsed(60)) {
            return;
        }

        if (Have.no(AUnitType.Protoss_Robotics_Facility) || Have.a(AUnitType.Protoss_Robotics_Support_Bay)) {
            return;
        }

        if (Have.notEvenInPlans(AUnitType.Protoss_Robotics_Support_Bay)) {
            buildNow(AUnitType.Protoss_Robotics_Support_Bay);
        }
    }

    private static void observatory() {
        if (!Have.no(AUnitType.Protoss_Observatory)) {
            return;
        }

        if (Count.includingPlanned(AUnitType.Protoss_Observatory) == 0) {
            buildNow(AUnitType.Protoss_Observatory);
        }
    }

    private static void roboticsFacility() {
        if (!A.supplyUsed(37) && !EnemyInfo.hasHiddenUnits()) {
            return;
        }
        if (!A.supplyUsed(44) && Have.cannon()) {
            return;
        }

        if (Count.includingPlanned(AUnitType.Protoss_Robotics_Facility) == 0) {
            buildNow(AUnitType.Protoss_Robotics_Facility);
        }
    }

    private static void gateways() {
        if (
                GamePhase.isEarlyGame()
                && EnemyStrategy.get().isRushOrCheese()
                && Count.ourOfTypeIncludingUnfinished(AUnitType.Protoss_Gateway) <= (A.hasMinerals(250) ? 2 : 1)
        ) {
            buildIfHaveMineralsAndGas(AUnitType.Protoss_Gateway);
            return;
        }

        buildIfAllBusyButCanAfford(AUnitType.Protoss_Gateway, 90, 0);
    }

    private static void forge() {
        buildToHaveOne(36, AUnitType.Protoss_Forge);
    }

    private static void stargate() {
        buildToHaveOne(80, AUnitType.Protoss_Stargate);
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
