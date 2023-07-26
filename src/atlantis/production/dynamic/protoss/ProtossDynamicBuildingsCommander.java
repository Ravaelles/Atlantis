package atlantis.production.dynamic.protoss;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.tech.ATechRequests;
import atlantis.production.dynamic.DynamicBuildingsCommander;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import bwapi.TechType;

import static atlantis.units.AUnitType.*;
import static atlantis.util.Helpers.*;

public class ProtossDynamicBuildingsCommander extends DynamicBuildingsCommander {
    @Override
    public void handle() {
        if (AGame.notNthGameFrame(7) || noSupply(25)) {
            return;
        }

        if (isItSafeToAddTechBuildings()) {
            cannons();
            arbiterTribunal();
            stargate();
            observatory();
            roboticsSupportBay();
            roboticsFacility();
            shieldBattery();
            forge();
        }

        gateways();
    }

    // =========================================================

    private static void roboticsSupportBay() {
        if (!A.supplyUsed(80)) {
            return;
        }

        if (Have.notEvenPlanned(Protoss_Robotics_Facility) || Have.a(Protoss_Robotics_Support_Bay)) {
            return;
        }

        if (Have.notEvenInPlans(Protoss_Robotics_Support_Bay)) {
            buildNow(Protoss_Robotics_Support_Bay);
        }
    }

    private static void observatory() {
        if (Have.a(Protoss_Observatory) || Have.notEvenPlanned(Protoss_Robotics_Facility)) {
            return;
        }

        if (Count.withPlanned(Protoss_Observatory) == 0) {
            buildNow(Protoss_Observatory);
        }
    }

    private static void roboticsFacility() {
        if (!Decisions.buildRoboticsFacility()) {
            return;
        }

        if (Have.notEvenPlanned(Protoss_Robotics_Facility)) {
            AddToQueue.withHighPriority(Protoss_Robotics_Facility);
            return;
        }

        if (Count.withPlanned(Protoss_Robotics_Facility) == 0) {
            buildNow(Protoss_Robotics_Facility);
        }
    }

    private static void shieldBattery() {
        // Dont use - it's really buggy and causes units to stand instead of fight, not worth it at the moment
//        buildToHaveOne(60, Protoss_Shield_Battery);
    }

    private static void gateways() {
        if (
            GamePhase.isEarlyGame()
                && EnemyStrategy.get().isRushOrCheese()
                && Count.ourOfTypeWithUnfinished(Protoss_Gateway) <= (A.hasMinerals(250) ? 2 : 1)
        ) {
            buildIfHaveMineralsAndGas(Protoss_Gateway);
            return;
        }

        buildIfAllBusyButCanAfford(Protoss_Gateway, A.supplyUsed() <= 90 ? 260 : 650, 0);
    }

    private static void forge() {
        int buildAtSupply = EnemyStrategy.get().isRushOrCheese() ? 46 : 36;
        buildToHaveOne(buildAtSupply, Protoss_Forge);
    }

    private static void stargate() {
        buildToHaveOne(80, Protoss_Stargate);
    }

    private static void arbiterTribunal() {
        buildToHaveOne(90, Protoss_Arbiter_Tribunal);

        if (hasFree(Protoss_Arbiter_Tribunal) && has(Protoss_Arbiter)) {
            ATechRequests.researchTech(TechType.Stasis_Field);
        }
    }

    private static boolean cannons() {
        if (A.notNthGameFrame(47)) {
            return false;
        }

        if (Count.inProductionOrInQueue(Protoss_Photon_Cannon) >= 2) {
            return false;
        }

        if (ProtossReinforceBases.handle()) {
            return true;
        }

        return false;
    }
}
