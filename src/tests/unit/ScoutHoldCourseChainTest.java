package tests.unit;

import atlantis.combat.scout.ScoutUnitManagers;
import atlantis.map.scout.ScoutCommander;
import atlantis.map.scout.ScoutState;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Chain wiring proof for ScoutHoldCourse (owner: the early scout circled
 * between bases, a different movement manager winning every frame, and
 * never saw the enemy). Through the real ScoutCommander chain the gate
 * claims the in-flight scout (visible in the manager log), and the frames
 * that follow hold one destination with no re-issue. The flip-flop itself
 * is pinned at unit level in ScoutHoldCourseTest (shouldRide matrix); a
 * stub world cannot flip the time-gated managers without faked base
 * knowledge, so this test pins the wiring and the held course instead
 * of pretending otherwise.
 */
@Tag("map")
public class ScoutHoldCourseChainTest extends WorldStubForTests {

    @AfterEach
    public void clearScoutState() {
        ScoutState.scouts.clear();
        ScoutState.scoutsKilledCount = 0;
    }

    @Test
    public void inFlightCourseRidesThroughTheRealChain() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);
        // Far raiders: close enough to arm the third-base watch, far
        // enough that no avoid manager ever fires for them.
        FakeUnit[] raiders = fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 100, 100),
            fake(AUnitType.Protoss_Zealot, 102, 100),
            fake(AUnitType.Protoss_Zealot, 104, 100));

        world(450, fakeOurs(nexus, probe), raiders, () -> {
            if (atlantis.game.A.now() == 1) {
                ScoutState.scoutUnitManager = new ScoutUnitManagers();
                ScoutState.scouts.add(probe);
                probe.setScout(true);
            }
            new ScoutCommander().handle();
        });

        List<FakeOrderSink.Record> moves = ((FakeOrderSink) probe.orderSink()).orders().stream()
            .filter(r -> r.operation.equals("move"))
            .collect(Collectors.toList());
        assertTrue(moves.size() >= 1,
            "the chain issues a scout course at all (harness must scout): "
                + ((FakeOrderSink) probe.orderSink()).orders());
        Set<String> destinations = new HashSet<>();
        for (FakeOrderSink.Record record : moves) destinations.add(record.detail);
        assertEquals(1, destinations.size(),
            "one destination all game, not an alternation: " + destinations);
        assertTrue(probe.managerLogs().toString().contains("ScoutHoldCourse"),
            "the gate claims in the real chain, not just in isolation: "
                + probe.managerLogs());
    }
}
