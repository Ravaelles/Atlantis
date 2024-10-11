package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.generic.OurArmy;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.*;

public class ProduceTemplarArchives {
    public static boolean produce() {
//        if (true) return false;
        if (Have.a(type())) return false;

        if (Enemy.zerg() && A.supplyUsed() < 180) return false;
        if (OurArmy.strength() <= 120) return false;
        if (Count.bases() <= 1) return false;
        if (Count.ourCombatUnits() <= 19 && !A.canAfford(150, 150)) return false;

        if (A.supplyUsed() < 165) return false;
        if (A.supplyUsed() >= 175) return requestProduce();
        if (A.hasGas(330) && Have.a(Protoss_Citadel_of_Adun) && Have.a(Protoss_Observatory)) return requestProduce();

        if (A.supplyUsed() >= 165 && (A.hasGas(180) || A.hasMinerals(400)) && A.fr % 41 == 0) return requestProduce();

        return requestProduce();
    }

    private static boolean requestProduce() {
        if (Have.notEvenPlanned(Protoss_Citadel_of_Adun)) {
            return AddToQueue.withHighPriority(Protoss_Citadel_of_Adun) != null;
        }
        if (!Have.a(Protoss_Citadel_of_Adun)) return false;

        if (Have.notEvenPlanned(type())) {
            A.errPrintln("ProduceTemplarArchives: Requested Templar Archives at " + A.s);
            return AddToQueue.withHighPriority(type()) != null;
        }

        return false;
    }

    private static AUnitType type() {
        return Protoss_Templar_Archives;
    }
}
