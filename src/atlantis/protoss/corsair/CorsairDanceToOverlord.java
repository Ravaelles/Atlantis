package atlantis.protoss.corsair;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class CorsairDanceToOverlord extends Manager {
    private AUnit target;

    public CorsairDanceToOverlord(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        target = unit.target();

        return target != null
            && target.isOverlord()
            && unit.cooldown() >= 9
            && unit.distTo(target) >= (3 + unit.woundPercent() / 50.0);
    }

    @Override
    public Manager handle() {
        if (unit.move(target, Actions.MOVE_DANCE_TO)) {
            return usedManager(this, "CorsairDance2Over");
        }

        return null;
    }
}
