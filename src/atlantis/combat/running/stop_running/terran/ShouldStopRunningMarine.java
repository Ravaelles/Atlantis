package atlantis.combat.running.stop_running.terran;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.units.AUnit;

public class ShouldStopRunningMarine extends Manager {
    public ShouldStopRunningMarine(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isMarine()
            && unit.isHealthy()
            && unit.cooldown() <= 2
            && unit.hasMedicInRange()
            && unit.meleeEnemiesNearCount(2.1) == 0;
    }

    @Override
    protected Manager handle() {
        unit.runningManager().stopRunning();

        if ((new AttackNearbyEnemies(unit)).invokeFrom(this) != null) {
            return usedManager(this);
        }

        return null;
    }
}
