package atlantis.combat.micro.terran;

import atlantis.terran.repair.UnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.architecture.Manager;

public class TerranWraith extends Manager {

    public TerranWraith(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            UnitBeingReparedManager.class
        };
    }

}
