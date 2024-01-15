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
import atlantis.production.dynamic.protoss.buildings.*;
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

        if (AGame.notNthGameFrame(7) || (!A.hasMinerals(550) && noSupply(20))) {
            return;
        }

        if (isItSafeToAddTechBuildings()) {
            ProduceCannon.produce();
            ProduceArbiterTribunal.produce();
            ProduceStargate.produce();
            ProduceObservatory.produce();
            ProduceRoboticsSupportBay.produce();
            ProduceRoboticsFacility.produce();
            ProduceShieldBattery.produce();
            ProduceForge.produce();
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
