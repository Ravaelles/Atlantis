package atlantis.combat.running;

import atlantis.debug.painter.APainter;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.We;
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
        if (unit.isGroundUnit() && (!position.isWalkable() || !position.isPositionVisible())) return false;

//        if (runFrom != null && position.distTo(unit) < position.distTo(runFrom)) {
//            System.err.println("er wut, why running towards enemy. " +
//                "\n     E2P: " + position.distTo(runFrom) +
//                "\n     U2P: " + position.distTo(unit) +
//                "\n     DIF: " + A.digit(position.distTo(unit) - position.distTo(runFrom))
//            );
////            return false;
//        }

        boolean isWalkable = isWalkableAndFree(position, unit);
        if (!isWalkable) return false;

        if (unit.hp() >= 61) {
            if (!position.isWalkable(2)) return false;
        }

        boolean isOkay = isWalkable
//                )
//                && (!includeUnitCheck || Select.our().exclude(this.unit).inRadius(0.6, position).count() <= 0)
//                && Select.ourWithUnfinished().exclude(unit).inRadius(unit.size(), position).count() <= 0
//                && distToNearestRegionBoundaryIsOkay(position)
            && unit.position().groundDistanceTo(position) <= 12
            && notTooCloseToGeysersOrMinerals(unit, position)
//            && isNotTooCloseToMinerals(position, unit)
//            && areAllNearbyTilesWalkable(position, unit)
//            && areMostNearbyTilesWalkable(position, unit);
            && !isCloseToEnemyThanToUs(position, unit, runFrom);
//            && position.isWalkable(unit.hp() >= 120 ? 2 : 1);
//                && Select.enemy().inRadius(1.2, position).count() == 0
//                && Select.ourBuildings().inRadius(1.2, position).count() == 0

        if (charForIsOk != null) {
            APainter.paintTextCentered(position, isOkay ? charForIsOk : charForNotOk, isOkay ? Color.Green : Color.Red);
        }

        return isOkay;
    }

    private static boolean isCloseToEnemyThanToUs(HasPosition position, AUnit unit, HasPosition runFrom) {
        if (position == null) return false;
        if (!unit.isMoving()) return false;
        if (unit.targetPosition() == null || unit.distToTargetPosition() <= 2) return false;
        if (unit.hp() <= 40) return false;

        double pointToUs = position.groundDist(unit);
        double pointToEnemy = position.groundDist(runFrom);

//        System.err.println("pointToUs = " + pointToUs);
//        System.err.println("pointToEnemy = " + pointToEnemy);

//        double safetyMargin = unit.hpPercent() / 40.0;
        double safetyMargin = 0.5;

        return (pointToEnemy - safetyMargin) < pointToUs;
    }

    private static boolean dontAvoidMinerals(AUnit unit) {
        return false;
//        return unit.isNotLarge() && !unit.isWorker();
    }

    private static boolean isWalkableAndFree(HasPosition position, AUnit unit) {
        boolean positionWalkable = position.isWalkable() && noBuildingsNorUnitsOn(position);

        if (!positionWalkable) return false;

        return !position.isCloseToMapBounds();
    }

    private static boolean noBuildingsNorUnitsOn(HasPosition position) {
        return Select.all().groundUnits().inRadius(0.15, position).exclude(unit).isEmpty();
    }

    private static boolean areMostNearbyTilesWalkable(HasPosition position, AUnit unit) {
        int walkRadius = 54;
        int walkable = 0;

        if (position.translateByPixels(-walkRadius, -walkRadius).isWalkable()) walkable++;
        if (position.translateByPixels(walkRadius, walkRadius).isWalkable()) walkable++;
        if (position.translateByPixels(walkRadius, -walkRadius).isWalkable()) walkable++;
        if (position.translateByPixels(-walkRadius, -walkRadius).isWalkable()) walkable++;

        return walkable >= 3;
    }

    private static boolean areAllNearbyTilesWalkable(HasPosition position, AUnit unit) {
        int walkRadius = 32;

        return position.translateByPixels(-walkRadius, -walkRadius).isWalkable()
            && position.translateByPixels(walkRadius, walkRadius).isWalkable()
            && position.translateByPixels(walkRadius, -walkRadius).isWalkable()
            && position.translateByPixels(-walkRadius, -walkRadius).isWalkable();
    }

    private static boolean notTooCloseToGeysersOrMinerals(AUnit unit, HasPosition position) {
        if (unit.isAir()) return true;

        return Select.mineralsAndGeysers()
//            .inRadius(Math.max(1.2, unit.size() * 5), position)
            .inRadius(We.protoss() ? 5 : 1.3, position)
            .isEmpty();
    }

    private static boolean isNotTooCloseToMinerals(HasPosition position, AUnit unit) {
        return Select.mineralsAndGeysers().inRadius(unit.isNotLarge() ? 1 : 6, position).isEmpty();
    }
}