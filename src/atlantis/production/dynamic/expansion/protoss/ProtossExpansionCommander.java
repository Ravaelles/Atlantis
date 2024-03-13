package atlantis.production.dynamic.expansion.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class ProtossExpansionCommander extends Commander {
    public ProtossExpansionCommander() {
    }

    @Override
    public boolean applies() {
        return A.everyNthGameFrame(67)
            && Count.ourOfTypeUnfinished(AUnitType.Protoss_Nexus) < maxBasesAtATime()
            && ShouldExpand.shouldExpand();
    }

    @Override
    protected void handle() {
//        System.err.println("@ " + A.now() + " ProtossExpansionCommander ");
        ProtossExpandNow.requestNewBase();
    }

    public static int maxBasesAtATime() {
        return A.minerals() <= 620 ? 1 : 2;
    }
}
