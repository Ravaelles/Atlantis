package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

/**
 * Ride the in-flight scout course: while this scout moves under a scout
 * order, the movement managers below never re-evaluate, so two of them
 * can no longer overwrite each other every frame (measured: the early
 * worker circled between the enemy second and third base and never saw
 * anything, each frame another manager's destination won).
 *
 * <p>Placement in the chain is the point: the avoid managers run first
 * every frame, so fleeing still preempts the course - the only thing
 * that may interrupt a riding scout is danger. Everything else waits
 * for arrival (unit stops), a wall push (no position change), or the
 * ride cap (knowledge may have changed meanwhile: the enemy base may
 * have been found by someone else).</p>
 */
public class ScoutHoldCourse extends Manager {

    /**
     * A fresh order needs frames to land before progress can be read.
     */
    public static final int SETTLE_FRAMES = 30;

    /**
     * No position change this long while moving = pushing a wall, re-pick.
     */
    public static final int STUCK_FRAMES = 100;

    /**
     * Older than this the course is stale even with progress: re-evaluate.
     */
    public static final int RIDE_MAX_FRAMES = 480;

    public ScoutHoldCourse(AUnit unit) {
        super(unit);
    }

    /**
     * The ride rule, clock-free: fresh orders land untouched, then only
     * a moving scout with progress and a fresh-enough course rides.
     */
    public static boolean shouldRide(boolean scoutAction, boolean moving, int orderAgo, int changedAgo) {
        if (!scoutAction || !moving) return false;
        if (orderAgo <= SETTLE_FRAMES) return true;
        if (changedAgo > STUCK_FRAMES) return false;

        return orderAgo <= RIDE_MAX_FRAMES;
    }

    @Override
    public boolean applies() {
        if (unit == null || !unit.isAlive()) return false;

        return shouldRide(
            unit.action() == Actions.MOVE_SCOUT || unit.action() == Actions.MOVE_EXPLORE,
            unit.isMoving(),
            unit.lastOrderWasFramesAgo(),
            unit.lastPositionChangedAgo()
        );
    }

    @Override
    protected Manager handle() {
        unit.setTooltipTactical("HoldCourse");

        return usedManager(this);
    }
}
