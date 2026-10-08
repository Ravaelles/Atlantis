package atlantis.production.v2.execution;

import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.Producible;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;
import java.util.List;

/**
 * The dry-run {@link OrderDirector} (M4): it records what the v2 engine would
 * have ordered instead of touching the game.
 *
 * <p>
 * This is how the redesign runs in parallel with the legacy queue for one
 * observed iteration: the legacy pipeline keeps playing, the v2 planner
 * computes its own plan from the same state, and this director captures the
 * difference. A cutover that starts with "the new engine also wanted a Zealot
 * here" is a cutover that can be trusted; one that starts with "trust me, the
 * unit tests pass" is not.
 * </p>
 *
 * <p>
 * It reports {@code false} for everything, deliberately: the dispatcher's
 * "issued" must mean "a command left", and in dry-run none did. The
 * {@link #orders()} list is what proves what the plan wanted.
 * </p>
 */
public final class DryRunOrderDirector implements OrderDirector {

    private final List<String> orders = new ArrayList<>();

    @Override
    public boolean trainFacility(String typeId, int producerId, Producible item) {
        orders.add("train " + item.id() + " at " + typeId + "#" + producerId);
        return false;
    }

    @Override
    public boolean buildAt(Producible building, PlacementReservation placement) {
        orders.add("build " + building.id() + " at tile(" + placement.tileX() + "," + placement.tileY() + ")");
        return false;
    }

    @Override
    public boolean researchOrUpgrade(String typeId, int producerId, Producible item) {
        orders.add("research " + item.id() + " at " + typeId + "#" + producerId);
        return false;
    }

    /** What the engine would have ordered, in dispatch order. */
    public List<String> orders() {
        return orders;
    }

    public void clear() {
        orders.clear();
    }

    /** Records the comparison line for the work log; no game state is touched. */
    public void logSummary(int frame, int planSize) {
        if (orders.isEmpty())
            return;

        ErrorLog.printMaxOncePerMinute("PRODUCTION_V2 dry-run @" + frame
                + ": plan=" + planSize + ", would issue " + orders.size()
                + " " + orders);
    }
}