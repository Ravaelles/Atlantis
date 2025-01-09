package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.map.base.BaseLocations;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class ProduceShieldBatteryAtNatural {
    public static boolean produce() {
        if (true) return false;

        if (!A.everyNthGameFrame(69)) return false;
        if (Count.cannons() < 2) return false;
        int shieldBatteries = Count.withPlanned(type());
        if (!A.hasMinerals(170 * shieldBatteries)) return false;
        if (shieldBatteries >= Math.ceil(A.supplyTotal() / 70.0)) return false;
        if (Army.strength() >= 180 && !A.hasMinerals(300)) return false;

        return request();
    }

    private static boolean request() {
        HasPosition at = Chokes.natural();
        if (at == null) return false;

        at = at.translatePercentTowards(BaseLocations.natural(), 25);

        return AddToQueue.withStandardPriority(type(), at) != null;
    }

    private static AUnitType type() {
        return AUnitType.Protoss_Shield_Battery;
    }
}
