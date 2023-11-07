package atlantis.production.dynamic.expansion.secure;

import atlantis.information.enemy.EnemyInfo;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.PositionModifier;
import atlantis.production.constructing.position.base.NextBasePosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;
import atlantis.util.We;

import static atlantis.units.AUnitType.Terran_Bunker;

public class SecuringBase {
    public static final int DIST_FROM_BASE = 8;
    private final SecuringWithBunker securingWithBunker = new SecuringWithBunker(this);

    private APosition baseToSecure;

    public SecuringBase(APosition baseToSecure) {
        this.baseToSecure = baseToSecure;
    }

    public boolean secure() {
//        if (true) return true;

        if (!We.terran()) return true;
        if (baseToSecure == null) return true;
//        if ((baseToSecure = Bases.natural()) == null) return true;
        if (isSecure()) return true;

        secureWithBunker();
        return false;
    }

    private boolean secureWithBunker() {
        if (Count.withPlanned(Terran_Bunker) >= 3) return true;

        if (Count.existingOrPlannedBuildingsNear(Terran_Bunker, DIST_FROM_BASE, baseToSecure) == 0) {
            ProductionOrder order = AddToQueue.withHighPriority(Terran_Bunker, NextBasePosition.nextBasePosition());
            System.out.println("order NATURAL BUNKER = " + order);
            if (order != null) {
                order.setModifier(PositionModifier.NATURAL);
                return false;
            }
        }

        return Count.inProductionOrInQueue(Terran_Bunker) >= 2;
    }

    public boolean isSecure() {
//        if (A.hasMinerals(1000)) return true; // Having lots of resources means we can afford potential losses

        if (!securingWithBunker.hasBunkerSecuring()) return false;
        if (!EnemyInfo.hasHiddenUnits() || !securingWithBunker.hasBunkerSecuring()) return false;

        return true;
    }

    public HasPosition getBaseToSecure() {
        return baseToSecure;
    }

//    public boolean isSecure() {
//        return A.hasMinerals(800) || Count.existingOrUnfinishedBuildingsNear(Terran_Bunker, 8, baseToSecure) > 0;
//    }
}
