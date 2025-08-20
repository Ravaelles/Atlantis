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
