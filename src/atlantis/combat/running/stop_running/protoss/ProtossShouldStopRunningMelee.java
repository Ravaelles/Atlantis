package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ProtossShouldStopRunningMelee extends Manager {
    public ProtossShouldStopRunningMelee(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isMelee()
            && unit.combatEvalRelative() >= 2;
    }

    @Override
    public Manager handle() {
        ProtossShouldStopRunning.decisionStopRunning(unit);
        
        if (unit.mission().handleManagerClass(unit) != null) return usedManager(this);

        return null;
    }
}
