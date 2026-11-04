package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.builders.CancelConstructionsWhereEnemyIsTooClose;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Empty battlefield must cancel nothing and throw nothing: with no
 * enemies around, the nearest-enemy lookup answers null, and the old
 * code dereferenced it every frame (measured: hundreds of
 * "distanceTo got null" + stack traces in one owner-watched game).
 * The exception path cancelled nothing, so an explicit false is
 * behavior-identical - it only silences the storm.
 */
public class NoEnemyNoCancelTest extends WorldStubForTests {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void hermetic() {
        clearQueue();
    }

    @AfterEach
    public void clean() {
        clearQueue();
    }

    private void clearQueue() {
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void noEnemyNearBuilderCancelsNothing() {
        FakeUnit mason = fake(AUnitType.Protoss_Probe, 38, 31);

        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            mason
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            Construction planned = new Construction(AUnitType.Protoss_Pylon);
            planned.setPositionToBuild(APosition.create(40, 33));

            assertFalse(
                CancelConstructionsWhereEnemyIsTooClose.cancelIfNeeded(mason, planned),
                "no enemy anywhere means no cancel - and no NullPointerException");
        });
    }
}
