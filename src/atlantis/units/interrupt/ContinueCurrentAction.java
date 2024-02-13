package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ContinueCurrentAction extends Manager {
    public ContinueCurrentAction(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ContinueShooting.class,
//            ContinueDragoonAttackOrder.class, // Dont!!
//            ContinueAttack.class, // Dont!
            ContinueMoving.class,
        };
    }
}