package atlantis.combat.running;

import atlantis.debug.painter.APainter;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import bwapi.Color;

public class IsReasonablePositionToRunTo {
    private static AUnit unit;

    /**
     * Returns true if given run position is traversable, land-connected and not very, very far
     */
    public static boolean check(AUnit unit, HasPosition position, HasPosition runFrom) {
        return check(
            unit, position, runFrom, null, null
        );
    }

    public static boolean check(
        AUnit unit, HasPosition position, HasPosition runFrom,
        String charForIsOk, String charForNotOk
    ) {
        IsReasonablePositionToRunTo.unit = unit;

        if (position == null) return false;
        if (unit.isFlying()) return true;

//        position = position.makeWalkable(1);
        if (position == null || (unit.isGroundUnit() && !position.isWalkable())) return false;

//        if (runFrom != null && position.distTo(unit) < position.distTo(runFrom)) {
//            System.err.println("er wut, why running towards enemy. " +
//                "\n     E2P: " + position.distTo(runFrom) +
//                "\n     U2P: " + position.distTo(unit) +
//                "\n     DIF: " + A.digit(position.distTo(unit) - position.distTo(runFrom))
//            );
////            return false;
//        }

        boolean isWalkable = isWalkableAndFree(position, unit);

        boolean isOkay = isWalkable
//                )
//                && (!includeUnitCheck || Select.our().exclude(this.unit).inRadius(0.6, position).count() <= 0)
//                && Select.ourWithUnfinished().exclude(unit).inRadius(unit.size(), position).count() <= 0
//                && distToNearestRegionBoundaryIsOkay(position)
            && unit.position().groundDistanceTo(position) <= 12
            && (dontAvoidMinerals(unit) || notTooCloseToGeysersOrMinerals(unit, position))
            && isNotTooCloseToMinerals(position, unit)
            && unit.hasPathTo(position);
//                && Select.enemy().inRadius(1.2, position).count() == 0
//                && Select.ourBuildings().inRadius(1.2, position).count() == 0

        if (charForIsOk != null) {
            APainter.paintTextCentered(position, isOkay ? charForIsOk : charForNotOk, isOkay ? Color.Green : Color.Red);
        }

        return isOkay;
    }

    private static boolean dontAvoidMinerals(AUnit unit) {
        return false;
//        return unit.isNotLarge() && !unit.isWorker();
    }

    private static boolean isWalkableAndFree(HasPosition position, AUnit unit) {
        boolean positionWalkable = position.isWalkable() && noBuildingsNorUnitsOn(position);

        if (dontAvoidMinerals(unit) || position.isCloseToMapBounds()) return positionWalkable;

        return positionWalkable && areNearbyTilesWalkable(position);
    }

    private static boolean noBuildingsNorUnitsOn(HasPosition position) {
        return Select.all().groundUnits().inRadius(0.15, position).exclude(unit).isEmpty();
    }

    private static boolean areNearbyTilesWalkable(HasPosition position) {
        int walkRadius = 32;

        return position.translateByPixels(-walkRadius, -walkRadius).isWalkable()
            && position.translateByPixels(walkRadius, walkRadius).isWalkable()
            && position.translateByPixels(walkRadius, -walkRadius).isWalkable()
            && position.translateByPixels(-walkRadius, -walkRadius).isWalkable();
    }

    private static boolean notTooCloseToGeysersOrMinerals(AUnit unit, HasPosition position) {
        return Select.mineralsAndGeysers()
//            .inRadius(Math.max(1.2, unit.size() * 5), position)
            .inRadius(1.3, position)
            .exclude(unit).isEmpty();
    }

    private static boolean isNotTooCloseToMinerals(HasPosition position, AUnit unit) {
        return Select.mineralsAndGeysers().inRadius(unit.isNotLarge() ? 1 : 6, position).isEmpty();
    }
}