package atlantis.combat.micro.attack.expansion;

import atlantis.architecture.Manager;
import atlantis.combat.missions.attack.focus.EnemyExistingExpansion;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
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
            && (Enemy.zerg() || EnemyUnits.discovered().bases().count() >= 3)
            && unit.cooldown() <= 0
            && unit.eval() >= 1.6
            && unit.lastUnderAttackMoreThanAgo(50)
            && (expansion = EnemyExistingExpansion.get()) != null
            && unit.distTo(expansion) > 8;
    }

    @Override
    public Manager handle() {
        if (firstAvoidCB()) {
            return usedManager(this, "firstAvoidCB");
        }

        if (expansion == null || !expansion.isWalkable() || !unit.hasPathTo(expansion)) return null;

        if (unit.move(expansion, Actions.MOVE_ENGAGE, null)) {
            return usedManager(this);
        }

        return null;
    }

    private boolean firstAvoidCB() {
        AUnit cb = unit.enemiesNear().combatBuildingsAnti(unit).inRadius(10, unit).nearestTo(unit);

        if (cb == null) return false;

        if (unit.moveAwayFrom(cb, 4, Actions.MOVE_AVOID, "FirstAvoidCB")) return true;

        return false;
    }
}
