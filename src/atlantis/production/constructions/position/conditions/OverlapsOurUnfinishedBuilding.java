package atlantis.production.constructions.position.conditions;

import atlantis.map.position.APosition;
import atlantis.production.constructions.PrepareConstructionSite;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

/**
 * True when the candidate tile overlaps one of OUR OWN unfinished
 * buildings.
 *
 * <p>Measured OpenBW `214926`: Gateway [94,111] was picked on top of our
 * own warping [92,111] (4x3 footprints share two columns). Every other
 * guard missed it: the warping building's construction is already gone
 * ({@code ProtossWarping} finishes on warp start, so no reservation
 * names the tiles anymore) and the engine occupancy answer did not
 * refuse it. The builder walked there, the engine refused at issue
 * time, the site cancelled, and two orders re-requested the same need
 * twice (three Gateways for a two-Gateway opening).</p>
 *
 * <p>Live unit list, both engines - the same source the status changer
 * trusts over engine fields. Only our unfinished buildings count: a
 * mining worker on the footprint is transient (that false positive
 * died 2026-10-10), finished buildings are the engine's answer, enemy
 * buildings never overlap our base sites in practice and stay out to
 * keep this guard narrow. Standing grazers (a completed building
 * overlapping the candidate) pass here BY PINNED DESIGN
 * ({@code WarpingBuildingBlocksOverlapTest.finishedSiblingNextDoorStaysLegal})
 * and die at proximity-finish instead - the two guards cover disjoint
 * worlds by construction (NEXT #150 branch, #151 no-merge), so a
 * same-frame REQUESTED-then-cancel against a standing building is
 * the finish path working, not this guard missing.</p>
 */
public class OverlapsOurUnfinishedBuilding {

    public static boolean check(APosition position, AUnitType building) {
        if (position == null || building == null) return false;

        int width = building.getTilesWidth();
        int height = building.getTilesHeights();

        for (AUnit unit : Select.ourWithUnfinished().list()) {
            if (unit == null || !unit.isAlive() || !unit.isABuilding() || unit.isCompleted()) continue;

            if (PrepareConstructionSite.footprintsOverlap(
                position.tx(), position.ty(), width, height,
                unit.tx(), unit.ty(),
                unit.type().getTilesWidth(), unit.type().getTilesHeights()
            )) return true;
        }

        return false;
    }
}
