package atlantis.production.constructing.position.conditions;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class ForbiddenForSupplyDepot {
    public static boolean isForbidden(AUnit builder, AUnitType building, APosition position) {
        if (!We.terran() || !building.isSupplyDepot()) return false;

        return position.tx() % 2 != 1 || position.ty() % 2 != 1;
    }
}
