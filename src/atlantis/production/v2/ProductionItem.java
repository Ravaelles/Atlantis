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

    public ProductionItem(Producible item, int startFrame, boolean isPrerequisite) {
        this.item = item;
        this.startFrame = startFrame;
        this.completionFrame = startFrame + item.buildDurationFrames();
        this.isPrerequisite = isPrerequisite;
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

    @Override
    public String toString() {
        return (isPrerequisite ? "[pre] " : "") + item.id() + "@" + startFrame + "-" + completionFrame;
    }
}
