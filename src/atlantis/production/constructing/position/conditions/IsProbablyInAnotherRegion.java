package atlantis.production.constructing.position.conditions;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class IsProbablyInAnotherRegion {
    public static boolean looksToBeTooFar(AUnit builder, AUnitType building, APosition position, HasPosition nearTo) {
        if (building.isBase() || building.isCombatBuilding()) return false;

        double groundDistance = builder.position().groundDistanceTo(position);

        return groundDistance >= 30
            && builder.distTo(position) * 2 >= groundDistance
            && position.regionsMatch(nearTo);
    }
}