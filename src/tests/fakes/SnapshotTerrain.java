package tests.fakes;

import atlantis.map.position.APosition;
import atlantis.map.snapshot.MapSnapshot;
import atlantis.placement.core.TileAvailabilityGrid;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link TileAvailabilityGrid.TerrainSource} backed by a real map
 * snapshot instead of flat terrain (NEXT V3 S0): the siting stack is
 * pinned on TauCross geometry - walkable bands, mineral lines,
 * geysers, depot footprints - so a green suite cannot serve tiles
 * the engine refuses.
 *
 * <p>Tile chars come straight from the snapshot: {@code '.'} is
 * walkable and buildable, {@code 'w'} is walkable only, everything
 * else blocks. Resources block their own tile on top of that (a
 * building never goes on minerals or gas through the ranker; the
 * Assimilator uses the exact-geyser path instead). Depot origins
 * are the snapshot's base tiles; the footprint around them is the
 * catalogue's business, not the terrain's.</p>
 */
public final class SnapshotTerrain implements TileAvailabilityGrid.TerrainSource {

    private static final String TAUCROSS_PATH = "src/tests/resources/taucross-snapshot.json";

    private static MapSnapshot tauCross;

    private final MapSnapshot snap;
    private final java.util.Set<Long> resourceTiles = new java.util.HashSet<>();
    private final java.util.Set<Long> depotTiles = new java.util.HashSet<>();

    public SnapshotTerrain(MapSnapshot snap) {
        this.snap = snap;
        for (MapSnapshot.Res res : snap.resources) {
            resourceTiles.add(key(res.x, res.y));
        }
        for (MapSnapshot.Base base : snap.bases) {
            depotTiles.add(key(base.x, base.y));
        }
    }

    private static long key(int tx, int ty) {
        return (((long) tx) << 32) | (ty & 0xffffffffL);
    }

    /**
     * The shared TauCross fixture, parsed once: every siting test
     * that needs real ground answers from the same map.
     */
    public static synchronized MapSnapshot tauCross() {
        if (tauCross == null) {
            try {
                String json = new String(
                    Files.readAllBytes(Paths.get(TAUCROSS_PATH)),
                    StandardCharsets.UTF_8);
                tauCross = MapSnapshot.parse(json);
            } catch (Exception e) {
                throw new IllegalStateException(
                    "cannot load TauCross snapshot fixture at " + TAUCROSS_PATH, e);
            }
        }

        return tauCross;
    }

    /**
     * Base tile of the n-th start location (depot footprint origin).
     */
    public APosition baseTile(int startIndex) {
        for (MapSnapshot.Base base : snap.bases) {
            if (base.startIndex == startIndex) {
                return APosition.create(base.x, base.y);
            }
        }

        throw new IllegalArgumentException("no base with startIndex " + startIndex);
    }

    /**
     * All mineral tiles of the n-th start's line, as positions.
     */
    public List<APosition> mineralLine(int startIndex) {
        List<APosition> out = new ArrayList<>();
        for (MapSnapshot.Base base : snap.bases) {
            if (base.startIndex != startIndex) continue;
            for (int id : base.mineralIds) {
                MapSnapshot.Res res = snap.resources.get(id);
                out.add(APosition.create(res.x, res.y));
            }
        }

        return out;
    }

    @Override
    public int mapWidth() {
        return snap.w;
    }

    @Override
    public int mapHeight() {
        return snap.h;
    }

    private char tileAt(int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= snap.w || ty >= snap.h) return 'x';

        return snap.tiles.get(ty).charAt(tx);
    }

    @Override
    public boolean isWalkable(int tx, int ty) {
        char c = tileAt(tx, ty);

        return c == '.' || c == 'w';
    }

    @Override
    public boolean isBuildable(int tx, int ty) {
        return tileAt(tx, ty) == '.' && !resourceTiles.contains(key(tx, ty));
    }

    @Override
    public boolean isResource(int tx, int ty) {
        return resourceTiles.contains(key(tx, ty));
    }

    @Override
    public boolean isDepotOrigin(int tx, int ty) {
        return depotTiles.contains(key(tx, ty));
    }
}
