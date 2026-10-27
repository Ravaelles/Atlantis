package atlantis.map.position;

import atlantis.Atlantis;
import atlantis.map.AMap;
import atlantis.map.GroundDistance;
import atlantis.map.base.ABaseLocation;
import atlantis.map.choke.AChoke;
import atlantis.map.region.ARegionBoundary;
import atlantis.units.AUnit;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.fogged.FoggedUnit;
import atlantis.util.log.ErrorLog;
import bwapi.Position;
import bwapi.Unit;

public class PositionUtil {

    public static final int DIST_RETURNED_FOR_FOGGED_UNITS_WITHOUT_POSITION = 989;
    /**
     * Owner rule (Steamhammer loss: two Dragoons died shooting nothing,
     * glued on a Gateway): an escape step never ends within this of a
     * building's edge. Maneuver room is survival - a step that hugs a
     * wall slides, re-issues and never fires.
     */
    public static final double BUILDING_CLEARANCE_TILES = 1.5;

    // =========================================================

    /**
     * Returns distance from one position to other in build tiles. One build tile equals to 32 pixels. Usage
     * of build tiles instead of pixels is preferable, because it's easier to imagine distances if one knows
     * building dimensions.
     */
    public static double distanceTo(Object object1, Object object2) {
//        if (true) throw new RuntimeException("PositionUtil.distanceTo was used");

        if (object1 == null || object2 == null) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(
                "object1\n" +
                    object1 + "\n" +
                    "object2\n" +
                    object2 + "\n" +
                    "distanceTo got null"
            );

            return 999;
        }

        // === Convert object1 to position or unit ====================

        Position fromPosition = null;
        Unit fromUnit = null;
        int bonus = 0;

//        System.err.println("object2 = " + object2 + " / " + (object2 instanceof AChoke));

        if (object1 instanceof FoggedUnit) {
            APosition foggedPosition = ((FoggedUnit) object1).position();
            if (foggedPosition == null) {
                return DIST_RETURNED_FOR_FOGGED_UNITS_WITHOUT_POSITION;
            }

            fromPosition = foggedPosition.p();
            if (fromPosition == null) {
                return DIST_RETURNED_FOR_FOGGED_UNITS_WITHOUT_POSITION;
            }
        }
        else if (object1 instanceof AbstractFoggedUnit) {
            fromPosition = ((AbstractFoggedUnit) object1).position().p();
            if (fromPosition == null) {
                return DIST_RETURNED_FOR_FOGGED_UNITS_WITHOUT_POSITION;
            }
        }
        else if (object1 instanceof AUnit) {
            AUnit unit = (AUnit) object1;

            // A unit with no engine object behind it - a double the harness put in
            // the world - has no bwapi Unit to measure from, but it does know
            // where it is. Asking the unit covers both without this method
            // knowing which kind it got.
            if (unit.u() != null) {
                fromUnit = unit.u();
            }
            else {
                fromPosition = unit.position().p();
            }
        }
        else if (object1 instanceof Unit) {
            fromUnit = (Unit) object1;
        }
        else if (object1 instanceof AChoke) {
            fromPosition = ((AChoke) object1).center().p();
            bonus = -((AChoke) object1).width();
        }
        else if (object1 instanceof APosition) {
            fromPosition = ((APosition) object1).p();
        }
//        else if (object1 instanceof HasPosition) {
//            fromPosition = ((HasPosition) object1).position().p();
//        }
        else if (object1 instanceof Position) {
            fromPosition = (Position) object1;
        }
        else if (object1 instanceof ABaseLocation) {
            fromPosition = ((ABaseLocation) object1).position().p();
        }
        else if (object1 instanceof ARegionBoundary) {
            fromPosition = ((ARegionBoundary) object1).position().p();
        }

        if (fromPosition == null && fromUnit == null) {
            throw new RuntimeException("Invalid class for argument `from`: " + object1);
        }

        // === Convert object2 to position or unit ===================

        Position toPosition = null;
        Unit toUnit = null;

        if (object2 instanceof FoggedUnit) {
            APosition pos = ((FoggedUnit) object2).position();
            if (pos == null) {
                return DIST_RETURNED_FOR_FOGGED_UNITS_WITHOUT_POSITION;
            }

            toPosition = pos.p();
            if (toPosition == null) {
                return DIST_RETURNED_FOR_FOGGED_UNITS_WITHOUT_POSITION;
            }
        }
        else if (object2 instanceof AbstractFoggedUnit) {
            toPosition = ((AbstractFoggedUnit) object2).position().p();
        }
        else if (object2 instanceof AUnit) {
            AUnit unit = (AUnit) object2;

            if (unit.u() != null) {
                toUnit = unit.u();
            }
            else {
                toPosition = unit.position().p();
            }
        }
        else if (object2 instanceof Unit) {
            toUnit = (Unit) object2;
        }
        else if (object2 instanceof AChoke) {
            toPosition = ((AChoke) object2).center().p();
            bonus = -((AChoke) object2).width();
        }
        else if (object2 instanceof APosition) {
            toPosition = ((APosition) object2).p();
        }
//        else if (object1 instanceof HasPosition) {
//            toPosition = ((HasPosition) object2).position().p();
//        }
        else if (object2 instanceof Position) {
            toPosition = (Position) object2;
        }
        else if (object2 instanceof ABaseLocation) {
            toPosition = ((ABaseLocation) object2).position().p();
        }
        else if (object2 instanceof ARegionBoundary) {
            toPosition = ((ARegionBoundary) object2).position().p();
        }

//        System.err.println("fromUnit = " + fromUnit);
//        System.err.println("fromPosition = " + fromPosition);
//        System.err.println("toPosition = " + toPosition);

        if (toPosition == null && toUnit == null) {
//            System.err.println("Object: " + object2);
//            System.err.println("Class:  " + object2 != null ? object2.getClass() : "- null -");
//            throw new RuntimeException("Invalid class for argument `to`: " + object2);
//            AConsole.printStackTrace("Invalid class for argument `to`: " + object2);
            System.err.println("Invalid class for argument `to`: " + object2);

            return 999;
        }

        // =========================================================
        // Make sure to always start calculating distance from unit, to include unit-to-unit distance

        if (fromUnit == null) {

        }

        // =========================================================

        // From is UNIT
        if (fromUnit != null) {
            if (toUnit != null) {
                return bonus + fromUnit.getDistance(toUnit) / 32.0; // UNIT to UNIT distance
            }

            else {
                return bonus + fromUnit.getDistance(toPosition) / 32.0;
            }
        }

        // From is POSITION
        else {
            if (toPosition != null) {
                return bonus + fromPosition.getDistance(toPosition) / 32.0;
            }
            else {
                return bonus + fromPosition.getDistance(toUnit.getPosition()) / 32.0;
            }
        }

    }

    /**
     * True when this destination would glue the unit onto a building:
     * within clearance of any building blocker's edge. Minerals and
     * geysers are walk-around cover with only the soft scoring penalty
     * (BWAPI flags mineral fields as buildings, and units do slip
     * between minerals - a hard veto there would wall off every
     * mineral line). Edge-aware: the blocker position is the footprint
     * center, so the footprint half-extent rides along (a Gateway
     * rejects wider than a Pylon). Null-safe: no dest, no blockers,
     * or an unreadable blocker never vetoes - the caller owns those
     * exits.
     */
    public static boolean violatesBuildingClearance(
        HasPosition dest, java.util.List<? extends HasPosition> blockers
    ) {
        if (dest == null || blockers == null || blockers.isEmpty()) return false;

        for (HasPosition blocker : blockers) {
            if (blocker == null) continue;
            double halfExtent = 0;
            try {
                if (!(blocker instanceof AUnit)) continue;
                AUnit building = (AUnit) blocker;
                if (!building.isABuilding()
                    || building.type().isMineralField()
                    || building.type().isGeyser()) {
                    continue;
                }
                halfExtent = building.type().bodyRadiusPx() / 32.0;
            } catch (Exception | Error ignored) {
                continue;
            }
            double edgeDist;
            try {
                edgeDist = dest.distTo(blocker) - halfExtent;
            } catch (Exception | Error ignored) {
                continue;
            }
            if (edgeDist < BUILDING_CLEARANCE_TILES) return true;
        }

        return false;
    }

    /**
     * Returns real ground distance to given point (not the air shortcut over impassable terrain).
     */
    public static double groundDistanceTo(HasPosition from, HasPosition to) {
        if (from == null || from.position() == null) return 999;
        if (to == null || to.position() == null) return 999;

        return groundDistanceTo(from.position().p(), to.position().p());
    }

    public static double groundDistanceTo(Position from, Position to) {
//        return BWTA.getGroundDistance(new TilePosition(from.p()), new TilePosition(to.p())) / 32.0;
//        return JBWEB.getGroundDistance(from, to) / 32.0;
        try {
            // No area graph, no ground distance: without BWEM the only honest
            // answer is Euclidean (the old code reached the same fallback
            // because the static hub game was null there).
            if (Atlantis.game() == null || AMap.bwem() == null) return PositionUtil.distanceTo(from, to);

            return GroundDistance.between(Atlantis.game(), AMap.bwem(), from, to) / 32.0;
        } catch (Exception e) {
//            System.err.println("from = " + from);
//            System.err.println("to = " + to);
//            e.printStackTrace();
            return PositionUtil.distanceTo(from, to);
        }
    }
}
