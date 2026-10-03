package tests.unit.orders;

import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stage D: every engine order issued through {@code AUnitOrders} wrappers
 * must reach the unit's {@code OrderSink}.
 */
public class OrderSinkTest extends WorldStubForTests {

    @Test
    void unitsUseLiveSinkByDefault() {
        FakeUnit marine = new FakeUnit(AUnitType.Terran_Marine, 20, 20);

        // FakeUnit replaces it with a recording fake in its constructor.
        assertTrue(
            marine.orderSink() instanceof FakeOrderSink,
            "Fake units must record orders instead of touching the engine"
        );
    }

    @Test
    void holdPositionIsRoutedThroughTheSink() {
        FakeUnit marine;
        FakeUnit[] ours = fakeOurs(
            marine = fake(AUnitType.Terran_Marine, 20)
        );
        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Zergling, 40)
        );

        FakeOrderSink sink = (FakeOrderSink) marine.orderSink();

        world(1, ours, enemies, () -> {
        assertTrue(marine.holdPosition(Actions.HOLD_POSITION, "test"));

        assertEquals(1, sink.orders().size());
        assertEquals("holdPosition", sink.orders().get(0).operation);
        assertEquals(marine.id(), sink.orders().get(0).actorId);
        });
    }

    /**
     * stop() and lift() used to answer "true" in a test without reaching the
     * sink, so a test could not see that the order had been issued at all. They
     * route through it now. Two units, because a command issued in the current
     * frame is refused by the next one ({@code lastCommandIssuedAgo() <= 1}) -
     * a real game rule, not something to route around.
     */
    @Test
    void stopAndLiftAreRoutedThroughTheSink() {
        FakeUnit marine;
        FakeUnit marine2;
        FakeUnit[] ours = fakeOurs(
            marine = fake(AUnitType.Terran_Marine, 20),
            marine2 = fake(AUnitType.Terran_Marine, 21)
        );
        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Zergling, 40)
        );

        FakeOrderSink sink = (FakeOrderSink) marine.orderSink();
        FakeOrderSink sink2 = (FakeOrderSink) marine2.orderSink();

        world(1, ours, enemies, () -> {
            assertTrue(marine.stop("test"), "stop reports the order as issued");
            assertTrue(marine2.lift(), "lift reports the order as issued");

            assertEquals(1, sink.orders().size());
            assertEquals("stop", sink.orders().get(0).operation);
            assertEquals(marine.id(), sink.orders().get(0).actorId);

            assertEquals(1, sink2.orders().size());
            assertEquals("lift", sink2.orders().get(0).operation);
            assertEquals(marine2.id(), sink2.orders().get(0).actorId);
        });
    }

    @Test
    void attackUnitIsRoutedThroughTheSink() {
        FakeUnit marine;
        FakeUnit zergling;
        FakeUnit[] ours = fakeOurs(
            marine = fake(AUnitType.Terran_Marine, 20)
        );
        FakeUnit[] enemies = fakeEnemies(
            zergling = fake(AUnitType.Zerg_Zergling, 40)
        );

        FakeOrderSink sink = (FakeOrderSink) marine.orderSink();

        world(1, ours, enemies, () -> {
        assertTrue(marine.attackUnit(zergling));

        assertEquals(1, sink.orders().size());
        assertEquals("attackUnit", sink.orders().get(0).operation);
        assertEquals(marine.id(), sink.orders().get(0).actorId);
        });
    }
}
