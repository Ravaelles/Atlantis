package atlantis.combat.advance.tank;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.units.AUnit;

public class AdvanceAsTank extends MissionManager {
    public AdvanceAsTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTank() && unit.isMissionAttack();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AdvanceAsTankWounded.class,
            AdvanceAsTankCoordinate.class,
        };
    }
}
