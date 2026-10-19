package atlantis.production.constructions;

import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

/**
 * Every unfinished building of a type, counted once. Aggregation lives
 * here - not in the queue counter - because it reads three domains
 * (queue rows, warping units, constructions) and the queue counter's
 * one reason is counting queue rows (SRP). Placement next to the
 * constructions is deliberate: production-internal edges already run
 * this way, and core (select) must never call production.
 *
 * <p>Undispatched queue rows (no linked construction) + warping units +
 * all unfinished constructions (PREPARING included - the old
 * notStarted-only counts were blind to picked-early sites). Terran
 * over-counts by the construction phase (unfinished unit + live
 * construction overlap); Protoss is exact, and the direction is safe
 * (a delayed structure, never a duplicate).</p>
 */
public class OutstandingBuildings {

    public static int ofType(AUnitType type) {
        return CountInQueue.countNotFinishedWithoutConstruction(type)
            + Select.ourUnfinished().ofType(type).count()
            + ConstructionRequests.notFinishedOfType(type).size();
    }
}
