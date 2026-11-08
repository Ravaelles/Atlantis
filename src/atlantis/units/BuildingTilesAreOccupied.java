package atlantis.units;

import atlantis.map.position.APosition;
import bwapi.TilePosition;
import atlantis.units.select.Select;

/**
 * "Is something already standing on the tiles this building would occupy?"
 *
 * <p>
 * This answers that question from the <b>live unit list</b> - not from the
 * engine's tile query and not from a cached grid. Both of those have
 * been wrong for this question:
 * </p>
 * <ul>
 * <li>the engine's {@code isBuildable}/{@code canBuildHere} refuse valid empty
 * tiles on OpenBW (documented in {@code MapTiles.canBuildHere}), which made
 * the finder re-find a tile and cancel the construction in a loop
 * ("position still not good after refresh / buildable:false");</li>
 * <li>a cached occupancy grid goes stale by construction (measured
 * 2026-10-08: frame-0 snapshot, every later building missing, new pylons
 * placed on top of existing structures, "took too long (45s) /
 * buildable:false"). The old port's grid died with the port (M0 diet,
 * 04_MAPLIB).</li>
 * </ul>
 *
 * <p>
 * This class asks the world the question directly, so a tile with a unit on
 * it can never be called placeable even if a grid somewhere goes stale again
 * some day.
 * </p>
 */
public class BuildingTilesAreOccupied {

    /**
     * X-&gt;Y-&gt;Z: callers ask per tile per goal, but the unit list only
     * changes per frame. The first check in a frame snapshots every unit
     * into plain footprint rects (Z); the rest of the frame reads Z, never
     * Select, never a mock. Same answers, one scan per frame.
     */
    private static int memoFrame = Integer.MIN_VALUE;
    private static Object memoGame = null;
    private static java.util.List<int[]> memoFootprints = java.util.Collections.emptyList();

    /**
     * One rect per live unit: left, top, right, bottom, id, flags
     * (1 = building, 2 = completed, 4 = ground unit).
     */
    private static void memo(int frame) {
        Object game = atlantis.Atlantis.game();
        if (frame == memoFrame && game == memoGame) return;
        memoFrame = frame;
        memoGame = game;

        java.util.List<int[]> rects = new java.util.ArrayList<>();
        for (AUnit unit : Select.all().list()) {
            if (unit == null || !unit.isAlive()) continue;
            TilePosition leftTop = unit.tilePosition();
            if (leftTop == null) continue;
            AUnitType type = unit.type();
            if (type == null) continue;
            int flags = 0;
            if (unit.isABuilding()) flags |= 1;
            if (unit.isCompleted()) flags |= 2;
            if (unit.isGroundUnit()) flags |= 4;
            rects.add(new int[]{leftTop.getX(), leftTop.getY(),
                leftTop.getX() + type.getTilesWidth(), leftTop.getY() + type.getTilesHeights(),
                unit.id(), flags});
        }
        memoFootprints = rects;
    }

    private static boolean rectOverlaps(int[] r, int left, int top, int right, int bottom) {
        return r[0] < right && r[2] > left && r[1] < bottom && r[3] > top;
    }

    /**
     * Returns true if any building (ours or the enemy's) overlaps any tile the
     * given building type would occupy if placed with its top-left at
     * {@code position}.
     *
     * <p>
     * <b>Only buildings count.</b> A first version of this asked about every unit,
     * and that broke the other way: in a mineral line a Pylon's tiles overlap a
     * worker gathering on them or a patch two tiles over, so every sensible
     * position came back "occupied" and the bot could not place its first Pylon
     * (measured 2026-10-08: {@code occupied=true} on tiles that read walkable,
     * buildable and buildable-including-buildings, i.e. empty). A worker on a tile
     * is handled by the engine's own answer and by the builder stepping aside; a
     * building already there is what this guard is for.
     * </p>
     */
    public static boolean check(APosition position, AUnitType buildingType) {
        return check(position, buildingType, null);
    }

    /**
     * The full question, with the rule the owner stated (2026-10-10,
     * {@code NEXT.md} #49):
     *
     * <p>
     * <b>StarCraft allows a building to be placed under the unit that is about to build
     * it.</b> The engine moves that unit onto the site, so the builder is not an obstacle.
     * Every <b>other</b> unit on the footprint is: in particular a worker mining at the
     * mineral line, which is the case that made the first Pylon unbuildable and whose
     * correct answer is "ask it to move", not "weaken the rule".
     * </p>
     *
     * <p>
     * <b>Why this signature exists.</b> The two older answers were each wrong in a
     * different direction, so neither could be adjusted into the right one:
     * {@code Game.isBuildable(..., true)} counts <i>every</i> unit and therefore also
     * refuses a tile occupied only by the builder (a placement the game allows), while the
     * buildings-only version below accepted a tile a mining worker stood on (a placement
     * the game refuses). One rule, one question: <b>the footprint may contain the assigned
     * builder and nothing else</b>. When no builder is assigned yet, nothing may be on the
     * footprint at all - hence {@code builder == null} means "buildings and units both
     * block".
     * </p>
     *
     * @param builder the unit that will build, excluded from the check; {@code null} when
     *                no builder has been chosen yet, in which case nothing is excluded
     */
    public static boolean check(APosition position, AUnitType buildingType, AUnit builder) {
        if (position == null || buildingType == null)
            return false;

        memo(atlantis.game.A.now());
        int builderId = builder == null ? -1 : builder.id();
        int left = position.tx();
        int top = position.ty();
        int right = left + buildingType.getTilesWidth();
        int bottom = top + buildingType.getTilesHeights();

        for (int[] r : memoFootprints) {
            if (r[4] == builderId) continue;
            if ((r[5] & 5) == 0) continue;
            if (rectOverlaps(r, left, top, right, bottom)) return true;
        }

        return false;
    }

    /**
     * The buildings-only question, kept for the callers that genuinely ask it (a tile is
     * free of <i>structures</i>, say before any builder exists). It is no longer the whole
     * answer to "can this building stand here" - see {@link #check(APosition, AUnitType,
     * AUnit)} for that, and note the difference is exactly the mining-worker case.
     * Only <i>completed</i> buildings count: an unfinished warping one is the
     * construction itself in progress, not a verdict on the tile.
     */
    public static boolean checkBuildingsOnly(APosition position, AUnitType buildingType) {
        if (position == null || buildingType == null)
            return false;

        memo(atlantis.game.A.now());
        int left = position.tx();
        int top = position.ty();
        int right = left + buildingType.getTilesWidth();
        int bottom = top + buildingType.getTilesHeights();

        for (int[] r : memoFootprints) {
            if ((r[5] & 3) != 3) continue;
            if (rectOverlaps(r, left, top, right, bottom)) return true;
        }

        return false;
    }

    /**
     * A warping building on the footprint that is not the row's own:
     * someone else is building here, and the row will never warp
     * through them (measured live 2026-11-08: a Gateway warped on the
     * Pylon row's site, 110 s of refused Builds on a banked supply
     * block). The row's own warp never counts (its building id is
     * excluded) - resiting off your own warp would chase endlessly.
     */
    public static boolean warpingForeignBuildingOn(
        APosition position, AUnitType buildingType, AUnit ownBuilding
    ) {
        if (position == null || buildingType == null)
            return false;

        memo(atlantis.game.A.now());
        int left = position.tx();
        int top = position.ty();
        int right = left + buildingType.getTilesWidth();
        int bottom = top + buildingType.getTilesHeights();
        int ownId = ownBuilding == null ? -1 : ownBuilding.id();

        for (int[] r : memoFootprints) {
            if (r[4] == ownId) continue;
            if ((r[5] & 1) == 0 || (r[5] & 2) != 0) continue;
            if (rectOverlaps(r, left, top, right, bottom)) return true;
        }

        return false;
    }

}
