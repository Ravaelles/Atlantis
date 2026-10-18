package atlantis.production.constructions;

public enum ConstructionOrderStatus {
    /**
     * Site picked in advance, no builder assigned, nothing issued (owner
     * 2026-10-14): opened one supply before the order unlocks so the
     * building is aimed before it is affordable. Waiting by design -
     * overdue, builders and status-changing ignore it; adoption (builder
     * + NOT_STARTED) happens in ProduceBuilding once the order is ready.
     */
    PREPARING,
    /**
     * Accepted, no builder yet. The demand half of the pair: a builder
     * assigned here is travelling, not waiting (see STARTING).
     */
    NOT_STARTED,
    /**
     * Builder assigned and travelling, not yet constructing (owner
     * 2026-10-19: the travel half had no status of its own, so "waiting
     * for supply" and "walking to the site" read the same). Entered only
     * from NOT_STARTED when a live builder is attached but has not
     * started (the status changer refines it, producers never write it
     * directly). Counts as started everywhere demand is counted
     * (hasStarted, notFinished, notStarted* families) - the only readers
     * that distinguish it are the travel drive, the advance seam and the
     * painter. Terminal states stay FINISHED-by-removal (named by twin);
     * lingering CANCELLED/DESTROYED tombstones were considered and
     * rejected - every reader would need an exclusion filter, which is
     * the leak class this statuses exist to kill.
     */
    STARTING,
    /**
     * Builder working (constructing, or building exists but unfinished -
     * Terran). Demoted back to NOT_STARTED when the builder is gone and
     * nothing stands (a dropped warp); finished (removed) when the
     * building stands on the tile (Protoss warp) or completes (Terran).
     */
    IN_PROGRESS,
    /**
     * Terminal, removed from the list on entry. The twin names the path
     * (warp, proximity finish, cancel) - no tombstone lingers.
     */
    FINISHED
}
