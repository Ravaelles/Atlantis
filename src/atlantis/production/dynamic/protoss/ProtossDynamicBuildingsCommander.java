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
            && (
            A.hasMinerals(550)
                || A.canAffordWithReserved(92, 0)
                || (A.hasMinerals(260) && !ShouldExpand.shouldExpand())
        );
    }

    @Override
    protected void handle() {
        if ((new ProduceFallbackPylonWhenSupplyLow()).produceIfNeeded()) return;

        if (A.hasMinerals(470)) ProduceGateway.produce();

        if (ProduceCyberneticsCore.produce()) return;

        boolean gatewaysEarly = A.supplyUsed() <= 30 || Count.gatewaysWithUnfinished() <= 1;
//        if (gatewaysEarly) {
//            ProduceGateway.produce();
//        }

        if (
            ProduceFirstAssimilator.produce()
//                || ProduceCannon.produce()
//                || ProduceCannonAtNatural.produce()
                || ProduceShieldBatteryAtNatural.produce()
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
                ProduceObservatory.produce()
//                    || ProduceTemplarArchives.produce()
                    || ProduceRoboticsSupportBay.produce()
                    || ProduceRoboticsFacility.produce()
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
