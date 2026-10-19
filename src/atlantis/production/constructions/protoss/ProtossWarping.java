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
            // Adopt the nearest waiting one (owner 2026-10-19); `defineConstruction`
            // still gets first refusal, so the normal, position-matching case is untouched.
            int waiting = ConstructionRequests.notFinishedOfType(newBuilding.type()).size();
            construction = nearestWaitingConstructionOf(newBuilding.type(), newBuilding);
            if (construction != null) {
                EventLog.key(LogKey.ADOPTED_CONSTRUCTION)
                    .param("type", newBuilding.type().name())
                    .param("warpTile", "[" + newBuilding.position().tx() + "," + newBuilding.position().ty() + "]")
                    .param("adopted", construction.id())
                    .param("oid", construction.productionOrder() == null ? null : construction.productionOrder().id())
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

        // A warping (uncompleted) building keeps its construction listed
        // as IN_PROGRESS: the painters iterate the list, so the warp stays
        // visible until it completes (owner: warping buildings kept
        // vanishing from painting). Completion finishes it - see
        // OnOurNewUnitCompleted - so IN_PROGRESS can no longer leak: only
        // completed buildings leave the list, never warping ones.
        if (newBuilding.isCompleted()) {
            construction.setStatus(ConstructionOrderStatus.FINISHED);
        }
        else {
            construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
        }
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
     * 2.5-tile radius of every reservation. The nearest unfinished one is the best available
     * answer: builders act locally, so an offset warp most likely belongs to the closest
     * site - attributing it to a far stale row instead re-fires demand that already stands
     * (the duplicate-pylon class). Constructions without a site sort last; if none has a
     * site the oldest is kept as the tiebreak, so a candidate always exists when the list
     * is non-empty.
     * </p>
     */
    private static Construction nearestWaitingConstructionOf(AUnitType type, AUnit warped) {
        Construction nearest = null;
        Construction oldestSiteless = null;
        double best = Double.MAX_VALUE;

        for (Construction construction : ConstructionRequests.notFinishedOfType(type)) {
            if (oldestSiteless == null || construction.id() < oldestSiteless.id()) {
                if (construction.buildPosition() == null) oldestSiteless = construction;
            }
            if (construction.buildPosition() == null) continue;
            double dist = construction.buildPosition().distTo(warped);
            if (dist < best) {
                best = dist;
                nearest = construction;
            }
        }

        return nearest != null ? nearest : oldestSiteless;
    }
}
