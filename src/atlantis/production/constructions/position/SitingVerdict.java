package atlantis.production.constructions.position;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.conditions.TooCloseToMineralsOrGeyser;
import atlantis.production.constructions.position.conditions.can_build_here.IsPoweredByAPylon;
import atlantis.production.constructions.position.conditions.can_build_here.IsWorkerCovered;
import atlantis.units.AUnitType;

/**
 * The single siting verdict (NEXT V3 S1-S2): one function owns the
 * per-tile answers - refused tile, footprint power, geyser/minerals,
 * standing-worker cover (S1), map edge (S2) - so no path can serve a
 * tile another path would decline. Lives next to the predicates it
 * consults (same package the placement engine already imports), so it
 * adds no new architecture edges; S4 relocates the whole family
 * together when the legacy spiral goes.
 *
 * <p>NEEDS_SUPPORT_PYLON lives outside this verdict on purpose: it
 * answers a different question ("this tile will work once a Pylon
 * lands", asked while ranking candidates) while the verdict answers
 * a finished tile ("can a builder be sent there now"). The scheduler
 * keeps the support flow; the verdict never mints demand.</p>
 *
 * <p>V2-direct deliberately skips the worker-cover gate (S1): the sim
 * assigns builders on covered tiles fine and HealthyMinimumBuildTest
 * stalls with no Gateway when the loop skips them. The live
 * builderless death the funnel guards against is not modeled in the
 * sim - unified at the S4 single door with live proof.</p>
 */
public final class SitingVerdict {

    /**
     * Map-edge margin in build tiles, every path (owner 2026-11-04):
     * no Protoss building (bases and gas exempt - fixed positions)
     * within 4 tiles of any map edge. Replaces the legacy 7-tile
     * early-Pylon rule and the 1-tile catalogue margin with one
     * number; the Steamhammer edge-Pylon shape is unservable.
     */
    public static final int EDGE_MARGIN_TILES = 4;

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
     * tile), then geyser/minerals, then standing-worker cover, then
     * the map edge (S2: 4 tiles everywhere). Builder-free and
     * nearTo-free by design: travel and reachability stay with the
     * caller, which owns live state the verdict must never read.
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

        if (isTooCloseToEdge(building, tile)) {
            return refuse("Too close to map edge", "too-close-to-edge");
        }

        return accept();
    }

    /**
     * The shared edge predicate (S2): the V2-direct loop consults this
     * directly (it cannot take the full verdict - an unpowered tile
     * must read NEEDS_SUPPORT, not REFUSE, so the support Pylon still
     * mints). Same numbers as the verdict gate, no second idea of
     * "close". Null-safe by construction: with no game there is no
     * map to be close to the edge of.
     */
    public static boolean isTooCloseToEdge(AUnitType building, APosition tile) {
        if (building == null || tile == null) return false;
        if (!building.isProtoss()) return false;
        if (building.isBase() || building.isGasBuilding()) return false;

        try {
            int mapW = atlantis.map.AMap.getMapWidthInTiles();
            int mapH = atlantis.map.AMap.getMapHeightInTiles();
            if (mapW <= 0 || mapH <= 0) return false;

            int tx = tile.tx();
            int ty = tile.ty();
            int w = Math.max(1, building.getTilesWidth());
            int h = Math.max(1, building.getTilesHeights());

            if (tx < EDGE_MARGIN_TILES || ty < EDGE_MARGIN_TILES) return true;
            if (tx + w > mapW - EDGE_MARGIN_TILES) return true;
            if (ty + h > mapH - EDGE_MARGIN_TILES) return true;

            return false;
        } catch (Exception | Error ignored) {
            return false;
        }
    }
}
