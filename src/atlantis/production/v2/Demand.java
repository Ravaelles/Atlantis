package atlantis.production.v2;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;

/**
 * V2 demand carrier (NEXT #228, WO-147): what the v2 plan wants, in v2
 * terms. Replaces the synthetic {@code ProductionOrder} the dispatcher
 * staples onto every construction today (a queue-flavored object that is
 * never enqueued - only read for its type and modifier by siting).
 *
 * <p>Introduced alongside the synthetic order, not instead of it: readers
 * migrate one file per commit, and the synthetic dies when the last reader
 * moves. Until then the director writes both, and the pin below keeps
 * them mirrored.</p>
 */
public final class Demand {

    private final AUnitType type;
    private final String modifier;
    private final HasPosition around;

    private Demand(AUnitType type, String modifier, HasPosition around) {
        this.type = type;
        this.modifier = modifier;
        this.around = around;
    }

    public static Demand of(AUnitType type) {
        return new Demand(type, null, null);
    }

    public static Demand of(AUnitType type, String modifier, HasPosition around) {
        return new Demand(type, modifier, around);
    }

    public AUnitType type() {
        return type;
    }

    /**
     * Siting modifier (e.g. MAIN_CHOKE): the only order-flavored read the
     * position finders need.
     */
    public String modifier() {
        return modifier;
    }

    public HasPosition around() {
        return around;
    }

    @Override
    public String toString() {
        return "Demand{" + type + (modifier == null ? "" : " " + modifier) + "}";
    }
}
