package atlantis.units.actions;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

/**
 * Move-order issuance policy (__SOLID.md item 2, P1 slice): the "should I
 * even issue this" decision, out of the {@code AUnitOrders.move} default
 * method. Pure predicate over the unit's current target and progress
 * clock - the funnel keeps validation, cadence stamping and the sink
 * call. Unit-testable without an engine; behavior-neutral by
 * construction (verbatim move of the heuristic, dead experiment lines
 * dropped).
 */
public final class OrderIssuance {

    private OrderIssuance() {
    }

    /**
     * A new destination, or the unit stopped making progress toward the
     * old one: re-issue. Otherwise the standing order still pursues the
     * same tile and re-issuing only spams the queue.
     */
    public static boolean isNewMoveDestination(AUnit unit, HasPosition target) {
        return !target.equals(unit.target()) || unit.lastPositionChangedAgo() >= 10;
    }

    /**
     * Re-issue cadence while moving: running units get a longer leash.
     */
    public static int commandMinDelayWhenMoving(AUnit unit) {
        return unit.isRunning() ? 3 : 2;
    }
}
