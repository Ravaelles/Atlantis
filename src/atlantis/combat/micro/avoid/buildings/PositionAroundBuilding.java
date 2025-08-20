package atlantis.combat.micro.avoid.buildings;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.Vector;
import bwapi.Color;

public class PositionAroundBuilding {
    /**
     * Computes a point around `around` (e.g. Spore Colony), at a fixed radius,
     * starting from the direction of `from` (e.g. Corsair), rotated by angleInDegrees.
     * <p>
     * from / around provide .tx()/.ty() in tiles.
     */
    public static APosition around(HasPosition from, HasPosition around, double radiusMargin, double angleInDegrees) {
//        from = around.translateByTiles(0, -10);
//        from = around.translateByTiles(0, -10).translatePercentTowards(10, from);

        Vector vector = new Vector(from.tx() - around.tx(), from.ty() - around.ty());

        if (vector.x == 0 && vector.y == 0) {
            vector.x = 1;
            vector.y = 1;
        }

//        vector = vector.normalizeTo1();
////        vector = vector.rotate(Math.toRadians(angleInDegrees));
//        vector = vector.multiplyVector(radiusMargin);
//
//        around.translateByVector(vector).paintCircle(8, Color.Yellow);

//        vector = vector.rotate(A.rand(1, 100) / 100.0);
        vector = vector.rotate(A.rand(30, 60) / 100.0);
        vector = vector.normalizeTo(radiusMargin);

//        System.out.println("DIST = " + around.translateByTileVector(vector).distTo(around));
//        System.out.println("DIST = " + around.translateByVector(vector).distTo(from));
//        System.err.println("vector = " + vector);
        
        return around.translateByTileVector(vector);
//        return around.translateByVector(vector);

        // =========================================================

//        // Convert tiles → pixels
//        double bx = around.tx();
//        double by = around.ty();
//
//        double ux = from.tx();
//        double uy = from.ty();
////        // Convert tiles → pixels
////        double bx = around.tx() * 32.0;
////        double by = around.ty() * 32.0;
////
////        double ux = from.tx() * 32.0;
////        double uy = from.ty() * 32.0;
//
//        // Vector from building → unit (in pixels)
//        Vector direction = new Vector(ux - bx, uy - by);
//
//        double length = direction.length();
//        if (length < 0.001) {
//            // Failsafe: if unit is exactly on the Spore Colony
//            return new APosition((int) around.tx() + 2, (int) around.ty() + 0);
//        }
//
//        // Normalize to length 1
//        direction.normalizeTo1();
//
//        // Rotate by angleInDegrees
//        double angleRad = Math.toRadians(angleInDegrees);
//        Vector rotated = direction.rotate(angleRad);
//
//        // Scale to desired radius
//        rotated.normalizeTo(radiusMargin);
//
//        // Final target pixel position
//        double x3 = bx + rotated.x;
//        double y3 = by + rotated.y;
//
//        // Convert back to tiles
//        int tileX = (int) x3;
//        int tileY = (int) y3;
////        int tileX = (int) Math.round(x3 / 32.0);
////        int tileY = (int) Math.round(y3 / 32.0);
//
//        return new APosition(tileX, tileY);
    }

//    AUnit spore = Select.enemies(AUnitType.Zerg_Spore_Colony).first();
//        if (spore != null) {
//        spore.paintCircleFilled(2, Color.Green);
//
//        APosition from = spore.translateByTiles(0, 10);
//
//        APosition goTo1 = PositionAroundBuilding.around(from, spore, 10, 20);
//        goTo1.paintCircleFilled(20, Color.Green);
//
//        System.out.println(spore.distTo(goTo1) + " / " + from.distTo(goTo1));
//
//        APosition goTo2 = PositionAroundBuilding.around(from, spore, 10, 40);
//        goTo2.paintCircleFilled(20, Color.Green);
//    }
}
