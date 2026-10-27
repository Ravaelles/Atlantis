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

        for (AUnit unit : Select.all().list()) {
            if (unit == null || !unit.isAlive())
                continue;
            if (builder != null && unit.id() == builder.id())
                continue;
            if (!unit.isABuilding() && !unit.isGroundUnit())
                continue;

            if (overlaps(unit, position, buildingType))

                return true;
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

        for (AUnit unit : Select.all().list()) {
            if (unit == null || !unit.isAlive())
                continue;
            // Only completed buildings count: a warping one is not a
            // verdict, it is the construction itself in progress -
            // counting it cancels our own warp every 23 frames and
            // re-requests the same tile until something else breaks the
            // loop (measured: 15x Gateway cancel/re-request at [112,19]
            // while its own warp stood there). An unfinished overlap
            // waits (the warp settles it, B-41 adopts); a completed one
            // kills the tile and the caller moves on.
            if (!unit.isABuilding() || !unit.isCompleted())
                continue;

            if (overlaps(unit, position, buildingType))

                return true;
        }

        return false;
    }

    /**
     * Tile-rectangle overlap between the unit and the building placed at
     * {@code position}.
     */
    private static boolean overlaps(AUnit unit, APosition position, AUnitType buildingType) {
        int buildingLeft = position.tx();
        int buildingTop = position.ty();
        int buildingRight = buildingLeft + buildingType.getTilesWidth();
        int buildingBottom = buildingTop + buildingType.getTilesHeights();

        // Top-left on both sides: tx()/ty() read the CENTER tile on
        // live units (engine pixels) while sites are top-left, so an even
        // footprint reads one tile off and an adjacent site looks covered
        // (measured live: Pylon [95,121] cancelled 256x in a row against
        // standing [93,121]). tilePosition() is top-left in both worlds
        // (engine truth live, stored tile on fakes).
        TilePosition leftTop = unit.tilePosition();
        if (leftTop == null) return false;

        int unitLeft = leftTop.getX();
        int unitTop = leftTop.getY();
        int unitRight = unitLeft + unit.type().getTilesWidth();
        int unitBottom = unitTop + unit.type().getTilesHeights();

        return unitLeft < buildingRight
            && unitRight > buildingLeft
            && unitTop < buildingBottom
            && unitBottom > buildingTop;
    }
}
