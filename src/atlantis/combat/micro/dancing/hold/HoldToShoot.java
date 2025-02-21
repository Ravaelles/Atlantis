package atlantis.combat.micro.dancing.hold;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;

/**
 * The idea is to stop the unit using Hold Position command and then attack the enemy,
 * gaining a couple of frames of advantage.
 */
public class HoldToShoot extends Manager {
    public HoldToShoot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isRanged()) return false;
        if (!unit.hasValidTarget()) return false;

        return true;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ContinueHoldToShoot.class,
            DragoonHoldToShoot.class,
        };
    }
}
