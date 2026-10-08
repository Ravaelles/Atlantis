package atlantis.placement.core;

/**
 * Per-tile availability flags, the map layer of the placement rewrite
 * (`_AI/redesign/03_PLACEMENT.md` §1.2, §2 Step 0.1).
 *
 * <p>
 * Four flags, one {@code byte} per tile - the same model Stardust uses:
 * </p>
 * <ul>
 * <li>{@link #BLOCKED} - unbuildable terrain, off-map, a resource depot, or a
 * geyser path we must not obstruct;</li>
 * <li>{@link #ADJACENT} - free, but touches a blocked tile, so a building here
 * would sit flush against bad terrain or a depot;</li>
 * <li>{@link #USED} - claimed by a placed building or by a location reserved
 * earlier in this planning pass;</li>
 * <li>{@link #BORDER} - free, but immediately next to a used/border tile; keeps
 * two buildings from being packed shoulder to shoulder.</li>
 * </ul>
 *
 * <p>
 * <b>Pure:</b> the terrain answers come from a {@link TerrainSource}, so the
 * grid
 * can be built and inspected in a unit test with a synthetic map, and
 * production
 * installs the engine-backed source. Nothing here reads BWAPI.
 * </p>
 */
public final class TileAvailabilityGrid {

    public static final int BLOCKED = 1;
    public static final int ADJACENT = 2;
    public static final int USED = 4;
    public static final int BORDER = 8;

    /** The raw terrain questions the grid needs; production supplies the engine. */
    public interface TerrainSource {
        int mapWidth();

        int mapHeight();

        /** Terrain can carry a building here at all (not water, not cliff). */
        boolean isBuildable(int tx, int ty);

        /** Walkable terrain; a buildable-but-unwalkable tile is a wall/cliff face. */
        boolean isWalkable(int tx, int ty);

        /**
         * True when a resource (mineral field or geyser) occupies this tile - the
         * grid blocks these and, for geysers, the collection path beside them.
         */
        boolean isResource(int tx, int ty);

        /**
         * True when a depot (Nexus/Command Center/Hatchery) footprint starts here,
         * so no Block may overlap a base.
         */
        boolean isDepotOrigin(int tx, int ty);
    }

    private final int width;
    private final int height;
    private final byte[] flags;

    public TileAvailabilityGrid(TerrainSource terrain) {
        this.width = terrain.mapWidth();
        this.height = terrain.mapHeight();
        this.flags = new byte[width * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                markTerrain(terrain, x, y);
            }
        }
    }

    /**
     * Terrain blocking, plus the one-tile margin Stardust enforces: a blocked
     * tile makes its eight neighbours {@link #ADJACENT}, so no building sits flush
     * against impassable terrain or a depot border.
     */
    private void markTerrain(TerrainSource terrain, int x, int y) {
        if (!terrain.isBuildable(x, y)
                || !terrain.isWalkable(x, y)
                || terrain.isResource(x, y)
                || terrain.isDepotOrigin(x, y)) {
            block(x, y);
        }
    }

    /** Marks a tile blocked and its neighbours adjacent. */
    public void block(int tx, int ty) {
        if (!inBounds(tx, ty))
            return;
        flags[index(tx, ty)] |= BLOCKED;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;
                int nx = tx + dx;
                int ny = ty + dy;
                if (inBounds(nx, ny))
                    flags[index(nx, ny)] |= ADJACENT;
            }
        }
    }

    /** Marks a rectangle used, and its surrounding ring as border. */
    public void markUsed(int left, int top, int tileWidth, int tileHeight) {
        for (int x = left; x < left + tileWidth; x++) {
            for (int y = top; y < top + tileHeight; y++) {
                if (inBounds(x, y))
                    flags[index(x, y)] |= USED;
            }
        }

        for (int x = left - 1; x <= left + tileWidth; x++) {
            for (int y = top - 1; y <= top + tileHeight; y++) {
                if (x >= left && x < left + tileWidth && y >= top && y < top + tileHeight)
                    continue;
                if (inBounds(x, y))
                    flags[index(x, y)] |= BORDER;
            }
        }
    }

    /** Frees a rectangle again (a building was cancelled or destroyed). */
    public void markFreed(int left, int top, int tileWidth, int tileHeight) {
        for (int x = left; x < left + tileWidth; x++) {
            for (int y = top; y < top + tileHeight; y++) {
                if (inBounds(x, y))
                    flags[index(x, y)] &= ~USED;
            }
        }

        for (int x = left - 1; x <= left + tileWidth; x++) {
            for (int y = top - 1; y <= top + tileHeight; y++) {
                if (x >= left && x < left + tileWidth && y >= top && y < top + tileHeight)
                    continue;
                if (inBounds(x, y))
                    flags[index(x, y)] &= ~BORDER;
            }
        }
    }

    /**
     * Can a {@code w x h} building start at this tile? Every covered tile must be
     * free of every flag - the same "the whole footprint fits" rule as
     * Stardust's {@code checkTile}.
     */
    public boolean isFreeFor(int left, int top, int w, int h) {
        for (int x = left; x < left + w; x++) {
            for (int y = top; y < top + h; y++) {
                if (!inBounds(x, y))
                    return false;
                if (flags[index(x, y)] != 0)
                    return false;
            }
        }
        return true;
    }

    public int flagsAt(int tx, int ty) {
        return inBounds(tx, ty) ? flags[index(tx, ty)] : BLOCKED;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    private boolean inBounds(int tx, int ty) {
        return tx >= 0 && ty >= 0 && tx < width && ty < height;
    }

    private int index(int tx, int ty) {
        return ty * width + tx;
    }
}
