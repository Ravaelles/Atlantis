package atlantis.production.constructions.protoss;

import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.DefineConstructionForNewUnit;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.workers.gather.GatherResources;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

public class ProtossWarping {

    /**
     * Detect a newly warped Protoss building and finish the construction that commissioned it.
     *
     * <p>
     * This runs from {@code OnOurUnitCreated}, so the building EXISTS - and that is the moment
     * the construction is over. Two defects were fixed here (NEXT.md #53, {@code BUGS.md}):
     * </p>
     *
     * <ol>
     *   <li><b>The construction was only marked {@code IN_PROGRESS}, never {@code FINISHED}.</b>
     *       {@code setStatus(FINISHED)} is the only status that removes it, so an
     *       {@code IN_PROGRESS} construction stayed in {@code ConstructionRequests} forever:
     *       {@code countPendingOfType} kept counting a building that already stood, and
     *       {@code BuilderManager.isBuilder} kept treating a Protoss construction as a reason to
     *       hold its worker.</li>
     *   <li><b>A building that appeared away from its reserved tile was not matched at all.</b>
     *       {@code DefineConstructionForNewUnit} identifies the construction positionally (2.5
     *       tiles), and the site can move ({@code RefreshConstructionPosition}) or the engine can
     *       snap a footprint. Measured in {@code out/openbw/bot.log}: 3534 frames of
     *       {@code construction=Gateway status=NOT_STARTED} on a Probe one tile from a Gateway
     *       that went up somewhere else, with {@code isConstructing=false} the whole time.</li>
     * </ol>
     */
    public static void updateNewBuildingJustWarped(AUnit newBuilding) {
        if (!We.protoss() || !newBuilding.type().isABuilding()) return;

        Construction construction = DefineConstructionForNewUnit.defineConstruction(newBuilding);
        if (construction == null) {
            // The building appeared somewhere the positional lookup could not reach. A
            // construction of this type was still waiting for it, so it is almost certainly
            // that one - and leaving it in the queue means its assigned worker is held
            // forever at a site that will never have a building (NEXT.md #53, measured:
            // 3534 frames of a Probe whose Gateway went up away from its reservation).
            // Adopt the oldest waiting one; `defineConstruction` still gets first refusal,
            // so the normal, position-matching case is untouched.
            int waiting = ConstructionRequests.notFinishedOfType(newBuilding.type()).size();
            construction = oldestWaitingConstructionOf(newBuilding.type());
            if (construction != null) {
                EventLog.key(LogKey.ADOPTED_CONSTRUCTION)
                    .param("type", newBuilding.type().name())
                    .param("warpTile", "[" + newBuilding.position().tx() + "," + newBuilding.position().ty() + "]")
                    .param("adopted", construction.id())
                    .param("adoptedTile", construction.buildPosition() == null ? "-"
                        : "[" + construction.buildPosition().tx() + "," + construction.buildPosition().ty() + "]")
                    .param("candidates", waiting)
                    .log(A.now(), A.minSec());
            }
        }

        if (construction == null) {
            System.err.println("CANNOT define construction for " + newBuilding);
            return;
        }

        // FINISHED, not IN_PROGRESS, and the difference is the whole defect: this method runs
        // from `OnOurUnitCreated` for a building that now EXISTS, so its construction is done.
        // `IN_PROGRESS` leaves it in `ConstructionRequests` - `notFinishedOfType` still counts
        // it, `ConstructionStatusChanger` keeps iterating it, and (before the builder was
        // released) its worker was held for the rest of the game. `FINISHED` is the one status
        // that removes it, and `setStatus` already performs that removal.
        construction.setStatus(ConstructionOrderStatus.FINISHED);
        construction.setBuildingUnit(newBuilding);

        // Construction
        newBuilding.setConstruction(construction);

        // === Validate ===========================================

        if (!newBuilding.type().equals(construction.buildingType())) {
            ErrorLog.printMaxOncePerMinute("@@@@@ Building type mismatch for " + newBuilding + " / " + construction);
        }

        // ========================================================

        // Production order
        if (newBuilding.productionOrder() == null) {
            newBuilding.setProductionOrder(construction.productionOrder());
        }

        if (construction.builder() != null) (new GatherResources(construction.builder())).forceHandle();
        construction.setBuilder(null);
    }

    /**
     * The construction that was waiting for a building of this type and did not get one.
     *
     * <p>
     * Only used when the positional lookup fails, i.e. the finished building is outside the
     * 2.5-tile radius of every reservation. The oldest unfinished one is the best available
     * answer: the construction list is ordered by creation, so the one that has waited
     * longest is the one whose assigned worker has been claimed longest - which is exactly
     * the worker this adoption exists to release.
     * </p>
     */
    private static Construction oldestWaitingConstructionOf(AUnitType type) {
        Construction oldest = null;

        for (Construction construction : ConstructionRequests.notFinishedOfType(type)) {
            if (oldest == null || construction.id() < oldest.id()) oldest = construction;
        }

        return oldest;
    }
}
