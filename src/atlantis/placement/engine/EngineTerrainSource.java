package atlantis.placement.engine;

import atlantis.map.position.APosition;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

/**
 * The engine-backed {@link TileAvailabilityGrid.TerrainSource}: the one place
 * the
 * placement core reads the game.
 *
 * <p>
 * It answers only terrain questions and only through existing ports
 * ({@code MapTiles} for buildable/walkable, {@code Select} for resources and
 * depots), so it never touches BWAPI directly - the same rule the rest of the
 * production package follows. Everything else in
 * {@code atlantis.placement.core}
 * is pure and unit-testable with a synthetic map.
 * </p>
 */
public final class EngineTerrainSource implements TileAvailabilityGrid.TerrainSource {

    @Override
    public int mapWidth() {
        return atlantis.map.AMap.getMapWidthInTiles();
    }

    @Override
    public int mapHeight() {
        return atlantis.map.AMap.getMapHeightInTiles();
    }

    @Override
    public boolean isBuildable(int tx, int ty) {
        return APosition.create(tx, ty).isBuildableNotIncludingBuildings();
    }

    @Override
    public boolean isWalkable(int tx, int ty) {
        return APosition.create(tx, ty).isWalkable();
    }

    /**
     * True when a mineral field or geyser occupies the tile. Only the tile itself
     * is checked here; the geyser collection path beside a base is handled by the
     * depot rule below (Stardust blocks the same 3x2 strip, and S1 keeps the
     * simpler "do not obstruct the gap between the geyser and the depot" as part
     * of that footprint).
     */
    @Override
    public boolean isResource(int tx, int ty) {
        return resourceAt(tx, ty);
    }

    @Override
    public boolean isDepotOrigin(int tx, int ty) {
        return depotAt(tx, ty) != null;
    }

    private static boolean resourceAt(int tx, int ty) {
        for (AUnit resource : Select.mineralsAndGeysers().list()) {
            if (covers(resource, tx, ty))
                return true;
        }
        return false;
    }

    private static AUnit depotAt(int tx, int ty) {
        for (AUnit base : Select.all().bases().list()) {
            if (covers(base, tx, ty)) return base;
        }
        return null;
    }

    /** Tile-rectangle containment, our side and the enemy's alike. */
    private static boolean covers(AUnit unit, int tx, int ty) {
        if (unit == null || unit.position() == null)
            return false;

        int left = unit.tx();
        int top = unit.ty();
        return tx >= left && tx < left + unit.type().getTilesWidth()
                && ty >= top && ty < top + unit.type().getTilesHeights();
    }
}
