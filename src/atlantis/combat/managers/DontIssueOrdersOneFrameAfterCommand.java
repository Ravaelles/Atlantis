package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class DontIssueOrdersOneFrameAfterCommand extends Manager {
    public DontIssueOrdersOneFrameAfterCommand(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.lastCommandIssuedAgo() <= 1;
    }

    @Override
    public Manager handle() {
        return usedManager(unit.manager());
    }
}
