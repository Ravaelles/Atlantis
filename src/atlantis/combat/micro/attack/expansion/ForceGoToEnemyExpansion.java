package atlantis.combat.micro.attack.expansion;

import atlantis.architecture.Manager;
import atlantis.combat.missions.attack.focus.EnemyExistingExpansion;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ForceGoToEnemyExpansion extends Manager {
    private HasPosition expansion;

    public ForceGoToEnemyExpansion(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.enemiesNear().combatUnits().inRadius(7, unit).atLeast(3)) return false;

        return unit.isCombatUnit()
            && unit.isMissionAttack()
            && unit.cooldown() <= 0
            && unit.lastUnderAttackMoreThanAgo(50)
            && (expansion = EnemyExistingExpansion.get()) != null
            && unit.distTo(expansion) > 8;
    }

    @Override
    public Manager handle() {
        if (unit.move(expansion, Actions.MOVE_ENGAGE, null)) {
            return usedManager(this);
        }

        return null;
    }
}
