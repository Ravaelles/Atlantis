package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.*;

public class ProducePylonNearEveryBase {
    public static boolean produce() {
        if (A.everyNthGameFrame(53)) return false;
        if (A.s <= 60 * 7) return false;

        int index = 0;
        for (AUnit base : Select.ourBasesWithUnfinished().list()) {
            if (base.friendsNear().ofType(type()).empty()) {
                return AddToQueue.withStandardPriority(type(), nearTo(base)) != null;
//                    && A.println(A.s + "s: ------- ProducePylonNearEveryBase (index:" + index + ")");
            }
            index++;
        }

        return true;
    }

    private static HasPosition nearTo(AUnit base) {
        AChoke choke = Chokes.nearestChoke(base);
        if (choke != null) {
            return base.translateTilesTowards(choke, 3);
        }

        APosition resources = Select.mineralsAndGeysers().inRadius(10, base).center();
        if (resources != null) return base.translateTilesTowards(-4, resources);

        return base;
    }

    private static AUnitType type() {
        return Protoss_Pylon;
    }
}
