package atlantis.production.dynamic.protoss;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.tech.ATechRequests;
import atlantis.map.ABaseLocation;
import atlantis.map.position.HasPosition;
import atlantis.production.dynamic.ADynamicBuildingsManager;
import atlantis.production.requests.AAntiLandBuildingRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import bwapi.TechType;

import static atlantis.units.AUnitType.*;

public class ProtossDynamicBuildingsManager extends ADynamicBuildingsManager {

    public static void update() {
        if (AGame.notNthGameFrame(7) || noSupply(25)) {
            return;
        }

        cannons();
        arbiterTribunal();
        stargate();
        observatory();
        roboticsSupportBay();
        roboticsFacility();
        shieldBattery();
        forge();
        gateways();
    }

    // =========================================================

    private static void roboticsSupportBay() {
        if (!A.supplyUsed(60)) {
            return;
        }

        if (Have.no(Protoss_Robotics_Facility) || Have.a(Protoss_Robotics_Support_Bay)) {
            return;
        }

        if (Have.notEvenInPlans(Protoss_Robotics_Support_Bay)) {
            buildNow(Protoss_Robotics_Support_Bay);
        }
    }

    private static void observatory() {
        if (Have.a(Protoss_Observatory) || Have.no(Protoss_Robotics_Facility)) {
            return;
        }

        if (Count.WithPlanned(Protoss_Observatory) == 0) {
            buildNow(Protoss_Observatory);
        }
    }

    private static void roboticsFacility() {
        if (!Decisions.buildRoboticsFacility()) {
            return;
        }

        if (Count.WithPlanned(Protoss_Robotics_Facility) == 0) {
            buildNow(Protoss_Robotics_Facility);
        }
    }

    private static void shieldBattery() {
//        buildToHaveOne(40, AUnitType.Protoss_Shield_Battery);
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

        buildIfAllBusyButCanAfford(Protoss_Gateway, 120, 0);
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

        if (
            hasFree(Protoss_Arbiter_Tribunal)
                && has(Protoss_Arbiter)
        ) {
            ATechRequests.researchTech(TechType.Stasis_Field);
        }
    }

    private static void cannons() {
        if (A.notNthGameFrame(47) || A.seconds() < 550) {
            return;
        }

        for (AUnit base : Select.ourBases().list()) {
            int existingCannonsNearby = Select.ourWithUnfinished()
                .inRadius(10, base)
                .count();

            if (existingCannonsNearby < 1) {
                HasPosition nearTo = ABaseLocation.mineralsCenter(base);
                if (Count.existingOrPlannedBuildingsNear(Protoss_Photon_Cannon, 10, nearTo) == 0) {
                    AAntiLandBuildingRequests.requestCombatBuildingAntiLand(nearTo);
                }
            }
        }
    }
}
