package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Citadel_of_Adun;
import static atlantis.units.AUnitType.Protoss_Templar_Archives;

public class ProduceTemplarArchives {
    public static boolean produce() {
        if (Count.ourCombatUnits() <= 19 && !A.canAfford(150, 150)) return false;
        if (Have.a(type())) return false;

        if (Have.notEvenPlanned(Protoss_Citadel_of_Adun)) {
            AddToQueue.withHighPriority(Protoss_Citadel_of_Adun);
            return true;
        }

        if (Have.a(Protoss_Citadel_of_Adun)) return false;

        if (Have.notEvenPlanned(type())) {
            AddToQueue.withHighPriority(type());
            return true;
        }

        return false;
    }

    private static AUnitType type() {
        return Protoss_Templar_Archives;
    }
}
