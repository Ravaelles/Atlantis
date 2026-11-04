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
 * March cohesion, wired end to end (owner order 2026-11-04): a squad
 * strung out in single file must dress - the front holds while the rear
 * walks up - instead of marching on as a snake. Pins the Regroup gate,
 * not just the pure verdict: without the gate no MARCH_COHESION twin
 * ever fires and the test fails on silence.
 */
@Tag("combat")
@Tag("full-tier")
public class MarchCohesionSimTest extends WorldStubForTests {

    @AfterEach
    public void clean() {
        BattleCommander.clearForTests();
        Alpha.get().clear();
        EventLog.clearForTests();
        atlantis.combat.formation.MarchCohesion.clearForTests();
        Missions.forceGlobalMissionDefend("test-cleanup");
    }

    @Test
    public void strungOutSquadDressesInsteadOfSnaking() throws Exception {
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(fake(AUnitType.Protoss_Nexus, 5, 5));
        // Single file along the march axis, 8 tiles apart each.
        ours.add(fake(AUnitType.Protoss_Zealot, 20, 20));
        ours.add(fake(AUnitType.Protoss_Zealot, 28, 20));
        ours.add(fake(AUnitType.Protoss_Dragoon, 36, 20));
        ours.add(fake(AUnitType.Protoss_Dragoon, 44, 20));
        APosition anchor = APosition.create(120, 20);

        Missions.forceGlobalMissionAttack("cohesion-repro");
        try {
            SimWorld world = new SimWorld(
                ours.toArray(new FakeUnit[0]), new FakeUnit[0],
                new SimArena(130 * 32, 130 * 32));
            try {
                Alpha.gather();
                boolean dressed = false;
                for (int i = 0; i < 1200 && !dressed; i++) {
                    final int tick = i;
                    world.tick(() -> {
                        for (FakeUnit u : ours) {
                            if (!u.type().isCombatUnit()) continue;
                            BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, anchor, u);
                        }
                    });
                    if (tick % 24 == 0 && sawCohesionHold()) {
                        dressed = true;
                    }
                }

                assertTrue(dressed,
                    "a strung-out squad must dress (front holds at the fallback) - "
                        + "no MARCH_COHESION twin in 1200 ticks");
            } finally {
                world.close();
            }
        } finally {
            Missions.forceGlobalMissionDefend("test-cleanup");
        }
    }

    private boolean sawCohesionHold() {
        try {
            return EventLog.toJsonLines().contains("\"key\":\"MARCH_COHESION\"");
        } catch (Exception ignored) {
            return false;
        }
    }
}
