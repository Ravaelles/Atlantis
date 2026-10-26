package atlantis.combat.twins;

import atlantis.combat.IdleWatch;
import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.ClusterBreach;
import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.retreating.RetreatFromLostFight;
import atlantis.combat.strategy.GhostAdvance;
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
 *
 * <p>Leader dest readout (owner 2026-10-23, Stardust-style: log the
 * transition target, not every step): the leader's current move dest.
 * On a retreat every unit walks its own home point, so the leader's
 * dest names the rally the army is actually walking (standoff vs base
 * vs anchor) - the question no center trace can answer.</p>
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
        int foeX = -1;
        int foeY = -1;
        String ghostSource = "-";
        int ghostDist = -1;
        int ghostX = -1;
        int ghostY = -1;
        int destX = -1;
        int destY = -1;
        String fleeReason = "-";
        int strength = -1;
        String focus = "-";
        try {
            if (tactic == FightOrFlight.Tactic.FLEE) {
                fleeReason = dominantFleeReason(tactic, anchor);
            }
            if (center != null) {
                AUnit nearest = Select.enemy()
                    .combatUnits().nearestTo(center);
                if (nearest != null) {
                    foeDist = (int) (center.distTo(nearest) * 10);
                    try {
                        if (nearest.position() != null) {
                            foeX = nearest.position().tx();
                            foeY = nearest.position().ty();
                        }
                    } catch (Exception ignored) {
                    }
                }
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
                try {
                    APosition dest = leader.targetPosition();
                    if (dest != null) {
                        destX = dest.tx();
                        destY = dest.ty();
                    }
                } catch (Exception ignored) {
                }
            }
            try {
                strength = atlantis.information.generic.Army.strength();
                focus = focusName(anchor);
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
        EventLog.key(LogKey.CLUSTER)
            .param("cluster", Alpha.get().size())
            .param("posture", posture.name())
            .param("tactic", tactic.name())
            .param("strength", strength)
            .param("focus", focus)
            .param("breached", ClusterBreach.breachForcesFight())
            .param("anchor", anchor == null ? "-" : anchor.toString())
            .param("cx", center == null ? -1 : center.tx())
            .param("cy", center == null ? -1 : center.ty())
            .param("foe", foeDist)
            .param("fx", foeX)
            .param("fy", foeY)
            .param("ghost", ghostSource)
            .param("gdist", ghostDist)
            .param("gx", ghostX)
            .param("gy", ghostY)
            .param("dtx", destX)
            .param("dty", destY)
            .param("fleeReason", fleeReason)
            .log(A.now(), A.minSec());

        noteArmyStall(center);
    }

    /**
     * Short focus name for the row (owner: place + kind, mobile units
     * excluded - a building anchor names its type, a chasing unit never
     * does). Non-focus anchors fall back to their own string. Public as
     * a seam: the board publisher reads the same answer, never its own
     * copy.
     */
    public static String focusName(HasPosition anchor) {
        if (!(anchor instanceof atlantis.battle.advance.focus.AFocusPoint)) {
            return anchor == null ? "-" : anchor.toString();
        }
        atlantis.battle.advance.focus.AFocusPoint fp =
            (atlantis.battle.advance.focus.AFocusPoint) anchor;
        String name = fp.name() == null ? "Focus" : fp.name();
        String tile = "?";
        try {
            if (fp.position() != null) tile = fp.position().tx() + "," + fp.position().ty();
        } catch (Exception ignored) {
        }
        try {
            AUnit u = fp.unit();
            if (u != null && u.isAlive() && !u.isABuilding() && u.canMove()) return name + "@" + tile;
            if (u != null && u.isAlive() && u.isABuilding()) return name + "@" + tile + ":" + u.type().name();
        } catch (Exception ignored) {
        }
        return name + "@" + tile;
    }

    /**
     * The dominant flee reason across the cluster (NEXT #195): on FLEE
     * frames the row names why the army runs - the majority of the
     * per-unit verdicts from the single classification point. "-"
     * when the cluster is empty or nobody flees. Public as a seam: the
     * aggregation pins without a game.
     */
    public static String dominantFleeReason(FightOrFlight.Tactic tactic, HasPosition anchor) {
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        String best = null;
        int bestCount = 0;

        AUnit leader = Alpha.get().leader();
        for (AUnit unit : Alpha.get().selection().list()) {
            if (unit == null || !unit.isAlive()) continue;

            RetreatFromLostFight.Verdict verdict;
            try {
                verdict = RetreatFromLostFight.decide(unit, tactic, anchor, leader);
            } catch (RuntimeException ignored) {
                continue;
            }
            if (verdict == null || verdict.reason == null) continue;

            int count = counts.merge(verdict.reason.name(), 1, Integer::sum);
            if (count > bestCount) {
                best = verdict.reason.name();
                bestCount = count;
            }
        }

        return best == null ? "-" : best;
    }

    /**
     * The all-units freeze (owner, Drag_v_LingsUpg2): every combat unit
     * banked ten idle seconds with no enemy near the center - the
     * per-unit IDLE rows share one cause, and it deserves one HIGH row.
     * Throttled: at most one row per 240 frames while it persists.
     */
    private static int lastStallLogFrame = -1000000;

    public static void clearForTests() {
        lastStallLogFrame = -1000000;
    }

    private static void noteArmyStall(HasPosition center) {
        try {
            if (center == null || Alpha.get().size() <= 0) return;
            if (A.now() - lastStallLogFrame < IdleWatch.IDLE_FRAMES * 2) return;

            int stillMin = Integer.MAX_VALUE;
            for (AUnit unit : Alpha.get().selection().list()) {
                if (unit == null || !unit.isAlive() || !unit.isCombatUnit()) continue;
                int still = IdleWatch.stillFramesOf(unit.id());
                if (still < stillMin) stillMin = still;
            }
            if (stillMin < IdleWatch.IDLE_FRAMES * 2) return;

            AUnit nearest = Select.enemy().combatUnits().nearestTo(center);
            if (nearest != null) {
                try {
                    if (center.distTo(nearest) < IdleWatch.FOE_FAR_TILES) return;
                } catch (Exception ignored) {
                    return;
                }
            }
            lastStallLogFrame = A.now();
            EventLog.key(LogKey.STALL)
                .param("cx", center.tx())
                .param("cy", center.ty())
                .param("count", Alpha.get().size())
                .param("still", stillMin)
                .log(A.now(), A.minSec());
        } catch (Exception ignored) {
        }
    }
}
