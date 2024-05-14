package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.running.ShouldContinueRunning;
import atlantis.units.AUnit;

public class ContinueRunning extends Manager {
    public ContinueRunning(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;
        return ShouldContinueRunning.handleContinueRunning(unit);
    }

    public Manager handle() {
        return usedManager(this);
    }
}
