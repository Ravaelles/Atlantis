package atlantis.map.position;

import atlantis.map.base.ABaseLocation;
import atlantis.map.choke.AChoke;
import atlantis.map.region.ARegionBoundary;
import atlantis.units.AUnit;
import atlantis.units.fogged.FakeFoggedUnit;
import atlantis.units.fogged.FoggedUnit;
import atlantis.util.log.ErrorLog;
import bwapi.Position;
import bwapi.Unit;
import jbweb.JBWEB;
import tests.fakes.FakeUnit;

public class PositionUtil {

    public static final int DIST_RETURNED_FOR_FOGGED_UNITS_WITHOUT_POSITION = 989;

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

        if (object1 instanceof FakeUnit) {
            fromPosition = ((FakeUnit) object1).position().p();
        }
        else if (object1 instanceof FoggedUnit) {
            fromPosition = ((FoggedUnit) object1).position().p();
            if (fromPosition == null) {
                return DIST_RETURNED_FOR_FOGGED_UNITS_WITHOUT_POSITION;
            }
        }
        else if (object1 instanceof FakeFoggedUnit) {
            fromPosition = ((FakeFoggedUnit) object1).position().p();
            if (fromPosition == null) {
                return DIST_RETURNED_FOR_FOGGED_UNITS_WITHOUT_POSITION;
            }
        }
        else if (object1 instanceof AUnit) {
            fromUnit = ((AUnit) object1).u();
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
        else if (object1 instanceof FoggedUnit) {
            fromPosition = ((FoggedUnit) object1).position().p();
        }

        if (fromPosition == null && fromUnit == null) {
            throw new RuntimeException("Invalid class for argument `from`: " + object1);
        }

        // === Convert object2 to position or unit ===================

        Position toPosition = null;
        Unit toUnit = null;

        if (object2 instanceof FakeUnit) {
            toPosition = ((FakeUnit) object2).position().p();
        }
        else if (object2 instanceof FoggedUnit) {
            toPosition = ((FoggedUnit) object2).position().p();
        }
        else if (object2 instanceof FakeFoggedUnit) {
            toPosition = ((FakeFoggedUnit) object2).position().p();
        }
        else if (object2 instanceof AUnit) {
            toUnit = ((AUnit) object2).u();
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
//            A.printStackTrace("Invalid class for argument `to`: " + object2);
            System.err.println("Invalid class for argument `to`: " + object2);
            return 999;
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

    // =========================================================

    /**
     * Returns real ground distance to given point (not the air shortcut over impassable terrain).
     */
    public static double groundDistanceTo(HasPosition from, HasPosition to) {
        return groundDistanceTo(from.position().p(), to.position().p());
    }

    public static double groundDistanceTo(Position from, Position to) {
//        return BWTA.getGroundDistance(new TilePosition(from.p()), new TilePosition(to.p())) / 32.0;
//        return JBWEB.getGroundDistance(from, to) / 32.0;
        try {
            return JBWEB.getGroundDistance(from, to) / 32.0;
        } catch (Exception e) {
//            System.err.println("from = " + from);
//            System.err.println("to = " + to);
//            e.printStackTrace();
            return PositionUtil.distanceTo(from, to);
        }
    }
}
