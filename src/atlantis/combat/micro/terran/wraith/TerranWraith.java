package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.air.RunForYourLife;
import atlantis.terran.repair.UnitBeingReparedManager;
import atlantis.units.AUnit;

public class TerranWraith extends Manager {
    public TerranWraith(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWraith();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            RunForYourLife.class,
            UnitBeingReparedManager.class,
            AttackAsWraith.class,
        };
    }

}
