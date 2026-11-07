package tests.unit.sim;

import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.Missions;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Median leash on the march (owner order 2026-11-07): a Zealot detached
 * from the ball with no target rejoins the median instead of marching
 * alone to the anchor, slot or ghost the march chain would otherwise
 * hand it. Measured live: one Zealot flapping GATHER/COLLAPSE/DRESS/
 * STANDOFF for 760 frames, never in the ball, ending in FLEE.
 *
 * <p>Pins the outcome, not the mechanism: the straggler must be
 * recalled (COHESION twin), must close on the ball, and must never
 * march east past its start toward the anchor alone.</p>
 */
@Tag("combat")
@Tag("full-tier")
public class MarchStragglerRejoinsSimTest extends WorldStubForTests {

    @AfterEach
    public void clean() {
        BattleCommander.clearForTests();
        Alpha.get().clear();
        EventLog.clearForTests();
        atlantis.combat.cluster.ClusterCohesion.clearForTests();
        atlantis.combat.twins.CohesionTwin.clearForTests();
        atlantis.battle.DuelMemory.clear();
        Missions.forceGlobalMissionDefend("test-cleanup");
    }

    @Test
    public void detachedZealotRejoinsMedianInsteadOfMarchingAlone() throws Exception {
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(fake(AUnitType.Protoss_Nexus, 5, 5));
        // Ball near x=20; the straggler is created LAST so it never
        // draws the vanguard (lowest id scouts ahead by design).
        FakeUnit z1 = fake(AUnitType.Protoss_Zealot, 20, 20);
        FakeUnit z2 = fake(AUnitType.Protoss_Zealot, 21, 21);
        FakeUnit z3 = fake(AUnitType.Protoss_Zealot, 22, 20);
        FakeUnit stray = fake(AUnitType.Protoss_Zealot, 45, 20);
        ours.add(z1);
        ours.add(z2);
        ours.add(z3);
        ours.add(stray);
        APosition anchor = APosition.create(120, 20);

        Missions.forceGlobalMissionAttack("median-leash-repro");
        try {
            SimWorld world = new SimWorld(
                ours.toArray(new FakeUnit[0]), new FakeUnit[0],
                new SimArena(130 * 32, 130 * 32));
            try {
                Alpha.gather();
                double startDist = stray.distTo(Alpha.get().median());
                boolean recalled = false;
                for (int i = 0; i < 1200; i++) {
                    world.tick(() -> {
                        for (FakeUnit u : ours) {
                            if (!u.type().isCombatUnit()) continue;
                            BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, anchor, u);
                        }
                    });
                    if (!recalled && sawRecall(stray.id())) recalled = true;
                }
                double endDist = stray.distTo(Alpha.get().median());

                assertTrue(recalled,
                    "a detached Zealot must be recalled to the ball, not answered"
                        + " with a march point - no COHESION_RECALL twin for #"
                        + stray.id());
                assertTrue(endDist < startDist - 10,
                    "the straggler must close on the ball: start " + (int) startDist
                        + " end " + (int) endDist);
                assertTrue(endDist <= 8,
                    "the straggler must march with the ball, not strung out behind"
                        + " or ahead of it: end " + (int) endDist);
            } finally {
                world.close();
            }
        } finally {
            Missions.forceGlobalMissionDefend("test-cleanup");
        }
    }

    private boolean sawRecall(int unitId) {
        try {
            String log = EventLog.toJsonLines();
            return log.contains("\"key\":\"COHESION_RECALL\"")
                && log.contains("\"unit\":" + unitId);
        } catch (Exception ignored) {
            return false;
        }
    }
}
