package atlantis.production.dynamic.protoss;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.dynamic.protoss.buildings.*;
import atlantis.units.AUnit;
import atlantis.util.We;

import static atlantis.util.Helpers.*;

public class ProtossDynamicBuildingsCommander extends DynamicCommanderHelpers {
    @Override
    public boolean applies() {
        return We.protoss()
            && AGame.everyNthGameFrame(7)
//            && A.hasMinerals(550)
            && (A.hasMinerals(450) || A.canAffordWithReserved(170, 0))
            && A.supplyUsed(20);
    }

    @Override
    protected void handle() {
        ProduceCannon.produce();
        ProduceForge.produce();

        if (isItSafeToAddTechBuildings()) {
            ProduceCyberneticsCore.produce();
            ProduceArbiterTribunal.produce();
            ProduceStargate.produce();
            ProduceObservatory.produce();
            ProduceRoboticsSupportBay.produce();
            ProduceRoboticsFacility.produce();
            ProduceShieldBattery.produce();
        }

        ProduceGateway.produce();
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
