package atlantis.production.v2;

/**
 * How a production item was planned: what, when it starts and finishes, which
 * facility produces it, where a building goes. Immutable planning node.
 *
 * <p>
 * Pure domain - the dispatcher reads it to issue real commands, the tests
 * assert on its fields. One goal expands to several of these (count, plus
 * prerequisites).
 * </p>
 */
public final class ProductionItem {

    private final Producible item;
    private final int startFrame;
    private final int completionFrame;
    private final boolean isPrerequisite;
    private final PlacementReservation placement;
    /** Id of the facility that produces it; {@link #NO_PRODUCER} for buildings and when unknown. */
    private final int producerId;

    public static final int NO_PRODUCER = 0;

    public ProductionItem(Producible item, int startFrame, boolean isPrerequisite) {
        this(item, startFrame, isPrerequisite, null, NO_PRODUCER);
    }

    public ProductionItem(Producible item, int startFrame, boolean isPrerequisite,
            PlacementReservation placement) {
        this(item, startFrame, isPrerequisite, placement, NO_PRODUCER);
    }

    /**
     * @param placement where a building goes; null for items that need no tile
     *                  (units, techs, upgrades) and for hand-built test items
     */
    public ProductionItem(Producible item, int startFrame, boolean isPrerequisite,
            PlacementReservation placement, int producerId) {
        this.item = item;
        this.producerId = producerId;
        this.startFrame = startFrame;
        this.completionFrame = startFrame + item.buildDurationFrames();
        this.isPrerequisite = isPrerequisite;
        this.placement = placement;
    }

    public Producible item() {
        return item;
    }

    public int startFrame() {
        return startFrame;
    }

    public int completionFrame() {
        return completionFrame;
    }

    /**
     * True when this was auto-inserted to unlock something else (a Core for
     * Dragoons).
     */
    public boolean isPrerequisite() {
        return isPrerequisite;
    }

    /**
     * Where the building goes, or null when the item needs no tile. The
     * dispatcher reads it to commit a builder; a plan built by the scheduler
     * always carries it for a building.
     */
    public PlacementReservation placement() {
        return placement;
    }

    /** The concrete facility (unit id) assigned to produce this, or {@link #NO_PRODUCER}. */
    public int producerId() {
        return producerId;
    }

    @Override
    public String toString() {
        return (isPrerequisite ? "[pre] " : "") + item.id() + "@" + startFrame + "-" + completionFrame
                + (producerId != NO_PRODUCER ? " by#" + producerId : "");
    }
}
