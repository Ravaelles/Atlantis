package atlantis.production.constructions.position;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.protoss.ProtossPositionFinder;
import atlantis.production.constructions.position.terran.TerranPositionFinder;
import atlantis.production.constructions.position.zerg.ZergPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class APositionFinder {
    public static Cache<APosition> cache = new Cache<>();

    // =========================================================

    public static void clearCache() {
        cache.clear();
    }

    // =========================================================

    /**
     * Returns standard build position for building near given position.
     */
    public static APosition findStandardPosition(AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
        return cache.get(
            "findStandardPosition:" + building + "," + nearTo + "," + builder + "," + A.digit(maxDistance),
            building.isBase() || building.isGasBuilding() ? 131 : 71,
            () -> {
                assert maxDistance >= 0 : "Negative maxDistance = " + maxDistance;
                assert nearTo != null : "Null nearTo!";

                // ===========================================================
                // = Handle standard building position according to the race =
                // = as every race uses completely different approach        =
                // ===========================================================

                // Terran
                if (We.terran()) {
                    return TerranPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
                }

                // Protoss
                else if (We.protoss()) {
                    return ProtossPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
                }

                // Zerg
                else if (We.zerg()) {
                    return ZergPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
                }

                else {
                    // Unreachable in any working game: the race is always one of
                    // the three above. It used to be System.exit(-1) here, which
                    // let a leaf position finder kill the whole JVM; an
                    // exception reports instead and lets the composition root
                    // decide. See _AI/BUGS.md B-8.
                    throw new IllegalStateException("Invalid race: " + AGame.playerUs().getRace());
                }
            }
        );
    }

}
