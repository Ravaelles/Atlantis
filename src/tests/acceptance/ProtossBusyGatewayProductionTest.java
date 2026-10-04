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
 * B-22's real-game shape: gateways the engine never calls idle.
 *
 * <p>Measured across the five games in {@code ~/.scbw/games} (2026-10-05): every one of
 * them produced one zealot and one dragoon - or two zealots - and then <b>no combat unit
 * at all</b> for the remaining 4-8 minutes, with 640-1368 gas gathered in four of the
 * five. Resources are ruled out by that; the only gate left that can stop both producers
 * at once is gateway capacity, and the bot read it as "how many gateways are idle".
 *
 * <p>So this is the state the fix is about: {@code busy = true} on every gateway, which
 * is what the harness calls "not free". The production used to return before it ever
 * asked the engine, and it did so silently, which is why the report was a week old.</p>
 */
public class ProtossBusyGatewayProductionTest extends WorldStubForTests {

    private final ArrayList<FakeUnit> spawned = new ArrayList<>();

    @Test
    public void busyGatewaysStillGetAskedToTrain() {
        currentMinerals = 800;
        currentGas = 300;

        // Every gateway, not one: with a single free gateway the old gates let production
        // through and the test would pass for the wrong reason (checked).
        FakeUnit firstGateway = fake(Protoss_Gateway, 12);
        firstGateway.busy = true;
        FakeUnit secondGateway = fake(Protoss_Gateway, 13);
        secondGateway.busy = true;

        FakeUnit[] ours = fakeOurs(
            fake(Protoss_Nexus, 10),
            fake(Protoss_Pylon, 11),
            firstGateway,
            secondGateway,
            fake(Protoss_Cybernetics_Core, 14),
            fake(Protoss_Assimilator, 15),
            fake(Protoss_Probe, 16),
            fake(Protoss_Zealot, 17),
            fake(Protoss_Dragoon, 18)
        );

        FakeUnit[] enemies = fakeEnemies(
            fakeEnemy(Terran_Marine, 40),
            fakeEnemy(Terran_SCV, 41)
        );

        world(200, ours, enemies, () -> {
            if (A.now() == 1) {
                initSupply(40, 60);
            }
            else {
                (new ProtossDynamicUnitProductionCommander()).forceHandle();
                finishWhatWasJustOrdered();
            }
        });

        assertTrue(trainedCombatUnits() >= 2,
            "no gateway is idle, and that must not be the end of unit production - "
                + "ordered: " + orderedSoFar());
    }

    // =========================================================

    /**
     * The stub world finishes a training when the test says so; otherwise the gateway
     * would be busy forever anyway, which would make this test pass for the wrong reason.
     * Only what the bot ordered is materialised.
     */
    private void finishWhatWasJustOrdered() {
        while (spawned.size() < FakeUnitData.TRAIN.size()) {
            AUnitType type = FakeUnitData.TRAIN.get(spawned.size());
            spawned.add(fake(type, 25 + spawned.size() * 0.5));

            ArrayList<FakeUnit> ourUnits = new ArrayList<>();
            ourUnits.add(fake(Protoss_Nexus, 10));
            ourUnits.add(fake(Protoss_Pylon, 11));
            ourUnits.add(fake(Protoss_Gateway, 12));
            ourUnits.add(fake(Protoss_Cybernetics_Core, 14));
            ourUnits.add(fake(Protoss_Assimilator, 15));
            ourUnits.add(fake(Protoss_Probe, 16));
            ourUnits.add(fake(Protoss_Zealot, 17));
            ourUnits.add(fake(Protoss_Dragoon, 18));
            ourUnits.addAll(spawned);

            DynamicMockOurUnits.mockOur(ourUnits);
            Queue.get().refresh();
        }
    }

    private int trainedCombatUnits() {
        int combat = 0;
        for (AUnitType type : FakeUnitData.TRAIN) {
            if (type == Protoss_Zealot || type == Protoss_Dragoon) combat++;
        }
        return combat;
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