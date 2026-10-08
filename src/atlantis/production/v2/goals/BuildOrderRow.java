package atlantis.production.v2.goals;

import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.v2.Producible;
import atlantis.production.v2.TechProducible;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.UpgradeProducible;

import java.util.ArrayList;
import java.util.List;

/**
 * One production line of a text build order, as a value: what, at which
 * supply, how many, and the raw position modifier ("MAIN", "NATURAL"...).
 * Mission and setting rows are not production and never become a row.
 */
public final class BuildOrderRow {

    private final Producible item;
    private final int minSupply;
    private final int multiplicity;
    private final String positionModifier;

    public BuildOrderRow(Producible item, int minSupply, int multiplicity, String positionModifier) {
        this.item = item;
        this.minSupply = minSupply;
        this.multiplicity = Math.max(1, multiplicity);
        this.positionModifier = positionModifier;
    }

    public static BuildOrderRow of(Producible item, int minSupply) {
        return new BuildOrderRow(item, minSupply, 1, null);
    }

    /** Adapter from the legacy parser output; drops mission rows. */
    public static List<BuildOrderRow> fromLegacy(List<ProductionOrder> orders) {
        List<BuildOrderRow> rows = new ArrayList<>();
        if (orders == null)
            return rows;

        for (ProductionOrder order : orders) {
            Producible item = producibleOf(order);
            if (item == null)
                continue;

            String modifier = order.getModifier();
            int multiplicity = parseMultiplicity(modifier);
            String position = multiplicity > 1 || isMultiplier(modifier) ? null : modifier;
            rows.add(new BuildOrderRow(item, order.minSupply(), multiplicity, position));
        }
        return rows;
    }

    /** "x3" -> 3; anything else -> 1. */
    static int parseMultiplicity(String modifier) {
        if (!isMultiplier(modifier))
            return 1;
        try {
            return Math.max(1, Integer.parseInt(modifier.trim().substring(1)));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static boolean isMultiplier(String modifier) {
        return modifier != null && modifier.trim().length() > 1
                && Character.toLowerCase(modifier.trim().charAt(0)) == 'x';
    }

    private static Producible producibleOf(ProductionOrder order) {
        if (order.unitType() != null)
            return UnitProducible.of(order.unitType());
        if (order.tech() != null)
            return TechProducible.of(order.tech());
        if (order.upgrade() != null)
            return UpgradeProducible.of(order.upgrade());
        return null;
    }

    public Producible item() {
        return item;
    }

    public int minSupply() {
        return minSupply;
    }

    public int multiplicity() {
        return multiplicity;
    }

    /** Raw position modifier, or null. */
    public String positionModifier() {
        return positionModifier;
    }

    @Override
    public String toString() {
        return minSupply + " - " + item.id() + (multiplicity > 1 ? " x" + multiplicity : "")
                + (positionModifier != null ? " @" + positionModifier : "");
    }
}
