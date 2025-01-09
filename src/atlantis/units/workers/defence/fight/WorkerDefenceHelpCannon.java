package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;
import atlantis.util.We;

public class WorkerDefenceHelpCannon extends Manager {
    private AUnit cannon;
    private Selection enemiesNearCannon;

    public WorkerDefenceHelpCannon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && unit.isWorker()
            && unit.hp() >= 19
            && (cannon = defineCannon()) != null
            && (enemiesNearCannon = cannon.enemiesNear().inRadius(7.8, cannon)).notEmpty()
            && enemiesNearCannon.countInRadius(7.8, cannon) >= 3
            && laterInGameAgainstRangedEnemiesJustIgnore()
            && ourCombatUnitsTooWeak();
    }

    private boolean laterInGameAgainstRangedEnemiesJustIgnore() {
        if (A.s >= 9.5 * 60) return true;
        if (Enemy.terran()) return false;

        return cannon.enemiesNear().ranged().atLeast(6)
            || cannon.enemiesNear().ranged().inRadius(10, cannon).atLeast(5);
    }

    private AUnit defineCannon() {
        return unit.friendsNear().cannons().mostDistantTo(unit);
    }

    private boolean ourCombatUnitsTooWeak() {
        double raceModifier = Enemy.protoss() ? 1 : 0.4;

        return cannon.friendsNear().combatUnits().count()
            < cannon.enemiesNear().havingAntiGroundWeapon().count() * raceModifier;
    }

    @Override
    public Manager handle() {
        if (unit.hp() <= 29 || cannon.enemiesNear().ranged().atLeast(10)) {
            if (unit.isMiningOrExtractingGas()) return null;
            if (unit.moveToNearestBase(Actions.MOVE_MACRO, null)) return usedManager(this);
        }

        AUnit enemy = enemiesNearCannon.canBeAttackedBy(unit, 99).nearestTo(unit);
        if (enemy == null) return null;

        if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemy)) {
            return usedManager(this);
        }

        return null;
    }
}
