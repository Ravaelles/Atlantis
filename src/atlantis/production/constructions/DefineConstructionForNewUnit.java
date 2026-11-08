package atlantis.production.constructions;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;

public class DefineConstructionForNewUnit {
    /**
     * The construction this newly created building belongs to - i.e. one that was waiting for
     * exactly this building, at exactly this spot.
     *
     * <p>
     * <b>This used to search only NOT_STARTED constructions, and that was wrong for the case
     * it exists to handle.</b> Measured 2026-10-11, owner's report: a wall of
     * </p>
     *
     * <pre>
     *   !!! No not started constructions for #120 Pylon @[114,16] / 1
     *   CANNOT define construction for #120 Pylon @[114,16]
     *   No construction for #120 Pylon @[114,16]
     * </pre>
     *
     * <p>
     * for every building the bot produced. The building had just been CREATED, so its
     * construction was already marked {@code IN_PROGRESS} - by {@code ConstructionStatusChanger}
     * and by {@code OnOurUnitCreated} itself - and a search restricted to {@code NOT_STARTED}
     * could therefore never find it. The lookup and the state it looked in were in direct
     * contradiction.
     * </p>
     *
     * <p>
     * It went unnoticed because the status used to never advance at all (the {@code buildUnit()}
     * defect fixed in {@code NEXT.md} #53): with everything stuck on {@code NOT_STARTED} the
     * search accidentally matched. Fixing the status exposed this one - which is the usual shape
     * of a two-bug stack, and the reason to fix the second properly rather than restore the
     * first.
     * </p>
     *
     * <p>
     * <b>The identity asked for now is positional</b>, which is what actually distinguishes the
     * construction: a building of this type, unfinished, standing within a couple of tiles of the
     * tile the construction reserved. Status is not part of the question - a construction that has
     * started is MORE likely to be the one whose building just appeared, not less.
     * </p>
     */
    public static Construction defineConstruction(AUnit newBuilding) {
        if (newBuilding == null) return null;

        // `notFinishedOfType` (= not FINISHED) rather than `notStartedOfType`: the building just
        // appeared, so its construction is IN_PROGRESS, and a search that excluded that state
        // could never find the one it was looking for.
        ArrayList<Construction> candidates = ConstructionRequests.notFinishedOfType(newBuilding.type());

        if (candidates.isEmpty() && !newBuilding.type().isAddon()) {
            ErrorLog.printMaxOncePerMinute(
                "!!! No unfinished construction for " + newBuilding
                    + " / pending: " + ConstructionRequests.countPendingOfType(newBuilding.type())
            );

            return null;
        }

        int nearest = -1;
        Construction nearestConstruction = null;

        for (Construction construction : candidates) {
            APosition position = construction.buildPosition();
            if (position == null) continue;

            double distance = position.distTo(newBuilding);

            // Only a construction whose site is where this building now stands can be its
            // construction. Without this bound, a second Pylon being built elsewhere would be
            // claimed by whichever building appeared first - the duplicate-worker problem
            // again, from the other side.
            if (distance > 2.5) continue;

            if (nearestConstruction == null || distance < nearest) {
                nearestConstruction = construction;
                nearest = (int) distance;
            }
        }

        if (nearestConstruction == null && !newBuilding.type().isAddon()) {
            // Orphan-warp fallback (owner 2026-11-09, live GAME_CDE27319):
            // a Citadel warped 4+ tiles from its row's tile (resite
            // between order and warp, engine nudge) and the 2.5 bound
            // above refused the only match - the row sat unitless while
            // its building stood unwatched. When exactly one unfinished
            // row of this type exists AND it has no building linked yet,
            // attribution is unambiguous (nothing else to misattribute
            // to), so adopt it regardless of distance instead of
            // orphaning the warp. Two same-type warps in flight keep the
            // tight bound - that is the duplicate-worker case it owns.
            Construction lone = loneUnitlessRow(candidates);
            if (lone != null) {
                atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.ADOPTED_CONSTRUCTION)
                    .param("type", newBuilding.type().name())
                    .param("result", "adopted-far")
                    .log(atlantis.game.A.now(), atlantis.game.A.minSec());

                return lone;
            }

            ErrorLog.printMaxOncePerMinute(
                "!!! No construction within reach of " + newBuilding
                    + " (" + candidates.size() + " unfinished of this type)"
            );
        }

        return nearestConstruction;
    }

    /**
     * The unambiguous orphan: exactly one unfinished row of this type
     * and it tracks no building yet. Null otherwise - with two rows
     * (or a row that already owns a warp) distance decides, as before.
     */
    private static Construction loneUnitlessRow(ArrayList<Construction> candidates) {
        try {
            if (candidates == null || candidates.size() != 1) return null;

            Construction row = candidates.get(0);

            return row != null && row.buildingUnit() == null ? row : null;
        } catch (Exception | Error ignored) {
            return null;
        }
    }
}
