package tests.acceptance;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.dynamic.expansion.decision.CancelNotStartedBases;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static atlantis.units.AUnitType.Protoss_Gateway;
import static atlantis.units.AUnitType.Protoss_Nexus;
import static atlantis.units.AUnitType.Protoss_Pylon;
import static atlantis.units.AUnitType.Protoss_Probe;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * B-23: "a new base appeared, drop the bases we never started" must not drop the
 * one that is already going up.
 *
 * <p>Reported from a game: a Nexus NATURAL queued at 4:13 was cancelled at ~5:00
 * with the reason "New base created, remove not started ones" - the pass meant to
 * throw away redundant <i>queued</i> expansions threw away one with a builder on it,
 * and the bot queued the same Nexus again at 5:15.</p>
 *
 * <p>The condition behind that message was
 * {@code !hasStarted() || progressPercent() <= 49}: the second half cancels anything
 * up to half built, which is a different policy under a different name - it is what
 * the "much weaker", "critical" and "hidden enemies" callers want, where the
 * minerals are the point. So there are two methods now, and the "new base created"
 * caller asks for the one its reason describes. The third test is there so the
 * aggressive policy cannot quietly lose its teeth either.</p>
 */
public class CancelNotStartedBasesTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void aBaseThatIsAlreadyBeingBuiltSurvivesTheNewBasePass() {
        FakeUnit natural = risingNatural(30);

        world(1, ourWorld(natural), enemies(), () -> {
            queue = initQueue();
            ProductionOrder order = new ProductionOrder(Protoss_Nexus, 0);
            order.setConstruction(natural.construction());
            order.setStatus(OrderStatus.IN_PROGRESS);
            Queue.get().addNew(0, order);

            // The base whose completion triggered the pass: the main.
            CancelNotStartedBases.cancelNotStartedBases(
                fake(Protoss_Nexus, 10), "New base created, remove not started ones");

            assertEquals(1, Queue.get().notFinished().ofType(Protoss_Nexus).size(),
                "a base with a builder on it at 30% is not 'not started'");
            assertEquals(ConstructionOrderStatus.IN_PROGRESS, natural.construction().status(),
                "and its construction is still going up");
            assertEquals(30, natural.hpPercent(),
                "still at the same hit points - nothing was cancelled under it");
        });
    }

    @Test
    public void aBaseWeNeverStartedIsStillDropped() {
        world(1, ourWorld(), enemies(), () -> {
            queue = initQueue();

            // What "not started" looks like in production: an order that is ready to
            // produce, and a construction with a position but no unit on it yet. (The
            // pass looks at `statusNotReady()`, which is the double negative - orders
            // whose status is *not* NOT_READY - so an order already marked NOT_READY
            // was never in scope.)
            Construction planned = new Construction(Protoss_Nexus);
            planned.setStatus(ConstructionOrderStatus.NOT_STARTED);
            planned.setPositionToBuild(atlantis.map.position.APosition.create(50, 10));

            ProductionOrder order = new ProductionOrder(Protoss_Nexus, 0);
            order.setConstruction(planned);
            Queue.get().addNew(0, order);
            // addNew refreshes, and a refresh would decide this order is NOT_READY
            // (nothing in this world can afford a Nexus) - which the pass does not
            // look at. The status is the state under test, so it goes last.
            order.setStatus(OrderStatus.READY_TO_PRODUCE);

            CancelNotStartedBases.cancelNotStartedBases(
                fake(Protoss_Nexus, 10), "New base created, remove not started ones");

            assertEquals(0, Queue.get().notFinished().ofType(Protoss_Nexus).size(),
                "that is the whole point of the doctrine: drop the ones we never started");
        });
    }

    @Test
    public void anEarlyBaseIsStillCancelledByTheAggressivePolicy() {
        FakeUnit natural = risingNatural(30);

        world(1, ourWorld(natural), enemies(), () -> {
            queue = initQueue();
            ProductionOrder order = new ProductionOrder(Protoss_Nexus, 0);
            order.setConstruction(natural.construction());
            order.setStatus(OrderStatus.IN_PROGRESS);
            Queue.get().addNew(0, order);

            // "Cancel base - much weaker", "Critical base cancel",
            // "HiddenEnemiesPressure" and the expansion veto all still want this: those
            // callers give up on an expansion to free minerals or to survive, and half
            // a Nexus is worth something back.
            CancelNotStartedBases.cancelNotStartedOrEarlyBases(
                fake(Protoss_Nexus, 10), "Cancel base - much weaker");

            assertEquals(0, Queue.get().notFinished().ofType(Protoss_Nexus).size(),
                "the aggressive policy still drops a half-built base");
        });
    }

    // =========================================================

    /**
     * A natural at {@code percent} of its hit points, wired the way the game wires a
     * rising building: the unit belongs to the construction, the construction is
     * IN_PROGRESS, and its progress percent is what the aggressive half of the old
     * condition used to read.
     */
    private FakeUnit risingNatural(int percent) {
        FakeUnit natural = fake(Protoss_Nexus, 50, 10).setCompleted(false);
        natural.setHp(natural.type().maxHp() * percent / 100);

        Construction construction = new Construction(Protoss_Nexus);
        construction.setBuildingUnit(natural);
        construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
        natural.setConstruction(construction);

        return natural;
    }

    private FakeUnit[] ourWorld(FakeUnit... extra) {
        FakeUnit[] base = fakeOurs(
            fake(Protoss_Nexus, 10, 10),
            fake(Protoss_Pylon, 11, 10),
            fake(Protoss_Gateway, 12, 10),
            fake(Protoss_Probe, 13, 10)
        );

        FakeUnit[] all = new FakeUnit[base.length + extra.length];
        System.arraycopy(base, 0, all, 0, base.length);
        System.arraycopy(extra, 0, all, base.length, extra.length);
        return all;
    }

    private FakeUnit[] enemies() {
        return fakeEnemies(fakeEnemy(AUnitType.Terran_Marine, 60, 10));
    }
}