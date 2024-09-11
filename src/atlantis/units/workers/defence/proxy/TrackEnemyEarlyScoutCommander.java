package atlantis.units.workers.defence.proxy;

import atlantis.architecture.Commander;
import atlantis.architecture.Manager;
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
        return Enemy.protoss() && A.supplyUsed() <= 28 && A.everyNthGameFrame(3);
    }

    @Override
    protected void handle() {
        if (detectEnemyScout()) {
            haveDefenderAssigned();
            if (ourDefender != null && ourDefender.isAlive()) {
                sendDefenderToFight();
            }
        }
        else noDefenderNeeded();
    }

    private Manager sendDefenderToFight() {
        if (ourDefender.hp() <= 18) {
            (new GatherResources(ourDefender)).invokeFrom(TrackEnemyEarlyScoutCommander.class);
            ourDefender = FreeWorkers.get().exclude(ourDefender).nearestTo(enemyScout);
        }

        return (new TrackEnemyEarlyScout(ourDefender, enemyScout)).invokeFrom(this);
    }

    private static void noDefenderNeeded() {
        if (ourDefender != null) {
            (new GatherResources(ourDefender)).invokeFrom(TrackEnemyEarlyScoutCommander.class);
        }

        ourDefender = null;
    }

    private void haveDefenderAssigned() {
        if (ourDefender == null || ourDefender.isDead()) {
            ourDefender = FreeWorkers.get().nearestTo(enemyScout);
        }
//        System.err.println("ourDefender = " + ourDefender);
    }

    private boolean detectEnemyScout() {
        if (enemyScout == null) {
            AUnit main = Select.main();
            if (main == null) return false;

//            enemyScout = Select.enemy().workers().inRadius(30, main).nearestTo(main);
            enemyScout = Select.enemy().inRadius(30, main).nearestTo(main);

//            if (enemyScout != null) {
//                System.err.println("Enemy scout detected: " + enemyScout);
//            }
        }

        return enemyScout != null && enemyScout.isAlive();
    }
}
