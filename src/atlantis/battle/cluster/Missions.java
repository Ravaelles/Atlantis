package atlantis.battle.cluster;

import atlantis.game.A;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * The global mission value. Phase 0 of the combat cutover
 * (_AI/__PLAN-COMBAT.md): the state machine that used to move it
 * (MissionChanger, force-calls, Sparta) is deleted - the value sits at
 * DEFEND until BattleCommander owns it from step 5. Everything that
 * reads it keeps compiling and behaves "defend" throughout.
 */
public class Missions {
    public static final Mission ATTACK = new Mission("Attack");
    public static final Mission DEFEND = new Mission("Defend");

    protected static Mission currentGlobalMission = DEFEND;
    protected static int historyCount = 0;

    public static Mission globalMission() {
        return currentGlobalMission != null ? currentGlobalMission : DEFEND;
    }

    public static void forceGlobalMissionDefend(String reason) {
        setGlobalMission(DEFEND, reason);
    }

    public static void forceGlobalMissionAttack(String reason) {
        setGlobalMission(ATTACK, reason);
    }

    /**
     * Single setter behind both force calls: counts every call, but
     * rows the twin only on an actual change (followPosture runs every
     * frame, so an unconditional row would flood the log with
     * no-op defends). Gives the unenaction sanity check a name and a
     * reason: which transition happened, when, and why.
     */
    private static void setGlobalMission(Mission next, String reason) {
        Mission prev = currentGlobalMission;
        currentGlobalMission = next;
        historyCount++;
        if (prev != null && prev.equals(next)) return;

        try {
            EventLog.key(LogKey.MISSION)
                .param("from", prev == null ? "-" : prev.name())
                .param("to", next == null ? "-" : next.name())
                .param("reason", reason == null ? "-" : reason)
                .log(A.now(), A.minSec());
        } catch (Exception ignored) {
        }
    }

    /**
     * The missions follow the posture: attack posture attacks, anything
     * else defends. The cluster owns the value; this only translates it.
     */
    public static void followPosture(ClusterMission.Posture posture) {
        if (posture == ClusterMission.Posture.ATTACK) {
            forceGlobalMissionAttack("cluster-attack");
        }
        else {
            forceGlobalMissionDefend("cluster-defend");
        }
    }

    public static boolean isGlobalMissionAttack() {
        return globalMission().equals(ATTACK);
    }

    public static boolean isGlobalMissionDefendOrSparta() {
        return globalMission().equals(DEFEND);
    }

    public static boolean isGlobalMissionSparta() {
        return false;
    }

    public static boolean isGlobalMissionContain() {
        return false;
    }

    public static boolean isFirstMission() {
        return historyCount == 0;
    }

    public static void reset() {
        currentGlobalMission = DEFEND;
        historyCount = 0;
    }

    public static int historyCount() {
        return historyCount;
    }

    public static Mission fromString(String name) {
        if (ATTACK.name().equalsIgnoreCase(name)) return ATTACK;

        return DEFEND;
    }
}
