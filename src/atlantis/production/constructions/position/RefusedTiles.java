package atlantis.production.constructions.position;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;

import java.util.HashMap;
import java.util.Map;

/**
 * Engine-refused tiles (owner #151): tiles the engine refused a Build on
 * while our own math said buildable, with the builder present and still.
 * The refresh loop re-serves the same cursed tile forever (003802:
 * 3000+ BUILD_REFUSED_WHY on [119,12]; 184529: 3869 attempts on one
 * Nexus tile) - remembering the refusal breaks the chase at the second
 * attempt instead of the three-thousandth.
 *
 * <p>Self-correcting, not a blacklist: entries expire after
 * {@link #REFUSAL_TTL_FRAMES} frames, and a tile that stops refusing
 * simply stops being re-added. Transient refusals (move-order timing,
 * a unit standing on the tile) age out on their own; hard refusals
 * (engine says no in every flag combination) re-add every time they
 * fire, so they stay refused as long as they keep refusing.</p>
 *
 * <p>Read by the catalogue ({@code propose} declines refused tiles with
 * a twin); the legacy spiral does not consult it yet - the twin names
 * which path answered, so a legacy-served refused tile stays visible.</p>
 */
public final class RefusedTiles {

    /** Long enough to outlive any chase, short enough to forgive a freed tile. */
    public static final int REFUSAL_TTL_FRAMES = 2400;

    /**
     * Map-edge margin for structural refusals (same 4 tiles as the S2
     * siting rule): the border never becomes buildable, so an
     * engine-refused edge tile is not a transient - re-serving it only
     * re-parks a builder there (measured live: Nexus [120,0] refused
     * 5:57, re-served, parked 105 s).
     */
    public static final int EDGE_TILES = 4;

    private static final Map<String, Integer> refusedAt = new HashMap<>();
    private static final Map<String, Integer> lastTwinnedAt = new HashMap<>();
    /**
     * Engine-refused map-edge tiles: no TTL, the border does not heal.
     * Unlike the S2 verdict predicate this has no base/gas exemption -
     * an engine refusal is ground truth even for a base (a refused tile
     * never warps), while the verdict must keep servable near-edge
     * base spots open.
     */
    private static final java.util.Set<String> structuralRefusals = new java.util.HashSet<>();

    private RefusedTiles() {
    }

    public static void note(HasPosition tile, AUnitType building) {
        if (tile == null || building == null) return;

        try {
            APosition p = APosition.create(tile);
            refusedAt.put(key(p.tx(), p.ty(), building), A.now());
            if (isMapEdgeTile(p.tx(), p.ty(), building)) {
                structuralRefusals.add(key(p.tx(), p.ty(), building));
            }
            twinNote(p, building);
        } catch (Exception | Error ignored) {
        }
    }

    /**
     * Names each refusal once per tile per TTL window (NEXT #151): the
     * note is silent by design, but whether it fires at all is exactly
     * what the stuck-tile hunts need to know (measured 20261102_015204:
     * Nexus [94,112] refused thousands of times with no twin naming a
     * note). Re-notes inside the window stay silent; expiry re-arms.
     */
    private static void twinNote(APosition p, AUnitType building) {
        try {
            String k = key(p.tx(), p.ty(), building);
            Integer last = lastTwinnedAt.get(k);
            int now = atlantis.game.A.now();
            if (last != null && now - last < REFUSAL_TTL_FRAMES) return;
            lastTwinnedAt.put(k, now);
            atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.TILE_REFUSED)
                .param("tile", p.tx() + "," + p.ty())
                .param("building", building.name())
                .log(now, atlantis.game.A.minSec());
        } catch (Exception | Error ignored) {
        }
    }

    public static boolean isRefused(HasPosition tile, AUnitType building) {
        if (tile == null || building == null) return false;

        try {
            APosition p = APosition.create(tile);
            String k = key(p.tx(), p.ty(), building);
            if (structuralRefusals.contains(k)) return true;

            Integer at = refusedAt.get(k);
            if (at == null) return false;

            return A.now() - at < REFUSAL_TTL_FRAMES;
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    /**
     * Map-edge tile for the building's footprint (pure over numbers, so
     * the boundary pins without a game): tx/ty below the margin or the
     * footprint crossing the far margin. Unknown map size fails open
     * (no game attached) - the TTL path still applies. Public as a seam:
     * the V2 loop consults the numbers without taking any verdict.
     */
    public static boolean isMapEdgeTile(int tx, int ty, AUnitType building) {
        try {
            int mapW = atlantis.map.AMap.getMapWidthInTiles();
            int mapH = atlantis.map.AMap.getMapHeightInTiles();
            if (mapW <= 0 || mapH <= 0) return false;

            return isMapEdgeTile(tx, ty,
                building == null ? 1 : Math.max(1, building.getTilesWidth()),
                building == null ? 1 : Math.max(1, building.getTilesHeights()),
                mapW, mapH);
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    public static boolean isMapEdgeTile(int tx, int ty, int w, int h, int mapW, int mapH) {
        if (tx < EDGE_TILES || ty < EDGE_TILES) return true;
        if (tx + w > mapW - EDGE_TILES) return true;

        return ty + h > mapH - EDGE_TILES;
    }

    public static void clearForTests() {
        refusedAt.clear();
        lastTwinnedAt.clear();
        structuralRefusals.clear();
    }

    private static String key(int tx, int ty, AUnitType building) {
        return tx + "," + ty + "," + building.id();
    }
}
