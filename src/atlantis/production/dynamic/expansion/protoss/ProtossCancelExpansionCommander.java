package atlantis.production.dynamic.expansion.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
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
            if (A.hasMinerals(600)) continue;
            if (construction.progressPercent() >= 70) continue;
            if (
                Enemy.protoss()
                    && construction.buildPosition() != null
                    && EnemyUnits.discovered().dts().countInRadius(12, construction.buildPosition()) > 0
            ) continue;


            System.err.println(A.minSec() + ": Cancelling expansion due to enemy pressure");
            construction.cancel("Cancel expansion due to enemy pressure");
        }
    }
}
