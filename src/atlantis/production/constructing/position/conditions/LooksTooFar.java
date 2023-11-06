package atlantis.production.constructing.position.conditions;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class LooksTooFar {
    public static boolean looksTooFar(AUnit builder, AUnitType building, APosition position) {
        if (building.isBase() || building.isCombatBuilding()) return false;

        double groundDistance = builder.position().groundDistanceTo(position);

        return (groundDistance >= 30 && builder.distTo(position) * 2 >= groundDistance);
    }
}