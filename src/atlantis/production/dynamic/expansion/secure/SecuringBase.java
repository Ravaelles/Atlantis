package atlantis.production.dynamic.expansion.secure;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.We;

import static atlantis.units.AUnitType.Terran_Bunker;

public class SecuringBase {
    public static final int DIST_FROM_BASE = 8;
    private final SecuringWithBunker securingWithBunker = new SecuringWithBunker(this);
    private final SecuringWithTurret securingWithTurret = new SecuringWithTurret(this);

    private APosition baseToSecure;

    public SecuringBase(APosition baseToSecure) {
        this.baseToSecure = baseToSecure;
    }

    public boolean secure() {
//        if (true) return true;

        if (!We.terran()) return true;
        if (baseToSecure == null) return true;
        if (isSecure()) return true;

        secureWithBunker();
        return false;
    }

    private boolean secureWithBunker() {
        if (CountInQueue.count(Terran_Bunker) >= 1) return true;
        if (Count.withPlanned(Terran_Bunker) >= 3) return true;

        APosition bunkerPosition = SecuringWithBunkerPosition.bunkerPosition();

//        System.err.println(A.now() + " bunkerPosition = " + bunkerPosition + " / " + Count.existingOrPlannedBuildingsNear(Terran_Bunker, DIST_FROM_BASE, bunkerPosition));
//        System.err.println(Select.main().position());

        if (bunkerPosition == null) return true; // Broken, temp fix

        if (Count.existingOrPlannedBuildingsNear(Terran_Bunker, DIST_FROM_BASE, bunkerPosition) == 0) {
            ProductionOrder order = AddToQueue.withTopPriority(Terran_Bunker, bunkerPosition);

//            System.err.println("@ " + A.now() + " - ");
//            System.err.println("Count.existingOrPlannedBuildingsNear = "
//                + Count.existingOrPlannedBuildingsNear(Terran_Bunker, DIST_FROM_BASE, baseToSecure));
            System.out.println("SECURE base " + baseToSecure + " with BUNKER = " + order);
//            if (order != null) {
//                order.setModifier(PositionModifier.NATURAL);
//                return false;
//            }
            return true;
        }

        return Count.inProductionOrInQueue(Terran_Bunker) >= 2;
    }

    public boolean isSecure() {
//        if (A.hasMinerals(1000)) return true; // Having lots of resources means we can afford potential losses

//        if (We.terran() && !Have.barracks()) return true;
        if (We.zerg() && !Have.spawningPool()) return true;

        if (!securingWithBunker.hasBunkerSecuring()) return false;
        if (EnemyInfo.hasHiddenUnits() && A.seconds() >= 350 && securingWithTurret.hasTurretSecuring()) return false;

        return true;
    }

    public HasPosition getBaseToSecure() {
        return baseToSecure;
    }

//    public boolean isSecure() {
//        return A.hasMinerals(800) || Count.existingOrUnfinishedBuildingsNear(Terran_Bunker, 8, baseToSecure) > 0;
//    }
}
