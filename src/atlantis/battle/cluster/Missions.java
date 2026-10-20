package atlantis.battle.cluster;

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
        currentGlobalMission = DEFEND;
        historyCount++;
    }

    public static void forceGlobalMissionAttack(String reason) {
        currentGlobalMission = ATTACK;
        historyCount++;
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
