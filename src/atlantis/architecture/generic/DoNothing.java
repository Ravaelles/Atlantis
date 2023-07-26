package atlantis.architecture.generic;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class DoNothing extends Manager {
    public DoNothing(AUnit unit) {
        super(unit);
    }

    @Override
    public Manager handle() {
        return null;
    }
}
