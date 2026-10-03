package atlantis.production.constructions.position.protoss;

import atlantis.game.A;
import atlantis.information.strategy.Strategy;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

/**
 * A Protoss pylon is power <b>and</b> an expansion: without one, nothing can be
 * built at a base. So this rule keeps the bot from committing to a base that is
 * far from its main before it is strong enough - more than 22 ground tiles from
 * the main while the bot has less than 40 supply and has not committed to
 * expanding.
 *
 * <p>This is a doctrine, not a safety net, and it has a real consequence worth
 * knowing: until one of the two conditions changes, <b>no</b> Protoss building
 * can be placed at such a base - the cannon that {@code ProtossSecureBaseWithCannons}
 * would want needs a pylon first, and the pylon is refused. Measured on the
 * acceptance world's natural (32.8 ground tiles from the main): with 4 supply
 * every candidate is rejected by this rule and the request fails, with 40 supply
 * the same world places the pylon 4.2 tiles from the natural. Both halves are
 * pinned in {@code RequestBuildingNearTest}, so a change to the rule has to be a
 * deliberate edit of those tests.</p>
 */
public class PylonTooFarFromBaseEarly {
    public static boolean isTooFar(AUnit builder, AUnitType building, APosition position) {
        if (!building.isPylon()) return false;
        if (A.supplyTotal() >= 40) return false;
        if (Strategy.get().isExpansion()) return false;

        return Select.mainOrAnyUnit().groundDist(position) > 22;
    }
}
