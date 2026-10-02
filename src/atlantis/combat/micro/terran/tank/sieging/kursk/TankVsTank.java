package atlantis.combat.micro.terran.tank.sieging.kursk;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.units.AUnit;

public class TankVsTank extends Manager {
    public TankVsTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isSieged();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            DontUnsiegeEnemyTanksNear::new,
        };
    }
}
