package atlantis.production.v2.execution;

import atlantis.production.v2.ProductionItem;

/**
 * What the dispatcher did with one due item this frame: the item, whether a
 * command left, and the reason when it did not. It is the unit of the dry-run
 * comparison log (M4) and the assertion target of the dispatcher tests.
 */
public final class DispatchResult {

    private final ProductionItem item;
    private final boolean issued;
    private final String detail;

    public DispatchResult(ProductionItem item, boolean issued, String detail) {
        this.item = item;
        this.issued = issued;
        this.detail = detail;
    }

    public ProductionItem item() {
        return item;
    }

    public boolean issued() {
        return issued;
    }

    public String detail() {
        return detail;
    }

    @Override
    public String toString() {
        return (issued ? "OK " : "-- ") + item.item().id() + "@" + item.startFrame() + " (" + detail + ")";
    }
}
