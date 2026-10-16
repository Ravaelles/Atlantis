package atlantis.combat.running.fallback;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class RunAttackFallback extends Manager {
    public RunAttackFallback(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return false;
    }
}
