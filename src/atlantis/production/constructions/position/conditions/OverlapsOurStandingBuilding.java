package atlantis.production.constructions.position.conditions;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.BuildingTilesAreOccupied;

/**
 * True when the candidate tile overlaps any standing (completed)
 * building's footprint, regardless of type.
 *
 * <p>The mirror of the proximity finish ({@code buildingFinishedAtSite}):
 * a site it would remove the same frame must not be picked in the first
 * place. Measured OpenBW: dynamic Gateway oid 72 requested tile [116,19]
 * 38 times, every one proximity-cancelled the same frame against the
 * standing Gateway at [114,18] (4x3 footprint covers [116,19]) - 38
 * BUILD rows, 38 CANCEL twins, zero progress until the spiral drifted
 * elsewhere.</p>
 *
 * <p>Any type, not same-type only: the old same-type-only rule assumed a
 * different-type overlap dies at issue time, but the refresh cancels it
 * first - so the spiral kept offering tiles the refresh kept killing
 * (measured live: CyberneticsC at [91,111], cancel/re-request loop with
 * an order re-minted every cycle while the bank hoarded). The pick-time
 * guard and the adopt-time guard read the same predicate now
 * ({@link BuildingTilesAreOccupied#checkBuildingsOnly}), so they cannot
 * disagree again. Footprint overlap, not nearness: an adjacent packed
 * sibling (touching edges, disjoint footprints) stays legal. Unfinished
 * warps never block (B-41 adopts them on position).</p>
 */
public class OverlapsOurStandingBuilding {

    public static boolean check(APosition position, AUnitType building) {
        return BuildingTilesAreOccupied.checkBuildingsOnly(position, building);
    }
}
