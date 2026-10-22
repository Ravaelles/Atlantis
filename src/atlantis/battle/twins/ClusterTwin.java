package atlantis.battle.twins;

import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.ClusterBreach;
import atlantis.battle.cluster.ClusterMission;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.battle.strategy.GhostAdvance;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * Throttled CLUSTER twin: posture, tactic, size, anchor. JSON-only,
 * stderr. The only per-game proof of what the cluster does - sampled
 * every 31st frame, skipped silently in between.
 *
 * <p>Cluster center readout (owner 2026-10-26): the file carries the
 * army's mean position and the nearest enemy's distance to it - the
 * forward-movement measure per-unit traces cannot give.</p>
 */
public final class ClusterTwin {

    private ClusterTwin() {
    }

    /** Sample the cluster state; only every 31st frame writes a row. */
    public static void sample(
        ClusterMission.Posture posture, FightOrFlight.Tactic tactic, HasPosition anchor
    ) {
        if (!A.everyNthGameFrame(31)) return;

        HasPosition center = Alpha.get().center();
        int foeDist = -1;
        String ghostSource = "-";
        int ghostDist = -1;
        int ghostX = -1;
        int ghostY = -1;
        try {
            if (center != null) {
                AUnit nearest = Select.enemy()
                    .combatUnits().nearestTo(center);
                if (nearest != null) foeDist = (int) (center.distTo(nearest) * 10);
            }
            AUnit leader = Alpha.get().leader();
            if (leader != null && leader.isAlive()) {
                ghostSource = GhostAdvance.ghostSourceFor(leader);
                APosition ghost = GhostAdvance.ghostPointFor(leader);
                if (ghost != null) {
                    ghostDist = (int) (leader.distTo(ghost) * 10);
                    ghostX = ghost.tx();
                    ghostY = ghost.ty();
                }
            }
        } catch (Exception ignored) {
        }
        EventLog.key(LogKey.CLUSTER)
            .param("cluster", Alpha.get().size())
            .param("posture", posture.name())
            .param("tactic", tactic.name())
            .param("breached", ClusterBreach.breachForcesFight())
            .param("anchor", anchor == null ? "-" : anchor.toString())
            .param("cx", center == null ? -1 : center.tx())
            .param("cy", center == null ? -1 : center.ty())
            .param("foe", foeDist)
            .param("ghost", ghostSource)
            .param("gdist", ghostDist)
            .param("gx", ghostX)
            .param("gy", ghostY)
            .log(A.now(), A.minSec());
    }
}
