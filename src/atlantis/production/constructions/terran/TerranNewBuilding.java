package atlantis.production.constructions.terran;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.DefineConstructionForNewUnit;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.workers.gather.GatherResources;
import atlantis.util.We;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

public class TerranNewBuilding {
    public static void updateNewBuilding(AUnit newBuilding) {
        if (!We.terran() || !newBuilding.isABuilding()) return;

        linkBuildingToConstruction(newBuilding);
    }

    /**
     * Test seam (the race guard reads the hardcoded
     * {@code Main.ourRace()}, so no stub world can be Terran): the linking
     * rule itself, without the gate.
     */
    public static void linkBuildingToConstruction(AUnit newBuilding) {
        Construction construction = DefineConstructionForNewUnit.defineConstruction(newBuilding);

        if (construction == null) {
            // Same orphan shape as Protoss #53: the building went up away from
            // its reservation, so adopt the oldest waiting one instead of
            // leaking it. Unlike Protoss the building has just STARTED (the
            // SCV is still constructing it), so it stays IN_PROGRESS with its
            // builder retained - completion belongs to ConstructionStatusChanger.
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

        if (construction == null) return;

        construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
        construction.setBuildingUnit(newBuilding);
        newBuilding.setConstruction(construction);
    }

    /**
     * The construction that was waiting for a building of this type and did
     * not get one. Mirrors the Protoss fallback except for the outcome: the
     * Terran building just started, so adoption keeps IN_PROGRESS and the
     * live builder - FINISHED and release would be the Protoss shape and are
     * wrong here.
     */
    private static Construction oldestWaitingConstructionOf(AUnitType type) {
        Construction oldest = null;

        for (Construction construction : ConstructionRequests.notFinishedOfType(type)) {
            if (oldest == null || construction.id() < oldest.id()) oldest = construction;
        }

        return oldest;
    }
}
