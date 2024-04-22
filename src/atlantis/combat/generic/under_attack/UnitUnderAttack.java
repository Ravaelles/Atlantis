package atlantis.combat.generic.under_attack;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class UnitUnderAttack extends Manager {
    public UnitUnderAttack(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossUnitUnderAttack.class,
            TerranUnitUnderAttack.class,
        };
    }
}

