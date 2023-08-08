package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class AdvanceAsTerran extends Manager {
    public AdvanceAsTerran(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTerran() && unit.mission().focusPoint() != null;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            BeCloseToLeader.class,
        };
    }
}
