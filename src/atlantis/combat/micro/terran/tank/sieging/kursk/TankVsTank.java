package atlantis.combat.micro.terran.tank.sieging.kursk;

import atlantis.architecture.Manager;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            DontUnsiegeEnemyTanksNear.class,
        };
    }
}
