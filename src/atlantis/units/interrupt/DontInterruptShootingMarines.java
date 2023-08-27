package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.units.AUnit;

public class DontInterruptShootingMarines extends Manager {
    public DontInterruptShootingMarines(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isMarine()
            && (!unit.hasMedicInHealRange() || unit.hp() <= 25)
            && unit.meleeEnemiesNearCount(2.4) > 0;
    }

    @Override
    public Manager handle() {
        AvoidEnemies avoidEnemies = new AvoidEnemies(unit);
        if (avoidEnemies.avoidEnemiesIfNeeded() != null) {
            return usedManager(avoidEnemies, "Avoid");
        }

        return null;
    }
}
