package atlantis.combat.missions.drops;

import atlantis.information.enemy.EnemyOnCloseIsland;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.We;

public class ProtossShouldDropToIsland {
    public static boolean check() {
        if (!We.protoss()) return false;
        if (EnemyOnCloseIsland.get() == null) return false;

        requestTransportIfNeeded();

        return true;
    }

    private static void requestTransportIfNeeded() {
        if (Count.shuttles() > 0) return;

        AUnitType type = transportUnit();
        if (Have.notEvenPlanned(type)) {
            AddToQueue.withHighPriority(type);
        }
    }

    private static AUnitType transportUnit() {
        if (We.protoss()) return AUnitType.Protoss_Shuttle;
        else if (We.terran()) return AUnitType.Terran_Dropship;
        else return AUnitType.Zerg_Overlord;
    }
}
