package atlantis.units.workers.defence.proxy;

import atlantis.architecture.Manager;
import atlantis.game.A;
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
        System.err.println("@ " + A.now() + " - " + unit + " - TrackEnemyEarlyScout");

        unit.attackUnit(enemyScout);
        unit.setTooltip("FollowEnemyScout");

        return null;
    }
}
