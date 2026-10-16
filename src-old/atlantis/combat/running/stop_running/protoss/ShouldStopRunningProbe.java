package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ShouldStopRunningProbe extends Manager {
    public ShouldStopRunningProbe(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWorker()
            && unit.enemiesNear().canAttack(unit, 2.5).havingAntiGroundWeapon().empty();
    }

    @Override
    protected Manager handle() {
        return usedManager(this);
    }
}
