package atlantis.production.constructions.protoss;

import atlantis.game.A;
import atlantis.game.event.AutomaticListener;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;

public class ProtossLimitExistingConstructions extends AutomaticListener {
    @Override
    public String listensTo() {
        return "OurBuildingCreated";
    }

    @Override
    public void onEvent(String event, Object... data) {
        AUnit unit = (AUnit) data[0];

        if (!unit.type().isABuilding()) return;

        if (unit.type().isPylon()) limitPylonsAtOnce();
    }

    private void limitPylonsAtOnce() {
        int notFinished;
        if ((notFinished = ConstructionRequests.countNotFinishedOfType(AUnitType.Protoss_Pylon)) <= maxPylonsAtOnce()) return;

        ArrayList<Construction> notStarted = ConstructionRequests.notStartedOfType(AUnitType.Protoss_Pylon);
        for (Construction construction : notStarted) {
            ErrorLog.debug("CANCELING too many pylons: " + notStarted.size());

            construction.cancel("Too many pylons");
        }

        ErrorLog.debug("NOW NOT STARTED: " + notStarted.size() + ", NOT FINISHED: " + notFinished);
    }

    private static int maxPylonsAtOnce() {
        return A.supplyUsed() <= 80 ? 1 : (2 + A.minerals() / 400);
    }
}
