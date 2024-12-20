package atlantis.combat.advance.tank;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.units.AUnit;

public class TerranAdvanceAsTank extends MissionManager {
    public TerranAdvanceAsTank(AUnit unit) {
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
