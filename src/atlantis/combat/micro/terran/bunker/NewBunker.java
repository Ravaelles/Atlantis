package atlantis.combat.micro.terran.bunker;

import atlantis.combat.micro.terran.bunker.position.NewBunkerApproximatePosition;
import atlantis.map.position.HasPosition;
import atlantis.production.dynamic.terran.buildings.ShouldProduceNewBunker;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Terran_Bunker;

public class NewBunker {
    public NewBunker() {
    }

    private static AUnitType what() {
        return Terran_Bunker;
    }

    public boolean requestNewAndAutomaticallyDecidePosition() {
        if (!(new ShouldProduceNewBunker()).shouldBuild()) return false;

        HasPosition approximatePositionAsUnspecified = (new NewBunkerApproximatePosition()).approximatePosition();
        if (Count.existingOrPlannedBuildingsNear(what(), 7, approximatePositionAsUnspecified) > 0) return false;

        return requestNewBunker(approximatePositionAsUnspecified);
    }

    public boolean requestNewBunker(HasPosition positionToSecure) {
        if (positionToSecure == null) return false;

//        APosition precisePosition = (new NewBunkerPositionFinder(positionToSecure)).find();
//        System.err.println("precisePosition = " + precisePosition);

        HasPosition at = positionToSecure;

        ProductionOrder order = AddToQueue.withTopPriority(what(), at);
//        if (order != null) order.markAsUsingExactPosition();
        if (order != null) {
            order.setMinSupply(1);
//            System.err.println("Requested NEW BUNKER at: " + at);
//            System.err.println(order);
//            System.err.println(order.construction());
//            System.err.println(order.status());
            return true;
        }

        return false;
    }
}
