package atlantis.units.workers.defence.proxy;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.CameraCommander;
import atlantis.units.AUnit;

public class TrackEnemyEarlyScout extends Manager {
    private final AUnit enemyScout;

    public TrackEnemyEarlyScout(AUnit unit, AUnit enemyScout) {
        super(unit);
        this.enemyScout = enemyScout;
    }

    @Override
    public boolean applies() {
        // A worker must never chase a scout while a real enemy is in reach.
        //
        // The owner's death log (2026-10-07) shows the exact failure this line
        // prevents: a Probe alternating between GatherResources,
        // WorkerHelpCombatUnitsFight and TrackEnemyEarlyScout - following the
        // scout - until a Zealot killed it. Chasing is a LUXURY: it is only
        // allowed when nothing that can hurt us is nearby.
        if (unit.enemiesNear().combatUnits().canAttack(unit, 4).notEmpty()) return false;

        // The same reasoning for the worker's own health: a hurt Probe has no
        // business following anything.
        if (unit.hp() <= 30) return false;

        return enemyScout != null
            && unit != null
            && unit.hp() >= 18
            && !unit.isBuilder()
            && unit.friendsNear().buildings().notEmpty()
            && enemyScout.isAlive()
            && enemyScout.hasPosition()
            && enemyScout.friendsNear().combatUnits().countInRadius(6, unit) == 0;
    }

    @Override
    public Manager handle() {
        if (unit == null || enemyScout == null) return null;

        if (!unit.isAttacking() || A.everyNthGameFrame(19)) {
            unit.attackUnit(enemyScout);
//            System.err.println("@ " + A.now() + " - " + unit + " - Track - " + enemyScout);
        }
        unit.setTooltip("FollowEnemyScout");

//        CameraCommander.centerCameraOn(unit);

        return usedManager(this);
    }
}
