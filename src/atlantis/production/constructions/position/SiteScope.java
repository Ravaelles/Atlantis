package atlantis.production.constructions.position;

import atlantis.units.AUnitType;
import atlantis.util.We;

/**
 * The catalogue's scope gate, once instead of once per entry: which
 * buildings the catalogue answers at all. Both funnel entries (queue rows
 * with orders, sited requests without) decline identically - gas and bases
 * keep dedicated finders, other races stay legacy (D8). Returns the decline
 * reason, or null when in scope (null buildings stay silent: garbage in,
 * no row out). Builder liveness is not scope: it is checked where the
 * proposal issues, in {@code propose}.
 */
final class SiteScope {

    private SiteScope() {
    }

    /**
     * Null means in scope; anything else is the twin's decline reason.
     */
    static String declineReason(AUnitType building) {
        if (building == null) return null;
        if (building.isGasBuilding() || building.isBase()) return "dedicated-finder";
        if (!We.protoss()) return "non-protoss";

        return null;
    }
}
