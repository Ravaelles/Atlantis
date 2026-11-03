package atlantis.production.constructions.position;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.conditions.TooCloseToMineralsOrGeyser;
import atlantis.production.constructions.position.conditions.can_build_here.IsPoweredByAPylon;
import atlantis.production.constructions.position.conditions.can_build_here.IsWorkerCovered;
import atlantis.units.AUnitType;

/**
 * The single siting verdict (NEXT V3 S1): one function owns the four
 * per-tile answers - refused tile, footprint power, geyser/minerals,
 * standing-worker cover - so no path can serve a tile another path
 * would decline. Lives next to the predicates it consults (same
 * package the placement engine already imports), so it adds no new
 * architecture edges; S4 relocates the whole family together when
 * the legacy spiral goes.
 *
 * <p>NEEDS_SUPPORT_PYLON lives outside this verdict on purpose: it
 * answers a different question ("this tile will work once a Pylon
 * lands", asked while ranking candidates) while the verdict answers
 * a finished tile ("can a builder be sent there now"). The scheduler
 * keeps the support flow; the verdict never mints demand.</p>
 */
public final class SitingVerdict {

    public enum Decision {
        ACCEPT,
        REFUSE
    }

    public final Decision decision;

    /** Human reason, doubling as the legacy `_STATUS` line. */
    public final String reason;

    /** Funnel twin key for the same refusal (`declined` rows). */
    public final String twin;

    private SitingVerdict(Decision decision, String reason, String twin) {
        this.decision = decision;
        this.reason = reason;
        this.twin = twin;
    }

    private static SitingVerdict accept() {
        return new SitingVerdict(Decision.ACCEPT, "ok", "-");
    }

    private static SitingVerdict refuse(String reason, String twin) {
        return new SitingVerdict(Decision.REFUSE, reason, twin);
    }

    public boolean accepted() {
        return decision == Decision.ACCEPT;
    }

    /**
     * One gate, in fixed order: refusal first (a deliberate human or
     * engine verdict beats geometry), then the footprint power rule
     * (the engine enforces the footprint center, not the origin
     * tile), then geyser/minerals, then standing-worker cover.
     * Builder-free and nearTo-free by design: travel and reachability
     * stay with the caller, which owns live state the verdict must
     * never read.
     */
    public static SitingVerdict check(AUnitType building, APosition tile) {
        if (building == null || tile == null) {
            return refuse("null building or tile", "no-tile");
        }

        if (RefusedTiles.isRefused(tile, building)) {
            return refuse("Tile refused by engine", "refused-tile");
        }

        if (building.needsPower() && !IsPoweredByAPylon.check(tile, building)) {
            return refuse("Not powered by a Pylon", "unpowered");
        }

        if (TooCloseToMineralsOrGeyser.isTooCloseToMineralsOrGeyser(building, tile)) {
            return refuse("Too close to minerals or gas", "mineral-gas-too-close");
        }

        if (IsWorkerCovered.check(tile)) return refuse("Worker-covered tile", "worker-covered");

        return accept();
    }
}
