package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;

public class ForceContinueCriticalMeleeAttack extends Manager {
    public ForceContinueCriticalMeleeAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isAttacking()) return false;
        if (!unit.isMelee()) return false;
        if (!unit.isCombatUnit()) return false;

        AUnit target = unit.target();
        if (target == null) return false;
        if (!unit.hasValidTarget()) return false;

        return isTargetThatShouldNotBeAbandoned(target) && unit.isTargetInWeaponRangeAccordingToGame(target);
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }

    private boolean isTargetThatShouldNotBeAbandoned(AUnit target) {
        if (Enemy.protoss()) return target.isReaver();
        else if (Enemy.terran()) return target.isTank() || target.isBunker();

        return false;
    }
}
