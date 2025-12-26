package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.protoss.ProtossAvoidEnemies;
import atlantis.units.AUnit;

public class ProtossShuttleAvoidEnemies extends Manager {
    public ProtossShuttleAvoidEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return true;
    }

    @Override
    public Manager handle() {
        if ((new ProtossAvoidEnemies(unit)).forceHandle() != null) {
            return usedManager(this);
        }

        return null;
    }
}
