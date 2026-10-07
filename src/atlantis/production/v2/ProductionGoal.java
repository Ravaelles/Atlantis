package atlantis.production.v2;

/**
 * Declarative strategy input: "I want {@code count} of this, starting around
 * this frame, placed this way, at this priority".
 *
 * <p>
 * Immutable and comparable by priority (lower integer = higher priority,
 * matching the priority bands in 01_PRODUCTION.md §5B). One goal may expand to
 * several {@link ProductionItem}s (count &gt; 1, plus prerequisites).
 * </p>
 */
public final class ProductionGoal implements Comparable<ProductionGoal> {

    /** Priority bands from 01_PRODUCTION.md §5B. */
    public static final int PRIORITY_EMERGENCY = 10;
    public static final int PRIORITY_WORKERS = 20;
    public static final int PRIORITY_DEPOTS = 30;
    public static final int PRIORITY_BASEDEFENSE = 40;
    public static final int PRIORITY_MAINARMYBASE = 60;
    public static final int PRIORITY_NORMAL = 80;
    public static final int PRIORITY_MAINARMY = 90;
    public static final int PRIORITY_LOWEST = 100;

    public static final int COUNT_CONTINUOUS = -1;

    private final Producible item;
    private final int priority;
    private final int count;
    private final int targetStartFrame;
    private final TargetPlacement placement;

    public ProductionGoal(Producible item, int priority, int count, int targetStartFrame, TargetPlacement placement) {
        this.item = item;
        this.priority = priority;
        this.count = count;
        this.targetStartFrame = targetStartFrame;
        this.placement = placement;
    }

    public static ProductionGoal emergency(Producible item) {
        return new ProductionGoal(item, PRIORITY_EMERGENCY, 1, 0, TargetPlacement.anywhere());
    }

    public Producible item() {
        return item;
    }

    public int priority() {
        return priority;
    }

    public int count() {
        return count;
    }

    public int targetStartFrame() {
        return targetStartFrame;
    }

    public TargetPlacement placement() {
        return placement;
    }

    @Override
    public int compareTo(ProductionGoal other) {
        return Integer.compare(this.priority, other.priority);
    }

    @Override
    public String toString() {
        return "Goal{" + item.id() + " x" + count + " p" + priority + "}";
    }
}
