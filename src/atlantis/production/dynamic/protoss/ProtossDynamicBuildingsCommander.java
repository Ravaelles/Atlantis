package atlantis.production.dynamic.protoss;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.tech.ATechRequests;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.We;
import bwapi.TechType;

import static atlantis.units.AUnitType.*;
import static atlantis.util.Helpers.*;

public class ProtossDynamicBuildingsCommander extends DynamicCommanderHelpers {
    @Override
    public boolean applies() {
        return We.protoss();
    }

    @Override
    protected void handle() {
        super.invokeCommander();

        if (AGame.notNthGameFrame(7) || (!A.hasMinerals(550) && noSupply(25))) {
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
        if (!A.supplyUsed(80)) return;
        if (Have.notEvenPlanned(Protoss_Robotics_Facility) || Have.a(Protoss_Robotics_Support_Bay)) return;

        if (Have.dontHaveEvenInPlans(Protoss_Robotics_Support_Bay)) {
            DynamicCommanderHelpers.buildNow(Protoss_Robotics_Support_Bay);
        }
    }

    private static void observatory() {
        if (Have.a(Protoss_Observatory) || Have.notEvenPlanned(Protoss_Robotics_Facility)) {
            return;
        }

        if (Count.withPlanned(Protoss_Observatory) == 0) {
            DynamicCommanderHelpers.buildNow(Protoss_Observatory);
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
            DynamicCommanderHelpers.buildNow(Protoss_Robotics_Facility);
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
                && Count.ourWithUnfinished(Protoss_Gateway) <= (A.hasMinerals(250) ? 2 : 1)
        ) {
            DynamicCommanderHelpers.buildIfHaveMineralsAndGas(Protoss_Gateway);
            return;
        }

        DynamicCommanderHelpers.buildIfAllBusyButCanAfford(Protoss_Gateway, A.supplyUsed() <= 90 ? 260 : 650, 0);
    }

    private static void forge() {
        int buildAtSupply = EnemyStrategy.get().isRushOrCheese() ? 46 : 36;
        DynamicCommanderHelpers.buildToHaveOne(buildAtSupply, Protoss_Forge);
    }

    private static void stargate() {
        DynamicCommanderHelpers.buildToHaveOne(80, Protoss_Stargate);
    }

    private static void arbiterTribunal() {
        DynamicCommanderHelpers.buildToHaveOne(90, Protoss_Arbiter_Tribunal);

        if (hasFree(Protoss_Arbiter_Tribunal) && has(Protoss_Arbiter)) {
            ATechRequests.researchTech(TechType.Stasis_Field);
        }
    }

    private static boolean cannons() {
        if (A.everyFrameExceptNthFrame(47)) return false;

        if (Count.inProductionOrInQueue(Protoss_Photon_Cannon) >= 2) return false;

        if (ProtossReinforceBases.invoke()) return true;

        return false;
    }

    // =========================================================

    protected static boolean isItSafeToAddTechBuildings() {
        if (EnemyStrategy.get().isRushOrCheese()) {
            if (ArmyStrength.ourArmyRelativeStrength() <= 80 && !A.hasMineralsAndGas(250, 100)) return false;
        }

        AUnit enemyUnitInMainBase = EnemyInfo.enemyUnitInMainBase();
        if (enemyUnitInMainBase == null || enemyUnitInMainBase.effUndetected()) return false;

        return true;
    }
}
