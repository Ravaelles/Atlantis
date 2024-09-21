package atlantis.production.dynamic.protoss;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.dynamic.protoss.buildings.*;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossDynamicBuildingsCommander extends DynamicCommanderHelpers {
    @Override
    public boolean applies() {
        return We.protoss()
            && AGame.everyNthGameFrame(17)
            && (A.supplyUsed() <= 30 || A.hasMinerals(410) || A.canAffordWithReserved(100, 0));
    }

    @Override
    protected void handle() {
        if (ProduceCyberneticsCore.produce()) return;

        ProduceFirstAssimilator.produce();
        ProduceCannon.produce();
        ProduceForge.produce();

        if (A.supplyUsed() <= 30 || A.hasMinerals(700)) {
            ProduceGateway.produce();
        }

        if (A.minerals() <= 500 && Queue.get().readyToProduceOrders().size() <= 1) return;

        if (isItSafeToAddTechBuildings()) {
            ProduceObservatory.produce();
            ProduceRoboticsFacility.produce();
            ProduceCitadelOfAdun.produce();
            ProduceStargate.produce();
            ProduceRoboticsSupportBay.produce();
            ProduceArbiterTribunal.produce();
            ProduceShieldBattery.produce();
        }

        ProduceGateway.produce();
    }

    // =========================================================

    protected static boolean isItSafeToAddTechBuildings() {
        if (EnemyStrategy.get().isRushOrCheese()) {
            if (ArmyStrength.ourArmyRelativeStrength() <= 80 && !A.hasMinerals(200)) return false;
        }

        AUnit enemyUnitInMainBase = EnemyInfo.enemyUnitInMainBase();
        if (enemyUnitInMainBase == null || enemyUnitInMainBase.effUndetected()) return false;

        return true;
    }
}
