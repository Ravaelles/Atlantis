package atlantis.production.v2;

import java.util.ArrayList;
import java.util.List;

/**
 * The outcome of one scheduler pass: every item that will be produced, when,
 * and where a building goes. Stateless - recreated from scratch every frame
 * and discarded; nothing here survives to the next frame.
 */
public final class ProductionPlan {

    private final List<ProductionItem> items = new ArrayList<>();

    public void add(ProductionItem item) {
        items.add(item);
    }

    public List<ProductionItem> items() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }

    /** First planned item of the given producible, or null. */
    public ProductionItem firstOf(Producible producible) {
        for (ProductionItem item : items) {
            if (item.item().id().equals(producible.id()))
                return item;
        }
        return null;
    }

    /** True when at least one item of this producible is already in the plan. */
    public boolean contains(Producible producible) {
        return firstOf(producible) != null;
    }

    @Override
    public String toString() {
        return "Plan{" + items + "}";
    }
}
