package atlantis.production.v2;

/**
 * The three BWAPI resources a production order consumes, as an immutable value.
 *
 * <p>
 * Pure domain: no BWAPI types, no game state - the timeline math in
 * {@link ResourceTimeline} works on these and is unit-testable without a
 * game. Supply is counted in "supply used by this item" (e.g. a Dragoon is 2),
 * which is how BWAPI reports both requirements and stock.
 * </p>
 */
public final class ResourceCost {

    private final int minerals;
    private final int gas;
    private final int supply;

    public ResourceCost(int minerals, int gas, int supply) {
        this.minerals = minerals;
        this.gas = gas;
        this.supply = supply;
    }

    public static ResourceCost of(int minerals, int gas, int supply) {
        return new ResourceCost(minerals, gas, supply);
    }

    public static ResourceCost none() {
        return new ResourceCost(0, 0, 0);
    }

    public int minerals() {
        return minerals;
    }

    public int gas() {
        return gas;
    }

    public int supply() {
        return supply;
    }

    public boolean isFree() {
        return minerals == 0 && gas == 0 && supply == 0;
    }

    @Override
    public String toString() {
        return "Cost{" + minerals + "m," + gas + "g," + supply + "s}";
    }
}
