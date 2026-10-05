package tests.acceptance;

import atlantis.game.A;
import atlantis.production.dynamic.protoss.ProtossDynamicUnitProductionCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import atlantis.util.Counter;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.fakes.FakeUnitData;
import tests.unit.DynamicMockOurUnits;

import java.util.ArrayList;

import static atlantis.units.AUnitType.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The owner's second report of 2026-10-05: unit production stops again, in real games.
 * His words on what the test has to be: a game with a Nexus, Probes, minerals, and even
 * a Pylon, a Gateway and a Cybernetics Core - asserting at least 4 Dragoons come out.
 *
 * <p>Two halves, because the report has two possible readings and they need different
 * answers. The first half is the owner's setup literally - Nexus, 4 Probes, 800/300, a
 * Pylon, a Gateway, a Core, supply 40/60 - through the real commander, asserting 4+
 * Dragoons in 300 frames. It passes with the B-22 fix reverted too (checked): with idle
 * gateways the old gates let production through, so this half is a characterization of
 * the whole chain, not a pin of the defect - any future breakage of the throttle, the
 * resource gates, the core requirement or the dragoon chain fails it.</p>
 *
 * <p>The second half is the same setup with both gateways busy, the way the engine
 * reports them in the games that stopped producing (B-22: one zealot and one dragoon,
 * then nothing for 4-8 minutes, gas banked). Busy here means {@code busy = true}, which
 * the harness reads as "not free" - and the re-mocked gateways stay busy, otherwise the
 * test would pass the moment the first order materialises. This half fails with the
 * B-22 fix reverted (checked: nothing ordered at all) and passes with it.</p>
 *
 * <p>What neither half is: an OpenBW match. The suite cannot play those yet (that is the
 * open OpenBW stepper in `_AI/IDEA-E2E-TESTS.md` Stage 3), so the pair is these tests
 * plus `unit_events.csv` from real games, which is how B-22 was measured in the first
 * place. The two games of 2026-10-05 ran on a jar that predates the B-22 fix (built
 * 16:53, fix landed 23:28), so they re-proved the old silence rather than a new one.</p>
 */
public class ProtossDragoonProductionTest extends WorldStubForTests {

    private final ArrayList<FakeUnit> spawned = new ArrayList<>();
    private boolean gatewaysBusy;

    @Test
    public void nexusProbesMineralsPylonGatewayAndCoreProduceDragoons() {
        gatewaysBusy = false;
        runProductionWorld();

        assertTrue(dragoonsOrdered() >= 4,
            "a Nexus, 4 Probes, 800/300, a Pylon, a Gateway and a Core have to produce "
                + "at least 4 Dragoons in 300 frames - ordered: " + orderedSoFar());
    }

    @Test
    public void busyGatewaysDoNotStopDragoonProduction() {
        gatewaysBusy = true;
        runProductionWorld();

        assertTrue(dragoonsOrdered() >= 4,
            "gateways the engine calls busy are not a reason to stop - the engine, not "
                + "the idle flag, decides whether a train order lands - ordered: "
                + orderedSoFar());
    }

    // =========================================================

    private void runProductionWorld() {
        currentMinerals = 800;
        currentGas = 300;
        spawned.clear();

        FakeUnit[] ours = fakeOurs(
            fake(Protoss_Nexus, 10),
            fake(Protoss_Probe, 11),
            fake(Protoss_Probe, 12),
            fake(Protoss_Probe, 13),
            fake(Protoss_Probe, 14),
            fake(Protoss_Pylon, 15),
            busyGateway(16),
            fake(Protoss_Cybernetics_Core, 17)
        );

        FakeUnit[] enemies = fakeEnemies(
            fakeEnemy(Terran_Marine, 40)
        );

        world(300, ours, enemies, () -> {
            if (A.now() == 1) {
                initSupply(40, 60);
            }
            else {
                (new ProtossDynamicUnitProductionCommander()).forceHandle();
                finishWhatWasJustOrdered();
            }
        });
    }

    private FakeUnit busyGateway(double x) {
        FakeUnit gateway = fake(Protoss_Gateway, x);
        gateway.busy = gatewaysBusy;
        return gateway;
    }

    /**
     * The stub world finishes a training when the test says so: only what the bot
     * ordered is materialised. The re-mocked gateway keeps the busy flag, otherwise a
     * "busy gateways" test would stop testing busy gateways after the first order.
     */
    private void finishWhatWasJustOrdered() {
        while (spawned.size() < FakeUnitData.TRAIN.size()) {
            AUnitType type = FakeUnitData.TRAIN.get(spawned.size());
            spawned.add(fake(type, 25 + spawned.size() * 0.5));

            ArrayList<FakeUnit> ourUnits = new ArrayList<>();
            ourUnits.add(fake(Protoss_Nexus, 10));
            ourUnits.add(fake(Protoss_Probe, 11));
            ourUnits.add(fake(Protoss_Probe, 12));
            ourUnits.add(fake(Protoss_Probe, 13));
            ourUnits.add(fake(Protoss_Probe, 14));
            ourUnits.add(fake(Protoss_Pylon, 15));
            ourUnits.add(busyGateway(16));
            ourUnits.add(fake(Protoss_Cybernetics_Core, 17));
            ourUnits.addAll(spawned);

            DynamicMockOurUnits.mockOur(ourUnits);
            Queue.get().refresh();
        }
    }

    private int dragoonsOrdered() {
        int dragoons = 0;
        for (AUnitType type : FakeUnitData.TRAIN) {
            if (type == Protoss_Dragoon) dragoons++;
        }
        return dragoons;
    }

    private String orderedSoFar() {
        Counter<AUnitType> counter = new Counter<>();
        for (AUnitType type : FakeUnitData.TRAIN) {
            counter.incrementValueFor(type);
        }

        StringBuilder result = new StringBuilder();
        for (AUnitType type : counter.keys()) {
            result.append(type).append("=").append(counter.getValueFor(type)).append(" ");
        }
        return result.toString();
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}