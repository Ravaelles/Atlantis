package atlantis.units.special.idle;

import atlantis.architecture.Manager;
import atlantis.combat.generic.DoNothing;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class FixIdleUnits extends Manager {
    public FixIdleUnits(AUnit unit) {
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
