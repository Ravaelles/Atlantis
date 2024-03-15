package atlantis.combat.generic;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.protoss.ProtossFlags;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

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

