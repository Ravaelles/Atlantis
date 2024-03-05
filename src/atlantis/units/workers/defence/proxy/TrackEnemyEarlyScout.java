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
        return enemyScout != null && enemyScout.isAlive() && enemyScout.hasPosition();
    }

    @Override
    public Manager handle() {
        if (!unit.isAttacking() || A.everyNthGameFrame(19)) {
            unit.attackUnit(enemyScout);
//            System.err.println("@ " + A.now() + " - " + unit + " - Track - " + enemyScout);
        }
        unit.setTooltip("FollowEnemyScout");

//        CameraCommander.centerCameraOn(unit);

        return usedManager(this);
    }
}
