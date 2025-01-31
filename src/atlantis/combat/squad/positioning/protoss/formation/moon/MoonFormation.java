package atlantis.combat.squad.positioning.protoss.formation.moon;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

import java.util.HashMap;
import java.util.Map;

public class MoonFormation {
    public static Map<AUnit, APosition> unitPositions(
        Selection units, HasPosition center, double radius, double separation
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

            if (!position.isWalkable()) {
                position = findNearestWalkable(position);
            }

            positions.put(unit, position);
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

    private static APosition findNearestWalkable(APosition position) {
        return position.makeWalkable(2);
    }
}