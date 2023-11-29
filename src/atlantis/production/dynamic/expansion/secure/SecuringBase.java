package atlantis.production.dynamic.expansion.secure;

import atlantis.combat.micro.terran.bunker.position.NewBunkerPositionFinder;
import atlantis.game.A;
import atlantis.map.base.IsNatural;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

import static atlantis.units.AUnitType.Terran_Bunker;

public class SecuringBase {
    public static final int DIST_FROM_BASE = 14;

    private final SecuringWithBunker securingWithBunker;
    private final SecuringWithTurret securingWithTurret;

    private APosition baseToSecure;

    public SecuringBase(APosition baseToSecure) {
        this.baseToSecure = baseToSecure;
        this.securingWithBunker = new SecuringWithBunker(this);
        this.securingWithTurret = new SecuringWithTurret(this);
    }

    public boolean secureWithCombatBuildings() {
//        if (true) return true;

        if (!We.terran()) return true;
        if (isSecure()) return consideredBaseAsSecure("isSecure");
        if (errorDetectedSameRegionAsMain()) return consideredBaseAsSecure("Same region as main base detected");

        return secureWithBunker();
    }

    private boolean errorDetectedSameRegionAsMain() {
        // @Fix
        if (baseToSecure.regionsMatch(Select.main())) {
            ErrorLog.printMaxOncePerMinute("Trying SecuringBase main - ignore");
            return true;
        }
        return false;
    }

    private boolean consideredBaseAsSecure(String reason) {
//        System.out.println("consideredBaseAsSecure reason = " + reason);
        return true;
    }

    private boolean secureWithBunker() {
        APosition bunkerPosition = (new NewBunkerPositionFinder(baseToSecure)).find();

//        System.err.println("@ " + A.now() + " - secureWithBunker base? " + baseToSecure + " / " + bunkerPosition);

        if (bunkerPosition == null) return false;
        if (errorDetectedSameRegionAsMain()) return false;

        if (isSecure()) {
            if (baseToSecure.distTo(Select.mainOrAnyBuilding()) > 15) {
                System.err.println("------ base is secure " + baseToSecure);
            }
            return true;
        }

//        System.err.println(A.now() + " bunkerPosition = " + bunkerPosition
//            + " / " + Count.existingOrPlannedBuildingsNear(Terran_Bunker, DIST_FROM_BASE, bunkerPosition)
//            + " // " + Select.main().position());

//        if (!isPlaceSecured(bunkerPosition)) {
        ProductionOrder order = AddToQueue.withTopPriority(Terran_Bunker, bunkerPosition);

        if (order == null) {
            ErrorLog.printMaxOncePerMinute("Empty ProductionOrder for secureWithBunker");
            return false;
        }

        order.markAsUsingExactPosition();
        order.forceSetPosition(bunkerPosition);

//            System.err.println("@ " + A.now() + " - ");
//            System.err.println("Count.existingOrPlannedBuildingsNear = "
//                + Count.existingOrPlannedBuildingsNear(Terran_Bunker, DIST_FROM_BASE, baseToSecure));
        System.err.println("@@@@ SECURE base " + baseToSecure + " with BUNKER = " + order);
//            if (order != null) {
//                order.setModifier(PositionModifier.NATURAL);
//                return false;
//            }
        return false;
//        }

//        return true;
//        return Count.inProductionOrInQueue(Terran_Bunker) >= 2;
    }

    private boolean isPlaceSecured(APosition bunkerPosition) {
        return Count.existingOrPlannedBuildingsNear(Terran_Bunker, DIST_FROM_BASE, bunkerPosition) < numOfBunkers();
    }

    private int numOfBunkers() {
        if (IsNatural.isPositionNatural(baseToSecure)) return 2;

        return 1;
    }

    public boolean isSecure() {
        if (baseToSecure == null) return consideredBaseAsSecure("Null baseToSecure");
        if (A.hasMinerals(1000)) return consideredBaseAsSecure("Lots of minerals");
        if (We.zerg() && !Have.spawningPool()) return consideredBaseAsSecure("Zerg fast expanded");
//        if (CountInQueue.count(Terran_Bunker, 6) >= 2) return consideredBaseAsSecure("Bunkers already queued");
        if (Count.withPlanned(Terran_Bunker) >= 2 + Count.basesWithPlanned())
            return consideredBaseAsSecure("Many bunkers already");

//        if (EnemyInfo.hasHiddenUnits() && A.seconds() >= 350 && !securingWithTurret.hasTurretSecuring()) return false;

        return securingWithBunker.hasBunkerSecuring();
    }

    public HasPosition baseToSecure() {
        return baseToSecure;
    }
}
