package atlantis.production.constructions.builders;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class MoveUnitsFromConstructionPlace {
    /**
     * Shove other units off a construction site, so the builder can take it.
     *
     * <p>
     * <b>`exclude(unit)` is load-bearing, and it was missing (NEXT.md #53).</b> The loop below
     * used to iterate `unit.friendsNear().nonBuildings()`, which INCLUDES the unit doing the
     * moving - the builder itself. So whenever a builder came within 3 tiles of its own site
     * it told itself to step two tiles away from it, and their own objective. Measured, the
     * result is a worker frozen mid-approach while holding `lastCommand=Move`:
     * </p>
     *
     * <pre>
     *   unit=#119 Probe @[9,45]   (identical frame after frame)
   *   distToBuild=13.2   isMoving=false (4464 frames vs 959 true)   lastCommand=Move
     * </pre>
     *
     * <p>
     * The builder is not an obstacle to its own construction - the engine moves it onto the
     * footprint when the building starts. That is the same rule the occupancy guard follows
     * (NEXT.md #49: "the footprint may contain the assigned builder and nothing else"), and
     * this method was the one place still acting on the opposite belief.
     * </p>
     */
    public static boolean move(AUnit unit, Construction construction, double distanceToConstruction) {
        if (distanceToConstruction >= 4) return false;

        APosition buildPosition = construction.buildPosition();
        if (buildPosition == null) return false;

        if (unit.enemiesNear().groundUnits().countInRadius(6, unit) >= 3) return false;

        for (AUnit otherUnit : unit.friendsNear().nonBuildings().exclude(unit).list()) {
            if (otherUnit.distTo(buildPosition) >= 3) continue;
            if (otherUnit.isAttacking() || otherUnit.isRunningOrRetreating() || otherUnit.hasCooldown()) continue;

            if (otherUnit.moveAwayFrom(buildPosition, 3, Actions.MOVE_SPACE)) {
                otherUnit.setTooltip("MovingFromConstructionPlaceA");
            }
            else if (otherUnit.moveAwayFrom(buildPosition, 1, Actions.MOVE_SPACE)) {
                otherUnit.setTooltip("MovingFromConstructionPlaceB");
            }
        }

        return true;
    }
}
