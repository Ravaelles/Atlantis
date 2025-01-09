package atlantis.production.dynamic.expansion.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class ProtossCancelExpansionCommander extends Commander {
    @Override
    public boolean applies() {
        return A.everyNthGameFrame(47)
            && Count.basesWithUnfinished() <= 2
            && Count.ourOfTypeUnfinished(AUnitType.Protoss_Nexus) > 0
            && shouldCancelDueToEnemyPressure();
    }

    private boolean shouldCancelDueToEnemyPressure() {
        return Army.strength() <= 80;
    }

    @Override
    protected void handle() {
        for (Construction construction : ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Nexus)) {
            if (construction.progressPercent() >= 70) continue;
            System.err.println(A.minSec() + ": Cancelling expansion due to enemy pressure");
            construction.cancel("Cancel expansion due to enemy pressure");
        }
    }
}
