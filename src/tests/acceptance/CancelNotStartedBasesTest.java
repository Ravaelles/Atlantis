package tests.acceptance;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.dynamic.expansion.decision.CancelNotStartedBases;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.information.enemy.UnitsArchive;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * B-23: the "a new base appeared, drop the rest" pass drops the rest - and only the
 * rest.
 *
 * <p>Reported from a game: a Nexus NATURAL queued at 4:13 was cancelled at ~5:00 with
 * exactly that reason, and queued again at 5:15. The rule now is the owner's: look at
 * everything unfinished of the base type (planned, rising, caught mid-build) and
 * cancel only when there are <b>two or more</b>, keeping the oldest of the ones nobody
 * has started on and never touching a construction that has a builder on it.</p>
 *
 * <p>Four tests are the four halves of that sentence, and two more cover the
 * precondition that came out of the same entry: a base that finishes right after we lost
 * one is a <i>rebuild</i>, not an expansion, and nothing was gained - see
 * {@link atlantis.information.enemy.UnitsArchive#lastTimeOurBaseDiedLessThanAgo(int)}.</p>
 */
public class CancelNotStartedBasesTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void aSinglePendingBaseSurvives() {
        world(1, ourWorld(), enemies(), () -> {
            queue = initQueue();
            queuePlannedBase();

            CancelNotStartedBases.cancelNotStartedBases(
                fake(Protoss_Nexus, 10, 10), "New base created, remove not started ones");

            assertEquals(1, pendingBases(),
                "one unfinished base is not 'the rest' - this is the churn that was reported");
        });
    }

    @Test
    public void ofTwoPendingBasesOnlyTheNewerIsDropped() {
        world(1, ourWorld(), enemies(), () -> {
            queue = initQueue();
            queuePlannedBase();
            queuePlannedBase();

            CancelNotStartedBases.cancelNotStartedBases(
                fake(Protoss_Nexus, 10, 10), "New base created, remove not started ones");

            assertEquals(1, pendingBases(),
                "two pending bases is exactly when there is a redundant one");
        });
    }

    @Test
    public void aBaseThatIsAlreadyBeingBuiltSurvivesEvenWithAPendingOne() {
        FakeUnit natural = risingNatural(30);

        world(1, ourWorld(natural), enemies(), () -> {
            queue = initQueue();
            ProductionOrder risingOrder = new ProductionOrder(Protoss_Nexus, 0);
            risingOrder.setConstruction(natural.construction());
            risingOrder.setStatus(OrderStatus.IN_PROGRESS);
            Queue.get().addNew(0, risingOrder);
            queuePlannedBase();

            CancelNotStartedBases.cancelNotStartedBases(
                fake(Protoss_Nexus, 10, 10), "New base created, remove not started ones");

            assertEquals(1, pendingBases(),
                "the one nobody started on goes; the one with a builder stays");
            assertEquals(30, natural.hpPercent(), "still at the same hit points");
            assertEquals(ConstructionOrderStatus.IN_PROGRESS, natural.construction().status(),
                "and its construction is still going up");
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
                fake(Protoss_Nexus, 10, 10), "Cancel base - much weaker");

            assertEquals(0, pendingBases(),
                "the aggressive policy still drops a half-built base");
        });
    }

    @Test
    public void aRebuildOfABaseWeLostDoesNotPruneAnything() {
        world(1, ourWorld(), enemies(), () -> {
            queue = initQueue();
            queuePlannedBase();
            queuePlannedBase();

            // We lost a base a moment ago and now one finished. That is not "we have
            // enough bases": it is what we had before, back again - which is the half of
            // B-23 the report could not decide, because from inside this pass the two
            // look identical.
            UnitsArchive.markUnitAsDestroyed(fake(Protoss_Nexus, 70, 10));

            CancelNotStartedBases.cancelNotStartedBases(
                fake(Protoss_Nexus, 10, 10), "New base created, remove not started ones");

            assertEquals(2, pendingBases(),
                "a rebuilt base is not a new base, so the premise of this pass is false");
        });
    }

    @Test
    public void aBaseThatFinishesWithoutALostBaseStillPrunes() {
        world(1, ourWorld(), enemies(), () -> {
            queue = initQueue();
            queuePlannedBase();
            queuePlannedBase();

            // The other half of the same sentence: the precondition above must not have
            // turned the pass off. No base was lost here, so two pending bases is still
            // one redundant base.
            CancelNotStartedBases.cancelNotStartedBases(
                fake(Protoss_Nexus, 10, 10), "New base created, remove not started ones");

            assertEquals(1, pendingBases());
        });
    }

    @Test
    public void aTenPercentWarpSurvivesWhenTheSecondWarpStarts() {
        // The exact shape of the 09:59:30 Tomas Vajda log (2026-10-05): two Nexus
        // orders queued minutes apart (4:13, 4:16), the first warp rising at ~10% HP,
        // then a second warp starts - and the pass, triggered by the *second* warp,
        // cancelled the *first* one:
        //   "7592 Cancelling pending base At 30 Nexus NATURAL* (IN_PROGRESS)(#172),
        //    Reason: New base created, remove not started ones"
        // three frames later the first warp was dead in unit_events.csv.
        // That jar predated this whole file (built 16:53, the fix landed 19:14), so the
        // game re-proved the old aggressive rule - but the shape is pinned here so no
        // future "not started" reading can take a rising base again.
        FakeUnit firstWarp = risingNatural(10);
        FakeUnit secondWarp = risingNatural(0);

        world(1, ourWorld(firstWarp, secondWarp), enemies(), () -> {
            queue = initQueue();

            ProductionOrder firstOrder = new ProductionOrder(Protoss_Nexus, 30);
            firstOrder.setConstruction(firstWarp.construction());
            firstOrder.setStatus(OrderStatus.IN_PROGRESS);
            Queue.get().addNew(0, firstOrder);

            ProductionOrder secondOrder = new ProductionOrder(Protoss_Nexus, 30);
            secondOrder.setConstruction(secondWarp.construction());
            secondOrder.setStatus(OrderStatus.IN_PROGRESS);
            Queue.get().addNew(0, secondOrder);

            // The trigger is the second warp, exactly as OnOurUnitCreated fires it:
            // ProtossWarping has already linked it and marked it IN_PROGRESS.
            CancelNotStartedBases.cancelNotStartedBases(
                secondWarp, "New base created, remove not started ones");

            assertEquals(2, pendingBases(),
                "a warp at 10% HP is not 'not started' - it is the natural, rising");
            assertEquals(ConstructionOrderStatus.IN_PROGRESS, firstWarp.construction().status(),
                "and its construction was not touched");
            assertTrue(firstWarp.isAlive(),
                "cancelling its construction would kill the warp in the engine, "
                    + "which is how the logged base died three frames later");
        });
    }

    // =========================================================

    /**
     * What "not started" looks like in production: an order that is ready to produce,
     * and a construction with a position but no unit on it yet. (The pass iterates
     * {@code statusNotReady()}, which is the double negative - orders whose status is
     * <i>not</i> NOT_READY - so an order already flagged NOT_READY is out of scope.)
     */
    private void queuePlannedBase() {
        Construction planned = new Construction(Protoss_Nexus);
        planned.setStatus(ConstructionOrderStatus.NOT_STARTED);
        planned.setPositionToBuild(atlantis.map.position.APosition.create(50, 10));

        ProductionOrder order = new ProductionOrder(Protoss_Nexus, 0);
        order.setConstruction(planned);
        Queue.get().addNew(0, order);
        // addNew refreshes, and a refresh would decide this order is NOT_READY
        // (nothing in this world can afford a Nexus) - out of scope for the pass. The
        // status is the state under test, so it goes last.
        order.setStatus(OrderStatus.READY_TO_PRODUCE);
    }

    private int pendingBases() {
        Queue.get().clearCache();
        return Queue.get().notFinished().ofType(Protoss_Nexus).size();
    }

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