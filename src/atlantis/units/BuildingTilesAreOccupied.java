package atlantis.units;

import atlantis.map.position.APosition;
import atlantis.units.select.Select;

/**
 * "Is something already standing on the tiles this building would occupy?"
 *
 * <p>
 * This answers that question from the <b>live unit list</b> - not from the
 * engine's tile query and not from JBWEB's {@code usedGrid}. Both of those have
 * been wrong for this question:
 * </p>
 * <ul>
 * <li>the engine's {@code isBuildable}/{@code canBuildHere} refuse valid empty
 * tiles on OpenBW (documented in {@code MapTiles.canBuildHere}), which made
 * the finder re-find a tile and cancel the construction in a loop
 * ("position still not good after refresh / buildable:false");</li>
 * <li>JBWEB's {@code usedGrid} was only filled at game start, because Atlantis
 * never called {@code JBWEB.onUnitDiscover}/{@code onUnitDestroy} (only
 * {@code onStart}). Every building raised afterwards was missing from it, so
 * {@code JBWEB.isPlaceable} called an <b>occupied</b> tile free: the finder
 * placed new pylons and gateways on top of existing structures, the builder
 * arrived, could not build, and the construction was cancelled
 * ("took too long (45s) / buildable:false", measured 2026-10-08).</li>
 * </ul>
 *
 * <p>
 * The unit lifecycle is now wired into JBWEB's grid (see
 * {@code Atlantis.keepJbwebGridCurrentOnCreate}), which fixes the grid itself.
 * This class is the second, independent guard: it asks the world the question
 * directly, so a tile with a unit on it can never be called placeable even if
 * the
 * grid is stale again some day.
 * </p>
 */
public class BuildingTilesAreOccupied {

    /**
     * Returns true if any unit (ours, the enemy's, or neutral) overlaps any tile
     * the given building type would occupy if placed with its top-left at
     * {@code position}.
     */
    public static boolean check(APosition position, AUnitType buildingType) {
        if (position == null || buildingType == null)
            return false;

        for (AUnit unit : Select.all().list()) {
            if (unit == null || !unit.isAlive())
                continue;

            if (overlaps(unit, position, buildingType))
                return true;
        }

        return false;
    }

    /**
     * Tile-rectangle overlap between the unit and the building placed at
     * {@code position}. Ground units count too: a probe standing on the tile is
     * exactly what makes the engine refuse a placement.
     */
    private static boolean overlaps(AUnit unit, APosition position, AUnitType buildingType) {
        int buildingLeft = position.tx();
        int buildingTop = position.ty();
        int buildingRight = buildingLeft + buildingType.getTilesWidth();
        int buildingBottom = buildingTop + buildingType.getTilesHeights();

        int unitLeft = unit.tx();
        int unitTop = unit.ty();
        int unitRight = unitLeft + unit.type().getTilesWidth();
        int unitBottom = unitTop + unit.type().getTilesHeights();

        return unitLeft < buildingRight
                && unitRight > buildingLeft
                && unitTop < buildingBottom
                && unitBottom > buildingTop;
    }
}
