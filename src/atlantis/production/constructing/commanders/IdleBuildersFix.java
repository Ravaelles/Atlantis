package atlantis.production.constructing.commanders;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.We;

import java.util.List;

public class IdleBuildersFix extends Commander {
    @Override
    public boolean applies() {
        if (!We.terran()) return false;
        if (A.everyFrameExceptNthFrame(27)) return false;

        return true;
    }

    @Override
    protected void handle() {
        for (AUnit worker : FreeWorkers.get().list()) {
            if (!worker.recentlyMoved(40)) continue;

            if (worker.isIdle() && !worker.isGatheringMinerals()) {
                List<AUnit> unfinished = Select.ourUnfinished()
                    .buildings()
                    .excludeTypes(AUnitType.Terran_Refinery)
                    .sortDataByDistanceTo(worker, true);
                for (AUnit construction : unfinished) {
                    if (construction.type().isAddon()) continue;

                    if (construction.friendsNear().workers().inRadius(1.6, construction).empty()) {
                        worker.doRightClickAndYesIKnowIShouldAvoidUsingIt(construction);
                        worker.setTooltip("ConstructionUglyFix");
                    }
                }
            }
        }
    }
}
