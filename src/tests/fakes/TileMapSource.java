package tests.fakes;

import atlantis.map.MapTiles;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

import java.util.HashSet;
import java.util.Set;

/**
 * A {@link MapTiles.Source} a test can draw on: everything is walkable,
 * buildable,
 * explored and visible <b>unless a tile is named as not walkable</b>.
 *
 * <p>
 * This exists so the position conditions under
 * {@code atlantis.production.constructions.position} can be tested as the pure
 * predicates they are. Those classes ask only "is this position walkable /
 * buildable
 * / powered" and then write a reason into
 * {@code AbstractPositionFinder._STATUS} -
 * they need no game, no units and no world, but without a source they reach the
 * engine and die. Several of the module's bugs (a hand-typed tile size, an
 * occupancy
 * rule that counted the wrong things) survived a long time precisely because no
 * cheap test could reach them.
 * </p>
 *
 * <p>
 * Default-allow rather than default-deny on purpose: a condition test is
 * normally
 * about the <b>one</b> thing it rejects, so the interesting setup is "the map
 * is
 * fine except here", and that reads much better as one call than as an explicit
 * allow-list of every tile the class happens to inspect.
 * </p>
 *
 * <p>
 * It is a fake, not a stub of one answer: {@link #blockTile},
 * {@link #blockRect}
 * and {@link #setBuildable} let one instance serve a whole test method, and
 * {@link #install()} / {@link #uninstall()} bracket it.
 * </p>
 */
public class TileMapSource implements MapTiles.Source {

    private final Set<Long> unwalkable = new HashSet<>();
    private final Set<Long> unbuildable = new HashSet<>();
    private final Set<Long> unexplored = new HashSet<>();

    private boolean defaultWalkable = true;
    private boolean defaultBuildable = true;
    private final boolean defaultExplored = true;

    public static TileMapSource everythingFine() {
        return new TileMapSource();
    }

    // ---- setup ----------------------------------------------------------------

    /**
     * Restores the stub world's own answers.
     */
    public static void uninstall() {
        FakeMapTiles.installAsSource();
    }

    private static long key(int tx, int ty) {
        return ((long) tx << 32) | (ty & 0xffffffffL);
    }

    /**
     * Convenience: the fakes install themselves as the tile source.
     */
    public TileMapSource install() {
        MapTiles.useSource(this);

        return this;
    }

    public TileMapSource blockTile(int tx, int ty) {
        unwalkable.add(key(tx, ty));

        return this;
    }

    public TileMapSource unbuildableTile(int tx, int ty) {
        unbuildable.add(key(tx, ty));

        return this;
    }

    public TileMapSource unexploredTile(int tx, int ty) {
        unexplored.add(key(tx, ty));

        return this;
    }

    /**
     * Every tile in the rectangle (inclusive of both corners) is unwalkable.
     */
    public TileMapSource blockRect(int x1, int y1, int x2, int y2) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                blockTile(x, y);
            }
        }
        return this;
    }

    public TileMapSource defaultWalkable(boolean value) {
        this.defaultWalkable = value;

        return this;
    }

    public TileMapSource defaultBuildable(boolean value) {
        this.defaultBuildable = value;

        return this;
    }

    // ---- MapTiles.Source ------------------------------------------------------

    @Override
    public boolean isWalkable(HasPosition at) {
        if (at == null)
            return false;

        return defaultWalkable && !unwalkable.contains(key(at.tx(), at.ty()));
    }

    @Override
    public boolean isExplored(HasPosition at) {
        if (at == null)
            return false;

        return defaultExplored && !unexplored.contains(key(at.tx(), at.ty()));
    }

    @Override
    public boolean isVisible(HasPosition at) {
        return isExplored(at);
    }

    @Override
    public boolean isBuildable(HasPosition at, boolean alsoCheckBuildings) {
        if (at == null)
            return false;

        if (unbuildable.contains(key(at.tx(), at.ty())))
            return false;

        return defaultBuildable;
    }

    /**
     * Reachability between two tiles: a straight-line answer is enough for the
     * conditions, which ask only "is there a path from the builder to here" and are
     * tested with both endpoints marked walkable when they expect yes.
     */
    @Override
    public boolean hasPathBetween(HasPosition from, HasPosition to) {
        return isWalkable(from) && isWalkable(to);
    }

    @Override
    public boolean canBuildHere(AUnit builder, AUnitType building, APosition at) {
        return isWalkable(at) && isBuildable(at, true);
    }

    /**
     * This is the stub/harness source, so the map-data fallbacks inside
     * {@code MapTiles} must not apply - the harness answers for itself.
     */
    @Override
    public boolean spawnsFromMapData() {
        return false;
    }
}
