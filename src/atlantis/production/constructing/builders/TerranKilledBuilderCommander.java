package atlantis.production.constructing.builders;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.We;

public class TerranKilledBuilderCommander extends Commander {
    @Override
    public boolean applies() {
        return We.terran() && A.everyNthGameFrame(47);
    }

    @Override
    protected void handle() {
        for (AUnit building : Select.ourUnfinishedBuildings().list()) {
            if (!building.type().isAddon() && isNotBeingConstructed(building)) {
                assignWorkerToBuildingWithoutABuilder(building);
            }
        }
    }

    private void assignWorkerToBuildingWithoutABuilder(AUnit building) {
        AUnit worker = Select.ourWorkers().notGathering().nearestTo(building);
        if (worker != null) {
            worker.doRightClickAndYesIKnowIShouldAvoidUsingIt(building);

        }
    }

    private boolean isNotBeingConstructed(AUnit building) {
        for (AUnit worker : Select.ourWorkers().notGathering().inRadius(1.5, building).list()) {
            if (worker.buildUnit() == building || worker.target() == building) return false;
        }

        return true;
    }
}
