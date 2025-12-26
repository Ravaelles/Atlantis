package atlantis.units.special.idle.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.generic.DoNothing;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossFixIdleUnits extends Manager {
    public ProtossFixIdleUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isCombatUnit()) return false;
        if (unit.lastPositionChangedAgo() <= 5) return false;
        if (unit.hasCooldown()) return false;
        if (We.protoss() && unit.hp() <= 50) return false;
        if (unit.enemiesNear().combatBuildingsAntiLand().notEmpty()) return false;

        if (unit.isActiveManager(DoNothing.class)) return true;

        return true;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            FixIdleUnitsGeneric.class,
            FixIdleUnitsPostAttack.class,
            FixIdleUnitsPostAvoid.class,
        };
    }
}
