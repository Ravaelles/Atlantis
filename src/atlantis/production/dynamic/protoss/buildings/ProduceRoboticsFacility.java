package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.decisions.Decisions;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.OurArmy;
import atlantis.production.dynamic.protoss.units.ProduceObserver;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Robotics_Facility;

public class ProduceRoboticsFacility {
    public static boolean produce() {
//        if (true) return false;

        if (!shouldBuild()) return false;

//            System.err.println("@ " + A.now() + " - REQUESTED Protoss_Robotics_Facility");
//        return DynamicCommanderHelpers.buildNow(Protoss_Robotics_Facility, true);
        return AddToQueue.withHighPriority(Protoss_Robotics_Facility) != null;
    }

    public static boolean shouldBuild() {
        if (!Have.forge()) return false;
        if (Count.workers() <= 17) return false;
        if (A.seconds() <= 390 && OurArmy.strength() <= 90) return false;
        if (Count.withPlanned(Protoss_Robotics_Facility) > 0) return false;
        if (A.supplyUsed() <= 38 && Decisions.enemyStrategyIsRushOrCheese()) return false;
        if (A.supplyUsed() <= 70 && Have.cannon() && !EnemyInfo.hasHiddenUnits()) return false;
        
        if (!ProduceObserver.earlyGamePressureDontInvest()) return false;

        int n = Count.roboticsFacilities();
        if (
            (n >= 1 && A.supplyUsed() < 140)
                || (n >= 2 && A.supplyUsed() < 195)
        ) {
            return false;
        }

        if (EnemyInfo.hasHiddenUnits()) {
//            System.err.println("roboticsFacility because hasHiddenUnits");
            return true;
        }

//        System.err.println("----- buildRoboticsFacility OK" );
//        System.err.println("EnemyStrategy.get().isRushOrCheese() = " + EnemyStrategy.get().isRushOrCheese());
//        System.err.println("EnemyStrategy.get().isRush() = " + EnemyStrategy.get().isRush());

        return true;
    }
}
