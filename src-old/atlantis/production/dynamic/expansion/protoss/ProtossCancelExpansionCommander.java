package atlantis.production.dynamic.expansion.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.expansion.decision.ExpansionUnderPressure;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class ProtossCancelExpansionCommander extends Commander {
    @Override
    public boolean applies() {
        return We.protoss()
            && A.everyNthGameFrame(47)
            && Count.basesWithUnfinished() <= 2
            && Count.ourOfTypeUnfinished(AUnitType.Protoss_Nexus) > 0;
    }

    @Override
    protected boolean handle() {
        for (Construction construction : ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Nexus)) {
            if (A.hasMinerals(600)) continue;
            if (construction.progressPercent() >= 70) continue;
            if (
                Enemy.protoss()
                    && construction.buildPosition() != null
                    && EnemyUnits.discovered().dts().countInRadius(12, construction.buildPosition()) > 0
            ) continue;

            // Cancel only what is actually threatened: our army being small is
            // the normal state of expanding, not a reason to kill the nexus.
            if (!ExpansionUnderPressure.check(construction.buildPosition())) continue;

            System.err.println(A.minSec() + ": Cancelling expansion due to enemy pressure");
            construction.cancel("Cancel expansion due to enemy pressure");
        }
        return false;
    }
}
