package atlantis.map.position;

import atlantis.map.ABaseLocation;
import atlantis.map.AChoke;
import atlantis.map.ARegionBoundary;
import atlantis.units.AUnit;
import atlantis.units.FoggedUnit;
import bwapi.Position;
import bwapi.Unit;
import jbweb.JBWEB;
import tests.unit.FakeFoggedUnit;
import tests.unit.FakeUnit;

public class PositionUtil {

    /**
     * Returns distance from one position to other in build tiles. One build tile equals to 32 pixels. Usage
     * of build tiles instead of pixels is preferable, because it's easier to imagine distances if one knows
     * building dimensions.
     */
    public static double distanceTo(Object object1, Object object2) {
        if (object1 == null || object2 == null) {
            System.err.println("object1");
            System.err.println(object1);
            System.err.println("object2");
            System.err.println(object2);
            throw new RuntimeException("distanceTo got null");
        }

        // === Convert object1 to position or unit ====================
        
        Position fromPosition = null;
        Unit fromUnit = null;

        if (object1 instanceof FakeUnit) {
            fromPosition = ((FakeUnit) object1).position();
        }
        else if (object1 instanceof FoggedUnit) {
            fromPosition = ((FoggedUnit) object1).position();
        }
        else if (object1 instanceof FakeFoggedUnit) {
            fromPosition = ((FakeFoggedUnit) object1).position();
        }
        else if (object1 instanceof AUnit) {
            fromUnit = ((AUnit) object1).u();
        }
        else if (object1 instanceof Unit) {
            fromUnit = (Unit) object1;
        }
        else if (object1 instanceof APosition) {
            fromPosition = (APosition) object1;
        }
        else if (object1 instanceof Position) {
            fromPosition = (Position) object1;
        }
        else if (object1 instanceof AChoke) {
            fromPosition = ((AChoke) object1).center();
        }
        else if (object1 instanceof ABaseLocation) {
            fromPosition = ((ABaseLocation) object1).position();
        }
        else if (object1 instanceof ARegionBoundary) {
            fromPosition = ((ARegionBoundary) object1).position();
        }

        if (fromPosition == null && fromUnit == null) {
            throw new RuntimeException("Invalid class for argument `from`: " + object1);
        }
        
        // === Convert object2 to position or unit ===================
        
        Position toPosition = null;
        Unit toUnit = null;
        
        if (object2 instanceof FakeUnit) {
            toPosition = ((FakeUnit) object2).position();
        }
        else if (object2 instanceof FoggedUnit) {
            toPosition = ((FoggedUnit) object2).position();
        }
        else if (object2 instanceof FakeFoggedUnit) {
            toPosition = ((FakeFoggedUnit) object2).position();
        }
        else if (object2 instanceof AUnit) {
            toUnit = ((AUnit) object2).u();
        }
        else if (object2 instanceof Unit) {
            toUnit = (Unit) object2;
        }
        else if (object2 instanceof APosition) {
            toPosition = (APosition) object2;
        }
        else if (object2 instanceof Position) {
            toPosition = (Position) object2;
        }
        else if (object2 instanceof AChoke) {
            toPosition = ((AChoke) object2).center();
        }
        else if (object2 instanceof ABaseLocation) {
            toPosition = ((ABaseLocation) object2).position();
        }
        else if (object2 instanceof ARegionBoundary) {
            toPosition = ((ARegionBoundary) object2).position();
        }

        if (toPosition == null && toUnit == null) {
            throw new RuntimeException("Invalid class for argument `to`: " + object2);
        }
        
        // =========================================================

        // From is UNIT
        if (fromUnit != null) {
            if (toUnit != null) {
                return fromUnit.getDistance(toUnit) / 32.0; // UNIT to UNIT distance
            }

            else {
                return fromUnit.getDistance(toPosition) / 32.0;
            }
        }

        // From is POSITION
        else {
            if (toPosition != null) {
                return fromPosition.getDistance(toPosition) / 32.0;
            }
            else {
                return fromPosition.getDistance(toUnit.getPosition()) / 32.0;
            }
        }

    }

    // =========================================================

    /**
     * Returns real ground distance to given point (not the air shortcut over impassable terrain).
     */
    public static double groundDistanceTo(Position from, Position to) {
//        return BWTA.getGroundDistance(new TilePosition(from.p()), new TilePosition(to.p())) / 32.0;
        return JBWEB.getGroundDistance(from, to) / 32.0;
    }
}
