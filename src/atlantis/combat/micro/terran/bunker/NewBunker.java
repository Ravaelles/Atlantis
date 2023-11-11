package atlantis.combat.micro.terran.bunker;

import atlantis.combat.micro.terran.bunker.position.NewBunkerEstimatePosition;
import atlantis.combat.micro.terran.bunker.position.NewBunkerPositionFinder;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;

import static atlantis.units.AUnitType.Terran_Bunker;

public class NewBunker {
    private final ShouldBuildNewBunker shouldBuildNewBunker = new ShouldBuildNewBunker();
    private final NewBunkerEstimatePosition newBunkerApproximator = new NewBunkerEstimatePosition();

    public NewBunker() {
    }

    public boolean requestNewAndAutomaticallyDecidePosition() {
        if (!shouldBuildNewBunker.shouldBuildNew()) return false;

        HasPosition approximatePositionAsUnspecified = newBunkerApproximator.approximatePosition();
        System.err.println("approximatePositionAsUnspecified = " + approximatePositionAsUnspecified);

        return requestNewBase(approximatePositionAsUnspecified);
    }

    protected boolean requestNewBase(HasPosition positionToSecure) {
        if (positionToSecure == null) return false;

        APosition precisePosition = (new NewBunkerPositionFinder(positionToSecure)).find();
        System.err.println("precisePosition = " + precisePosition);

        ProductionOrder order = AddToQueue.withTopPriority(Terran_Bunker, precisePosition);
        if (order != null) order.markAsUsingExactPosition();

        return order != null;
    }
}
