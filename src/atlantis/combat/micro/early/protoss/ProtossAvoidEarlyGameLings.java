package atlantis.combat.micro.early.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;

public class ProtossAvoidEarlyGameLings extends Manager {
    private Selection enemies;

    public ProtossAvoidEarlyGameLings(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && Enemy.zerg()
            && Count.ourCombatUnits() <= 7
            && unit.isMelee()
            && (Enemy.zerg() || Enemy.protoss())
            && unit.shieldWound() >= 4
            && unit.combatEvalRelative() <= 1.1
            && unit.lastAttackFrameLessThanAgo(30 * 5)
            && (enemies = unit.enemiesNear().groundUnits().melee()).notEmpty()
            && enemies.inRadius(4, unit).notEmpty()
            && unit.friendsNear().workers().inRadius(2, unit).empty()
            && unit.friendsNear().buildings().inRadius(2, unit).empty();
    }

    @Override
    public Manager handle() {
        AUnit enemy = enemies.havingAtLeastHp(1).first();
        if (enemy == null) return null;

        if (enemy.enemiesNear().workers().inRadius(2, unit).notEmpty()) return null;

        if (unit.runningManager().runFrom(
            enemies.nearestTo(unit), 4, Actions.MOVE_AVOID, true
        )) return usedManager(this);

        return null;
    }
}

