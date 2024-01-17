package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.decisions.Decisions;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.OurArmyStrength;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Forge;
import static atlantis.units.AUnitType.Protoss_Robotics_Facility;

public class ProduceRoboticsFacility {
    public static void produce() {
        if (!shouldBuild()) return;

        if (Count.withPlanned(Protoss_Robotics_Facility) == 0) {
//            System.err.println("@ " + A.now() + " - REQUESTED Protoss_Robotics_Facility");
            DynamicCommanderHelpers.buildNow(Protoss_Robotics_Facility, true);
        }
    }

    public static boolean shouldBuild() {
        if (Have.roboticsFacility() || !Have.forge()) return false;
        if (A.seconds() <= 450 && OurArmyStrength.relative() <= 90) return false;
        if (Count.workers() <= 19) return false;

        if (EnemyInfo.hasHiddenUnits()) {
//            System.err.println("roboticsFacility because hasHiddenUnits");
            return true;
        }
        if (A.supplyUsed() <= 44 && Decisions.enemyStrategyIsRushOrCheese()) return false;
        if (A.supplyUsed() <= 46 && Have.cannon()) return false;

//        System.err.println("----- buildRoboticsFacility OK" );
//        System.err.println("EnemyStrategy.get().isRushOrCheese() = " + EnemyStrategy.get().isRushOrCheese());
//        System.err.println("EnemyStrategy.get().isRush() = " + EnemyStrategy.get().isRush());

        return true;
    }
}
