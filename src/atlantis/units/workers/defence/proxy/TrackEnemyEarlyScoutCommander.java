package atlantis.units.workers.defence.proxy;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.units.workers.GatherResources;
import atlantis.util.Enemy;

public class TrackEnemyEarlyScoutCommander extends Commander {
    private static AUnit enemyScout = null;
    private static AUnit ourDefender = null;

    @Override
    public boolean applies() {
        return Enemy.protoss() && A.supplyUsed() <= 28 && A.everyNthGameFrame(7);
    }

    @Override
    protected void handle() {
        if (detectEnemyScout()) {
            haveDefenderAssigned();
            if (ourDefender != null) {
                (new TrackEnemyEarlyScout(ourDefender, enemyScout)).invoke(this);
            }
        }
        else noDefenderNeeded();
    }

    private static void noDefenderNeeded() {
        if (ourDefender != null) {
            (new GatherResources(ourDefender)).invoke(TrackEnemyEarlyScoutCommander.class);
        }

        ourDefender = null;
    }

    private void haveDefenderAssigned() {
        if (ourDefender == null) {
            ourDefender = FreeWorkers.get().nearestTo(enemyScout);
        }
    }

    private boolean detectEnemyScout() {
        if (enemyScout == null) {
            AUnit main = Select.main();
            if (main == null) return false;

//            enemyScout = Select.enemy().workers().inRadius(30, main).nearestTo(main);
            enemyScout = Select.enemy().inRadius(30, main).nearestTo(main);
        }

        return enemyScout != null && enemyScout.isAlive();
    }
}
