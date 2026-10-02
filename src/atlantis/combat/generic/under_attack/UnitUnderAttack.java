package atlantis.combat.generic.under_attack;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.generic.under_attack.protoss.ProtossMeleeUnitUnderAttack;
import atlantis.units.AUnit;

public class UnitUnderAttack extends Manager {
    public UnitUnderAttack(AUnit unit) {
        super(unit);
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossMeleeUnitUnderAttack::new,
            TerranUnitUnderAttack::new,
        };
    }
}

