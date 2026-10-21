package atlantis.placement.core;

/**
 * Which tiles are connected to which, computed from {@link WalkabilityGrid}
 * rather than
 * asked of the engine.
 *
 * <p>
 * <b>The question this answers.</b> {@code Game.hasPath(a, b)} is the engine's
 * reachability query, and on OpenBW it answers {@code false} for every pair
 * because the
 * server publishes no regions (see {@link WalkabilityGrid}). The question
 * actually
 * wanted is usually not "what is the shortest path" but the much weaker "<b>are
 * these
 * two tiles in the same connected walkable area?</b>" - which is exactly what a
 * region
 * id encodes, and what {@code hasPath} was standing in for.
 * </p>
 *
 * <p>
 * <b>Why union-find and not an A* copy.</b> Stardust's own A*
 * ({@code PathFinding::Search}) is the right tool for travel time and choke
 * routing. For
 * connectivity it is the wrong one: it costs a search per query, and its answer
 * is only
 * as good as the heuristic, while "same connected component" is a one-pass
 * property of
 * the map. Union-find computes it once per map state in O(tiles), then answers
 * every
 * later question in O(1) - and, unlike the three disagreeing answers this
 * codebase
 * already had for "can a building stand here", it cannot disagree with itself.
 * </p>
 *
 * <p>
 * <b>8-connectivity, terrain only.</b> Diagonal steps are allowed (a unit can
 * cut a
 * corner the same way the engine's pathing does), and only walkability is
 * consulted:
 * buildings are not obstacles here on purpose, because a building that will be
 * placed is
 * not an obstacle to the builder walking to it, and treating one as such is the
 * mistake
 * {@code MapTiles.canBuildHere}'s history is made of. Rebuild on building
 * create/destroy if a caller ever needs the stricter variant.
 * </p>
 *
 * <p>
 * <b>Pure and deterministic:</b> no BWAPI, no statics, no game. A synthetic
 * tile array
 * in, region ids out - so the interesting cases (two separate areas, a one-tile
 * gap, an
 * island) are unit tests rather than game runs.
 * </p>
 */
public final class ConnectivityRegions {

    /** Marks a tile that can never be walked on, and is therefore in no region. */
    public static final int NO_REGION = -1;

    private final int width;
    private final int height;
    private final int[] regionOf;
    private final int regionCount;

    public ConnectivityRegions(WalkabilityGrid grid) {
        this.width = grid.mapWidth();
        this.height = grid.mapHeight();
        this.regionOf = new int[width * height];

        for (int i = 0; i < regionOf.length; i++) {
            regionOf[i] = NO_REGION;
        }

        this.regionCount = labelRegions(grid);
    }

    public int regionCount() {
        return regionCount;
    }

    /**
     * The connected area this tile belongs to, or {@link #NO_REGION} when it is
     * unwalkable or off the map. Two tiles can reach each other exactly when this
     * returns the same value for both - and, unlike the engine's query, a tile
     * always
     * reaches itself.
     */
    public int regionAt(int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= width || ty >= height)
            return NO_REGION;

        return regionOf[index(tx, ty)];
    }

    /**
     * True when a ground unit can get from one tile to the other. <b>A tile always
     * reaches itself</b> - the property the engine's region-based answer fails, and
     * the
     * reason a builder could not reach the site it was standing on.
     */
    public boolean areConnected(int fromTx, int fromTy, int toTx, int toTy) {
        int a = regionAt(fromTx, fromTy);
        int b = regionAt(toTx, toTy);

        return a != NO_REGION && a == b;
    }

    /**
     * Two-pass connected-component labelling (the classic union-find variant):
     * first pass
     * unions each tile with its already-visited west/north neighbours, second pass
     * assigns contiguous region numbers by resolving each tile's root. Region ids
     * are
     * deterministic for a given grid, which is what makes a test able to assert
     * them.
     */
    private int labelRegions(WalkabilityGrid grid) {
        boolean[] walkable = new boolean[width * height];
        int[] parent = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!grid.isWalkable(x, y))
                    continue;

                walkable[index(x, y)] = true;
                parent[index(x, y)] = index(x, y);

                // West and north, plus the two diagonals that raster order has already
                // visited. Each tile therefore unions with every neighbour exactly once.
                unionNeighbour(parent, walkable, x, y, x - 1, y);
                unionNeighbour(parent, walkable, x, y, x - 1, y - 1);
                unionNeighbour(parent, walkable, x, y, x, y - 1);
                unionNeighbour(parent, walkable, x, y, x + 1, y - 1);
            }
        }

        return assignRegionNumbers(walkable, parent);
    }

    private void unionNeighbour(
            int[] parent, boolean[] walkable, int x, int y, int nx, int ny) {
        if (nx < 0 || ny < 0 || nx >= width || ny >= height)
            return;

        int neighbour = index(nx, ny);
        if (!walkable[neighbour])
            return;

        union(parent, index(x, y), neighbour);
    }

    /**
     * Second pass: every walkable tile takes its root's region number. Numbers are
     * assigned in raster order over the roots, so they are deterministic for a given
     * grid and a test can assert them rather than only their equality.
     */
    private int assignRegionNumbers(boolean[] walkable, int[] parent) {
        int[] rootToRegion = new int[parent.length];
        for (int i = 0; i < rootToRegion.length; i++) {
            rootToRegion[i] = NO_REGION;
        }

        int next = 0;
        for (int i = 0; i < parent.length; i++) {
            if (!walkable[i])
                continue;

            int root = find(parent, i);
            if (rootToRegion[root] == NO_REGION) {
                rootToRegion[root] = next++;
            }

            regionOf[i] = rootToRegion[root];
        }

        return next;
    }

    private int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }

        return i;
    }

    private void union(int[] parent, int a, int b) {
        int rootA = find(parent, a);
        int rootB = find(parent, b);

        if (rootA != rootB) {
            parent[rootB] = rootA;
        }
    }

    private int index(int tx, int ty) {
        return ty * width + tx;
    }
}
