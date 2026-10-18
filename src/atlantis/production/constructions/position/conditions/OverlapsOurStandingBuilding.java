package atlantis.production.constructions.position.conditions;

import atlantis.map.position.APosition;
import atlantis.production.constructions.PrepareConstructionSite;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

/**
 * True when the candidate tile overlaps one of OUR OWN standing
 * (completed) buildings of the same type.
 *
 * <p>The mirror of the proximity finish ({@code buildingFinishedAtSite}):
 * a site it would remove the same frame must not be picked in the first
 * place. Measured OpenBW: dynamic Gateway oid 72 requested tile [116,19]
 * 38 times, every one proximity-cancelled the same frame against the
 * standing Gateway at [114,18] (4x3 footprint covers [116,19]) - 38
 * BUILD rows, 38 CANCEL twins, zero progress until the spiral drifted
 * elsewhere. The pick-time guard saw only unfinished overlap, so the
 * covered tile passed every check on the way in.</p>
 *
 * <p>Same type only, like the finish: a different-type overlap dies
 * elsewhere (engine refusal at issue time) and stays out to keep this
 * guard narrow. Footprint overlap, not nearness: an adjacent packed
 * sibling (touching edges, disjoint footprints) stays legal. Reads the
 * same live unit list as the finish, so the two cannot disagree.</p>
 */
public class OverlapsOurStandingBuilding {

    public static boolean check(APosition position, AUnitType building) {
        if (position == null || building == null) return false;

        int width = building.getTilesWidth();
        int height = building.getTilesHeights();

        for (AUnit unit : Select.ourOfTypeWithUnfinished(building).list()) {
            if (unit == null || !unit.isAlive() || !unit.isCompleted()) continue;

            if (PrepareConstructionSite.footprintsOverlap(
                position.tx(), position.ty(), width, height,
                unit.tx(), unit.ty(),
                unit.type().getTilesWidth(), unit.type().getTilesHeights()
            )) return true;
        }

        return false;
    }
}
