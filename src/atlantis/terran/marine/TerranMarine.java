package atlantis.terran.marine;

import atlantis.architecture.Manager;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranMarineLongNotAttacked.class,
        };
    }
}
