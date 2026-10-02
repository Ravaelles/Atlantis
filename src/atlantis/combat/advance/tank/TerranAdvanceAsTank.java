package atlantis.combat.advance.tank;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            AdvanceAsTankWounded::new,
            AdvanceAsTankCoordinate::new,
        };
    }
}
