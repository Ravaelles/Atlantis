package tests.unit.sim;

import atlantis.map.position.APosition;
import atlantis.map.region.ARegion;
import atlantis.map.region.Regions;

import java.util.ArrayList;
import java.util.List;

/**
 * The sim fake map (NEXT #191, owner sketch): four quadrant regions
 * separated by blocked bands with choke gaps, instead of a walkable
 * everywhere void. Top-left is the enemy, top-right our main,
 * bottom-left the middle, bottom-right the natural; neighbours link
 * through the gaps. Installed per test (source + region list) and
 * uninstalled in {@code finally} - the arena owns the walls, this
 * owns the answers about them.
 *
 * <p>Layout on a W x H tile arena (defaults fit the 70x40 sim arenas):
 * the vertical band x in [33,36] and the horizontal band y in [18,21]
 * are blocked except 2-tile gaps (vertical band: y in [8,9] north and
 * [28,29] south; horizontal band: x in [12,13] west and [52,53]
 * east).</p>
 */
public final class SimRectMap {

    public final SimRectRegion enemy;
    public final SimRectRegion main;
    public final SimRectRegion middle;
    public final SimRectRegion natural;
    public final List<ARegion> regions;

    private SimRectMap(
        SimRectRegion enemy, SimRectRegion main, SimRectRegion middle, SimRectRegion natural
    ) {
        this.enemy = enemy;
        this.main = main;
        this.middle = middle;
        this.natural = natural;
        this.regions = new ArrayList<>();
        regions.add(enemy);
        regions.add(main);
        regions.add(middle);
        regions.add(natural);
    }

    /**
     * Blocks the bands on the arena and installs the region answers.
     * Walkability source stays the caller's job (arena-backed, like the
     * pocket tests) - this only names the ground. Tile dims come from
     * the caller: the arena itself only knows pixels.
     */
    public static SimRectMap install(SimArena arena, int tilesWide, int tilesHigh) {
        int w = tilesWide;
        int h = tilesHigh;
        if (w < 37 || h < 22) {
            throw new IllegalArgumentException("rect map needs at least 37x22 tiles");
        }

        int vx0 = 33;
        int vx1 = 36;
        int hy0 = 18;
        int hy1 = Math.min(h - 1, 21);

        // Vertical band with north + south gaps.
        arena.block(vx0 * 32, 0, (vx1 - vx0 + 1) * 32, 8 * 32);
        arena.block(vx0 * 32, 10 * 32, (vx1 - vx0 + 1) * 32, 18 * 32);
        arena.block(vx0 * 32, 30 * 32, (vx1 - vx0 + 1) * 32, Math.max(0, (h - 30)) * 32);
        // Horizontal band with west + east gaps.
        arena.block(0, hy0 * 32, 12 * 32, (hy1 - hy0 + 1) * 32);
        arena.block(14 * 32, hy0 * 32, 38 * 32, (hy1 - hy0 + 1) * 32);
        arena.block(54 * 32, hy0 * 32, Math.max(0, (w - 54)) * 32, (hy1 - hy0 + 1) * 32);

        SimRectRegion enemy = new SimRectRegion("enemy", APosition.create(16, 9));
        SimRectRegion main = new SimRectRegion("main", APosition.create(53, 9));
        SimRectRegion middle = new SimRectRegion("middle", APosition.create(16, 30));
        SimRectRegion natural = new SimRectRegion("natural", APosition.create(53, 30));

        enemy.link(main);
        main.link(natural);
        enemy.link(middle);
        middle.link(natural);

        SimRectMap map = new SimRectMap(enemy, main, middle, natural);

        Regions.useSource((tx, ty) -> map.regionAt(tx, ty));
        Regions.useRegions(new ArrayList<>(map.regions));

        return map;
    }

    public static void uninstall() {
        Regions.useEngine();
        Regions.clearRegions();
    }

    private ARegion regionAt(int tx, int ty) {
        boolean west = tx <= 33;
        boolean north = ty <= 18;
        if (west) return north ? enemy : middle;

        return north ? main : natural;
    }
}
