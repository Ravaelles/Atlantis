package atlantis.combat.twins;

import atlantis.combat.cluster.Approach;
import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.cluster.ClusterTask;
import atlantis.combat.strategy.BattleStrategy;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

import java.util.HashMap;
import java.util.Map;

/**
 * The approach twin (owner 2026-11-09, approach narrowing): what the
 * task+geometry rule computed for this unit against the live pick
 * answer, with the geometry flags that explain a divergence.
 * Transitions always row, steady approaches heartbeat - twin before
 * gate, so a game log shows the shadow alive and stable (and where it
 * disagrees with the pick) before the commander port reads it.
 */
public final class ApproachTwin {

    public static final int HEARTBEAT_FRAMES = 300;

    private static final Map<Integer, String> lastByUnit = new HashMap<>();
    private static final Map<Integer, Integer> frameByUnit = new HashMap<>();

    private ApproachTwin() {
    }

    public static void clearForTests() {
        lastByUnit.clear();
        frameByUnit.clear();
    }

    public static void sample(AUnit unit, HasPosition anchor, BattleStrategy live) {
        try {
            if (unit == null) return;

            ArmyMission mission = ArmyMission.current();
            if (mission == null) return;

            Approach.Geometry geometry = Approach.capture(unit, anchor);
            BattleStrategy shadow = Approach.forTask(ClusterTask.forMission(mission), geometry);

            int now = A.now();
            String key = mission.name() + "/" + shadow.name() + "/" + (live == null ? "-" : live.name());
            String last = lastByUnit.get(unit.id());
            Integer lastFrame = frameByUnit.get(unit.id());
            boolean transition = !key.equals(last);
            if (!transition && lastFrame != null && now - lastFrame < HEARTBEAT_FRAMES) return;

            if (lastByUnit.size() > 500) {
                lastByUnit.clear();
                frameByUnit.clear();
            }
            lastByUnit.put(unit.id(), key);
            frameByUnit.put(unit.id(), now);

            EventLog.key(LogKey.APPROACH)
                .param("unit", unit.id())
                .param("mission", mission.name())
                .param("approach", shadow.name())
                .param("live", live == null ? "-" : live.name())
                .param("agree", live != null && live == shadow)
                .param("chokeNear", geometry.chokeNear)
                .param("crush", geometry.crushing())
                .param("duel", geometry.isDuel())
                .param("transition", transition)
                .log(now, A.minSec());
        } catch (Exception ignored) {
        }
    }
}
