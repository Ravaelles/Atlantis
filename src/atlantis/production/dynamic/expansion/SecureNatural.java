package atlantis.production.dynamic.expansion;

import atlantis.game.A;
import atlantis.map.base.Bases;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.PositionModifier;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;
import atlantis.util.We;

import static atlantis.units.AUnitType.Terran_Bunker;

public class SecureNatural {
    public static final int DIST_FROM_BASE = 8;

    private static APosition natural;

    public static boolean secure() {
        if (true) return true;

        if (!We.terran()) return true;
        if ((natural = Bases.natural()) == null) return true;
        if (isAlreadySecure()) return true;

        secureWithBunker();
        return false;
    }

    private static boolean secureWithBunker() {
        if (Count.withPlanned(Terran_Bunker) >= 3) return true;

        if (Count.existingOrPlannedBuildingsNear(Terran_Bunker, DIST_FROM_BASE, natural) == 0) {
            ProductionOrder order = AddToQueue.withHighPriority(Terran_Bunker, null);
            System.out.println("order NATURAL BUNKER = " + order );
            if (order != null) {
                order.setModifier(PositionModifier.NATURAL);
                return false;
            }
        }

        return Count.inProductionOrInQueue(Terran_Bunker) >= 2;
    }

    private static boolean isAlreadySecure() {
        return A.hasMinerals(800) || Count.existingOrUnfinishedBuildingsNear(Terran_Bunker, 8, natural) > 0;
    }
}
