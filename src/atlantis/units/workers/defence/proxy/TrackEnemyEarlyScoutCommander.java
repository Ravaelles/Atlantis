package atlantis.units.workers.defence.proxy;

import atlantis.architecture.Commander;
import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;

/**
 * Tracks an early enemy worker scout and sends one of our own probes to deal
 * with
 * it.
 *
 * <p>
 * Deliberately lives in this package, which is inside {@code atlantis.units}: a
 * commander here must not reach into a consumer package, so it sends a worker
 * home
 * with {@link AUnit#gatherBestResources()} rather than by invoking the
 * {@code workers.gather} manager. That import was the last ArchUnit violation
 * in
 * this area.
 * </p>
 *
 * <p>
 * <b>The tracker used to die every game</b> (owner report, 2026-10-08). Two
 * causes,
 * both fixed here:
 * </p>
 * <ul>
 * <li>the defender was chosen once and kept even when it was nearly dead, so a
 * 20 hp Probe was ordered to attack a scout that kills it in two hits. A
 * defender
 * below {@link #DEFENDER_MIN_HP} is now sent home and replaced instead of being
 * re-sent;</li>
 * <li>the assignment was {@code static}, so it survived between games (and
 * between tests) - a defender from the previous game could be "still assigned".
 * {@link #clear()} resets it per game.</li>
 * </ul>
 */
public class TrackEnemyEarlyScoutCommander extends Commander {

    /**
     * A defender below this is not sent into a fight: it is replaced and sent home.
     */
    private static final int DEFENDER_MIN_HP = 36;

    private static AUnit enemyScout = null;
    private static AUnit ourDefender = null;

    /** Reset the per-game assignment. Called on game start. */
    public static void clear() {
        enemyScout = null;
        ourDefender = null;
    }

    @Override
    public boolean applies() {
        return Enemy.protoss() && A.supplyUsed() <= 28 && A.everyNthGameFrame(3);
    }

    @Override
    protected boolean handle() {
        if (detectEnemyScout()) {
            assignOrReplaceDefender();
            if (ourDefender != null && ourDefender.isAlive()) {
                sendDefenderToFight();
            }
        } else
            noDefenderNeeded();
        return false;
    }

    /**
     * Sends the defender at the scout, but only while it is healthy enough to
     * survive. A hurt defender is sent home and replaced by a fresh one - the fix
     * for "the tracker worker kept dying": previously the same wounded Probe was
     * re-sent until it died.
     */
    private Manager sendDefenderToFight() {
        if (ourDefender.hp() < DEFENDER_MIN_HP) {
            ourDefender.gatherBestResources();
            ourDefender = null;
            assignOrReplaceDefender();
            if (ourDefender == null)
                return null;
        }

        return (new TrackEnemyEarlyScout(ourDefender, enemyScout)).invokeFrom(this);
    }

    private static void noDefenderNeeded() {
        if (ourDefender != null) {
            ourDefender.gatherBestResources();
        }

        ourDefender = null;
    }

    /**
     * Picks a healthy free worker as the defender, or replaces a dead or wounded
     * one. Replace-on-wounded is what stops a wound becoming a death: a Probe at
     * 20 hp cannot trade with an enemy worker.
     */
    private void assignOrReplaceDefender() {
        if (ourDefender != null && ourDefender.isAlive() && ourDefender.hp() >= DEFENDER_MIN_HP) return;

        // Select.ourWorkers() rather than FreeWorkers: FreeWorkers lives in
        // atlantis.units.workers, which this rule treats as a consumer package, and
        // a commander in the core package must not reach into one.
        ourDefender = Select.ourWorkers()
                .exclude(ourDefender)
                .havingAtLeastHp(DEFENDER_MIN_HP)
                .nearestTo(enemyScout);
    }

    private boolean detectEnemyScout() {
        if (enemyScout == null) {
            AUnit main = Select.main();
            if (main == null)
                return false;

            enemyScout = Select.enemy().workers().inRadius(30, main).nearestTo(main);

            // Not a lone scout - it has friends near, so this is a push, not a scout.
            if (enemyScout != null && enemyScout.friendsNear().workers().countInRadius(7, enemyScout) > 0) {
                enemyScout = null;
            }

            if (enemyScout != null && enemyScout.friendsNear().combatUnits().countInRadius(5, enemyScout) > 0) {
                enemyScout = null;
            }
        }

        return enemyScout != null && enemyScout.isAlive();
    }
}
