package atlantis.production.requests.produce;

import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

/**
 * One named refusal gate of the single building door
 * ({@link ProduceBuilding#requestBuilding}). A gate answers the refusal
 * reason or null when it passes; {@link BuildGates#all()} fixes the
 * evaluation order. The order is semantic - a gate reordered wrongly
 * re-trips an earlier refusal - so it is written as a contract here,
 * not scattered across an if-chain.
 */
public interface BuildGate {

    /**
     * The twin-visible refusal reason, e.g. {@code "pylon-cap"}.
     */
    String name();

    /**
     * The refusal reason when this gate blocks the request, null when it
     * passes. Pure read of the request and the game state, no issuance.
     */
    String check(AUnitType type, ProductionOrder order);
}
