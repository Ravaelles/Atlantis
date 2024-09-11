package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.protoss.reaver.ReaverAlwaysAttack;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ProtossReaver extends Manager {
    public ProtossReaver(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isReaver();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ReaverAlwaysAttack.class,
            ReaverControlEnemyDistance.class,
        };
    }
}
