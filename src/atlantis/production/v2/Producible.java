package atlantis.production.v2;

import java.util.List;

/**
 * A recipe for one producible thing: its cost, build time, prerequisites and
 * the facility type that produces it.
 *
 * <p>
 * OCP seam (01_PRODUCTION.md §2 smell 2): the scheduler never branches on
 * {@code isBuilding()} or race. A unit, a tech research and an upgrade are
 * three implementations of this interface; adding Zerg larvae mechanics later
 * means another implementation, not 50 {@code if (We.zerg())} branches.
 * </p>
 *
 * <p>
 * Pure domain: no BWAPI types here - a UnitProducible adapter carries the
 * {@code AUnitType} and answers the questions from it. That keeps the timeline
 * and scheduler 100% deterministic in JUnit.
 * </p>
 */
public interface Producible {

    /**
     * Stable identifier for logs and dedup, e.g. "Dragoon", "Protoss_Assimilator".
     */
    String id();

    /** What producing one of these costs. */
    ResourceCost cost();

    /** Frames from issuing the order until the thing exists. */
    int buildDurationFrames();

    /**
     * Things that must exist (or be under construction in this plan) before
     * this can be produced, e.g. Dragoon needs Cybernetics Core.
     */
    List<Producible> immediatePrerequisites();

    /**
     * The unit type that produces this, as an id resolvable by the facility
     * registry.
     */
    String producerTypeId();

    /** Buildings and (future) wall parts need a tile; units and tech do not. */
    boolean requiresPlacement();
}
