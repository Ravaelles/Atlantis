package atlantis.production.constructions.position.conditions;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class TooCloseToMainBase {
    /**
     * Is this position too close to our main base for this building?
     *
     * <p>
     * <b>A base is exempt.</b> A Nexus is not a combat building, so the guard below
     * used to let it through and then reject it at distance 0 - "too close to the
     * main base" - because it <i>is</i> the main base. Found by
     * {@code PositionConditionsTest} on 2026-10-10, the day the module got direct
     * tests; its sister {@code TooCloseToBaseLocation} has always had the
     * {@code building.isBase()} guard, so this was an omission rather than a rule.
     * </p>
     */
    public static boolean isTooCloseToMainBase(AUnitType building, APosition position) {
        if (building.isCombatBuilding()) return false;
        if (building.isBase()) return false;

        HasPosition base = Select.main();

//        APainter.paintCircle(position, 10, Color.Green);
        if (base != null) {
            double minDistFromBase = minDistFromBase(building);

            if (We.terran()) base = base.translateByTiles(2, 0);

//            System.err.println("distFromBase = " + base.distTo(position) + " / " + minDistFromBase);
            double dist = base.distTo(position);
            if (dist <= minDistFromBase) {
                return failed("Too close to main base (" + (int) dist + ")");
            }
        }

        return false;
    }

    private static double minDistFromBase(AUnitType building) {
        if (We.protoss()) {
            if (building.isGateway()) return 5;

            return building.isPylon() ? (A.supplyTotal() <= 16 ? 5 : 4) : 5;
        }

        else if (We.terran()) {
            if (building.isCombatBuilding()) return 0;

            return building.isSupplyDepot() ? 8 : 4;
        }

        return 2;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;

        return true;
    }
}
