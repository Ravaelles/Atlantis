package atlantis.combat.cluster;

import atlantis.combat.Behavior;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.game.A;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * The enforced half of a mission (owner #232, slices 3a/3b): when the decided
 * mission forbids leaving (HOLD_MAIN_CHOKE, HOLD_NATURAL_CHOKE), the march-out
 * branches hold the mission's own ground instead. Contact branches never
 * reach here - the caller veers only the no-target fallthrough (ghost
 * creep, collapse march), never a fight. FLEE rallies keep their own
 * path for the same reason: retreat owns routing, the mission owns
 * waiting.
 */
public final class MissionHold {

    private static String lastTwinned;
    private static int lastTwinFrame = -1000000;

    private MissionHold() {
    }

    public static void clearForTests() {
        lastTwinned = null;
        lastTwinFrame = -1000000;
    }

    /**
     * Hold the choke standoff the mission owns. A displaced unit walks
     * back to it (marching in, never out); a dressed one holds position.
     * Always handled: falling through would leak into the march-out legs
     * this veto exists to forbid. The standoff follows the mission: the
     * natural choke for HOLD_NATURAL_CHOKE (slice 3b), the main choke
     * otherwise.
     */
    public static boolean hold(AUnit unit, HasPosition anchor) {
        if (unit == null || !unit.isAlive()) return true;

        // Early harass override (owner 2026-11-07): the standoff is the
        // other half of the pinned dragoon - a healthy first Dragoon
        // with melee in front marches out instead of standing off.
        if (atlantis.combat.cluster.EarlyHarass.shouldHarass(unit, null)) return false;

        try {
            HasPosition standoff = ArmyMission.current() == ArmyMission.HOLD_NATURAL_CHOKE
                ? ClusterRally.naturalStandoffPoint()
                : ClusterRally.chokeStandoffPoint();
            if (standoff != null && standoff.position() != null
                && unit.distTo(standoff) > 2) {
                unit.move(standoff, Actions.MOVE_ENGAGE, Behavior.STANDOFF.tag());
            }
            else {
                unit.holdPosition(Actions.HOLD_POSITION, "mission-hold");
            }
        } catch (Exception | Error ignored) {
        }

        twin(unit);
        return true;
    }

    private static void twin(AUnit unit) {
        try {
            ArmyMission mission = ArmyMission.current();
            String row = (mission == null ? "-" : mission.name()) + "/forbid-outmarch";
            int now = A.now();
            boolean transition = !row.equals(lastTwinned);
            if (!transition && now - lastTwinFrame < 300) return;

            lastTwinned = row;
            lastTwinFrame = now;
            EventLog.key(LogKey.MISSION_ENFORCED)
                .param("mission", mission == null ? "-" : mission.name())
                .param("reason", "forbid-outmarch")
                .param("unit", unit == null ? -1 : unit.id())
                .param("transition", transition)
                .log(now, A.minSec());
        } catch (Exception ignored) {
        }
    }
}
