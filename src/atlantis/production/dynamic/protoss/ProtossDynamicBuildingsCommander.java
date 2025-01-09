package atlantis.production.dynamic.protoss;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.production.dynamic.protoss.buildings.*;
import atlantis.production.dynamic.supply.ProduceFallbackPylonWhenSupplyLow;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.We;

public class ProtossDynamicBuildingsCommander extends DynamicCommanderHelpers {
    @Override
    public boolean applies() {
        return We.protoss()
            && AGame.everyNthGameFrame(17)
            && applyForStandardCases();
    }

    private static boolean applyForStandardCases() {
        return A.hasMinerals(550)
            || A.canAffordWithReserved(92, 0)
            || (A.hasMinerals(260) && !ShouldExpand.shouldExpand());
    }

    private static boolean topPriority() {
        if ((new ProduceFallbackPylonWhenSupplyLow()).produceIfNeeded()) return true;

        if (
            ProduceCyberneticsCore.produce()
                || ProduceFirstAssimilator.produce()
                || ProduceObservatory.produce()
                || ProduceRoboticsFacility.produce()
        ) return true;

        return false;
    }

    @Override
    protected void handle() {
        if (topPriority()) return;

        if (!applyForStandardCases()) return;

        if (A.hasMinerals(470)) ProduceGateway.produce();

        boolean gatewaysEarly = A.supplyUsed() <= 30 || Count.gatewaysWithUnfinished() <= 1;
//        if (gatewaysEarly) {
//            ProduceGateway.produce();
//        }

        if (
//                || ProduceCannon.produce()
//                || ProduceCannonAtNatural.produce()
                ProduceShieldBatteryAtNatural.produce()
                || ProduceForge.produce()
                || (gatewaysEarly && ProduceGateway.produce())
                || ProducePylonNearEveryBase.produce()
                || ProduceTemplarArchives.produce()
                || ProduceCitadelOfAdun.produce()
        ) return;

        if (A.minerals() <= 500 && Queue.get().readyToProduceOrders().size() <= 1) return;

        if (isItSafeToAddTechBuildings()) {
            if (
//                ProduceCitadelOfAdun.produce()
                 ProduceRoboticsSupportBay.produce()
//                    || ProduceTemplarArchives.produce()
                    || ProduceStargate.produce()
                    || ProduceArbiterTribunal.produce()
                    || ProduceShieldBattery.produce()
            ) return;
        }

        ProduceGateway.produce();
    }

    // =========================================================

    protected static boolean isItSafeToAddTechBuildings() {
        if (A.s >= 500) return true;

        if (EnemyStrategy.get().isRushOrCheese() && A.s <= 400) {
            if (ArmyStrength.ourArmyRelativeStrength() <= 80 && !A.hasMinerals(200)) return false;
        }

        AUnit enemyUnitInMainBase = EnemyInfo.enemyUnitInMainBase();
        if (enemyUnitInMainBase == null || enemyUnitInMainBase.effUndetected()) return false;

        return true;
    }
}
