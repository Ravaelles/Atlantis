package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ShouldStopRunningProtossAir extends Manager {
    public ShouldStopRunningProtossAir(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isAir()
            && unit.enemiesNear().groundUnits().combatUnits()
            .countInRadius(8.5 + unit.woundPercent() / 30.0, unit) == 0;
    }

    @Override
    protected Manager handle() {
        return usedManager(this);
    }
}
