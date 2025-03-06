package atlantis.combat.squad.positioning.formations.moon;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

import java.util.HashMap;
import java.util.Map;

public class MoonUnitPositionsCalculator {
    public static Map<AUnit, APosition> calculateUnitPositions(
        Selection units, HasPosition center, AUnit leader, double radius, double separation
    ) {
        Map<AUnit, APosition> positions = new HashMap<>();

        // Compute the angle towards the enemy position
        double angleToCenter = Math.atan2(center.ty() - units.center().ty(), center.tx() - units.center().tx());

        // Total angle span depends on unit count and their sizes
        double angleSpan = calculateTotalAngleSpan(units, radius, separation);
        double angleStart = angleToCenter - Math.toRadians(angleSpan / 2); // Leftmost point of the crescent
        double angleStep = Math.toRadians(angleSpan / Math.max(1, units.size() - 1));

        int i = 0;
        for (AUnit unit : units.list()) {
            double angle = angleStart + i * angleStep;
            double offsetX = Math.cos(angle) * radius;
            double offsetY = Math.sin(angle) * radius;

            APosition position = APosition.create((int) (center.tx() - offsetX), (int) (center.ty() - offsetY));
            position = validatePosition(position, unit, leader);
//            System.err.println("position = " + position);

            if (isPositionOkay(position, unit)) {
                positions.put(unit, position);
            }
//            else {
//                A.errPrintln("Position is not okay: " + position + " / " + position.isWalkable() + " / " + position.regionsMatch(unit));
//            }

            i++;
        }
        return positions;
    }

    private static double calculateTotalAngleSpan(Selection units, double radius, double separation) {
        double totalWidth = 0.0;

        // Calculate total width of all units including safety margins
        for (AUnit unit : units.list()) {
            totalWidth += (unit.size() + separation);
        }

        // Convert width to angle span (arc length = radius * angle in radians)
        return Math.toDegrees(totalWidth / radius);
    }

    private static APosition validatePosition(APosition position, AUnit unit, AUnit leader) {
        if (position == null) return null;

        if (!position.isWalkable()) {
            position = position.makeWalkable(1, unit.position().region());
        }

        if (position == null || !position.isWalkable()) {
            position = leader.position();
        }

        return position;
    }

    private static boolean isPositionOkay(APosition position, AUnit unit) {
        return position != null
            && position.isWalkable()
            && position.regionsMatchOrClose(unit, 8);
    }
}