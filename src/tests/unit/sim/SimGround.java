package tests.unit.sim;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.snapshot.MapSnapshot;
import atlantis.units.AUnitType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Single map-truth authority for the economy sim, read off one snapshot.
 *
 * <p>{@code SimArena} owns geometric obstacles for combat scenarios;
 * {@code SimMap} installs tile/region answers into the bot's statics.
 * This class answers the sim driver's own questions - walkability,
 * buildability, reachability, footprints, ground distance - from the
 * same snapshot object, so the two authorities cannot disagree: there
 * is one source of tiles, and it is this file's input, not a second
 * copy of the meaning.</p>
 */
public final class SimGround {

    private final MapSnapshot snap;

    public SimGround(MapSnapshot snap) {
        this.snap = snap;
    }

    public int mapWidthTiles() {
        return snap.w;
    }

    public int mapHeightTiles() {
        return snap.h;
    }

    public boolean inBounds(int tx, int ty) {
        return tx >= 0 && ty >= 0 && tx < snap.w && ty < snap.h;
    }

    /** Tiles: '.' buildable, 'w' walkable-only, 'x' blocked. */
    public char tileAt(int tx, int ty) {
        if (!inBounds(tx, ty)) return 'x';
        String row = snap.tiles.get(ty);

        return tx < row.length() ? row.charAt(tx) : 'x';
    }

    public boolean isWalkableTile(int tx, int ty) {
        return tileAt(tx, ty) != 'x';
    }

    public boolean isBuildableTile(int tx, int ty) {
        return tileAt(tx, ty) == '.';
    }

    /** Every footprint tile must be buildable (OpenBW rule 6, simplified). */
    public boolean footprintBuildable(AUnitType type, int tx, int ty) {
        int w = type.getTilesWidth();
        int h = type.getTilesHeights();
        for (int y = ty; y < ty + h; y++) {
            for (int x = tx; x < tx + w; x++) {
                if (!isBuildableTile(x, y)) return false;
            }
        }

        return true;
    }

    /** True when a geyser resource sits on this tile (refinery rule). */
    public boolean isGeyserTile(int tx, int ty) {
        for (MapSnapshot.Res res : snap.resources) {
            if ("G".equals(res.t) && res.x == tx && res.y == ty) return true;
        }

        return false;
    }

    /** Region id under a tile, or -1 outside any region. */
    public int regionIdAt(int tx, int ty) {
        if (!inBounds(tx, ty)) return -1;

        return snap.regionOf.get(ty * snap.w + tx);
    }

    /**
     * Reachability through walkable neighbours (OpenBW
     * {@code is_reachable}: region-group equality). Same region answers
     * true without touching the matrix.
     */
    public boolean hasPathBetween(HasPosition from, HasPosition to) {
        if (from == null || to == null) return false;

        int idA = regionIdAt(from.position().x() / 32, from.position().y() / 32);
        int idB = regionIdAt(to.position().x() / 32, to.position().y() / 32);
        if (idA < 0 || idB < 0) return false;
        if (idA == idB) return true;

        int n = snap.regions.size();
        if (n > 0 && snap.regionReach.size() == n * n) {
            int a = indexOfRegion(idA);
            int b = indexOfRegion(idB);
            if (a >= 0 && b >= 0) return snap.regionReach.get(a * n + b) == 1;
        }

        return bfsConnected(idA, idB);
    }

    /**
     * Tile BFS ground distance (4-neighbourhood, walkable tiles only),
     * or -1 when unreachable. FAITHFUL travel resolves walks on this;
     * FAST uses Euclidean segments and only validates endpoints.
     */
    public int groundDistTiles(APosition from, APosition to) {
        int sx = from.x() / 32;
        int sy = from.y() / 32;
        int dx = to.x() / 32;
        int dy = to.y() / 32;
        if (!isWalkableTile(sx, sy) || !isWalkableTile(dx, dy)) return -1;
        if (sx == dx && sy == dy) return 0;

        int w = snap.w;
        int h = snap.h;
        int[] dist = new int[w * h];
        for (int i = 0; i < dist.length; i++) dist[i] = -1;
        ArrayDeque<Integer> open = new ArrayDeque<>();
        open.add(sy * w + sx);
        dist[sy * w + sx] = 0;
        int[] stepX = { 1, -1, 0, 0 };
        int[] stepY = { 0, 0, 1, -1 };
        while (!open.isEmpty()) {
            int cur = open.poll();
            int cx = cur % w;
            int cy = cur / w;
            for (int d = 0; d < 4; d++) {
                int nx = cx + stepX[d];
                int ny = cy + stepY[d];
                if (!isWalkableTile(nx, ny)) continue;
                int at = ny * w + nx;
                if (dist[at] >= 0) continue;
                dist[at] = dist[cur] + 1;
                if (nx == dx && ny == dy) return dist[at];

                open.add(at);
            }
        }

        return -1;
    }

    /**
     * Footprint overlap between a building placed at (tx, ty) and a
     * standing unit, in tiles. Shared by order validation and the
     * warp-start proximity check so both answer the same question.
     */
    public static boolean footprintsOverlap(AUnitType type, int tx, int ty,
            atlantis.units.AUnit other) {
        AUnitType otherType = other.type();
        int ax1 = tx;
        int ay1 = ty;
        int ax2 = tx + type.getTilesWidth();
        int ay2 = ty + type.getTilesHeights();
        int bx1 = other.position().tx();
        int by1 = other.position().ty();
        int bx2 = bx1 + otherType.getTilesWidth();
        int by2 = by1 + otherType.getTilesHeights();

        return ax1 < bx2 && bx1 < ax2 && ay1 < by2 && by1 < ay2;
    }

    /** Tiles of a footprint as positions, for occupancy checks. */
    public List<APosition> footprintTiles(AUnitType type, int tx, int ty) {
        List<APosition> out = new ArrayList<>();
        for (int y = ty; y < ty + type.getTilesHeights(); y++) {
            for (int x = tx; x < tx + type.getTilesWidth(); x++) {
                out.add(APosition.create(x, y));
            }
        }

        return out;
    }

    /** Mineral patch ids of one snapshot base, for miner assignment. */
    public List<MapSnapshot.Res> baseMinerals(MapSnapshot.Base base) {
        List<MapSnapshot.Res> out = new ArrayList<>();
        for (int id : base.mineralIds) {
            if (id >= 0 && id < snap.resources.size()) out.add(snap.resources.get(id));
        }

        return out;
    }

    private int indexOfRegion(int id) {
        for (int i = 0; i < snap.regions.size(); i++) {
            if (snap.regions.get(i).id == id) return i;
        }

        return -1;
    }

    private boolean bfsConnected(int idA, int idB) {
        Set<Integer> seen = new java.util.HashSet<>();
        ArrayDeque<Integer> open = new ArrayDeque<>();
        open.add(idA);
        seen.add(idA);
        java.util.Map<Integer, List<Integer>> adj = new java.util.HashMap<>();
        for (MapSnapshot.Region r : snap.regions) adj.put(r.id, r.neighbors);
        while (!open.isEmpty()) {
            int cur = open.poll();
            List<Integer> next = adj.get(cur);
            if (next == null) continue;
            for (int id : next) {
                if (id == idB) return true;

                if (seen.add(id)) open.add(id);
            }
        }

        return false;
    }
}
