package atlantis.terran.marine;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.units.AUnit;

public class TerranMarine extends Manager {
    public TerranMarine(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMarine()) return false;

        return true;
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            TerranMarineLongNotAttacked::new,
        };
    }
}
