package atlantis.production.constructions.position;

import atlantis.units.AUnitType;
import atlantis.util.We;

/**
 * The catalogue's scope gate, once instead of once per entry: which
 * buildings the catalogue answers at all. Both funnel entries (queue rows
 * with orders, sited requests without) decline identically - gas keeps
 * its dedicated finder (single geyser tile, no search), other races stay
 * legacy (D8). Bases are catalogue ground since migration A2 (owner
 * #146): the expansion demand already names the natural, the catalogue
 * ranks the tiles. Returns the decline reason, or null when in scope
 * (null buildings stay silent: garbage in, no row out). Builder liveness
 * is not scope: it is checked where the proposal issues, in {@code propose}.
 */
final class SiteScope {

    private SiteScope() {
    }

    /**
     * Null means in scope; anything else is the twin's decline reason.
     */
    static String declineReason(AUnitType building) {
        if (building == null) return null;
        if (building.isGasBuilding()) return "dedicated-finder";
        if (!We.protoss()) return "non-protoss";

        return null;
    }
}
