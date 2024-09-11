package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.ContinueLastAttack;
import atlantis.combat.micro.avoid.ContinueRunning;
import atlantis.units.AUnit;

public class ContinueCurrentAction extends Manager {
    public ContinueCurrentAction(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            ContinueShooting.class,
//            ContinueRunning.class,
//            ContinueLastAttack.class,
//            composer.json
//            ContinueShootingAsDragoon.class, // Dont!!
//            ContinueAttack.class, // Dont!
//            ContinueMoving.class,
        };
    }
}
