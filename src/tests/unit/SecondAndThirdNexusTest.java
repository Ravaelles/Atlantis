package tests.unit;

import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.production.dynamic.expansion.protoss.ProtossShouldExpand;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Expansion must actually happen: rich decisions have to say yes, and an
 * approved Nexus order has to land in the queue - for the second base and
 * (through different logic) for the third.
 *
 * <p>
 * Measured need (owner): bank hoarded to 600+ at 41/41 with one base - the
 * decision chain, not just the thresholds, has to be pinned so a future
 * break of decision-to-queue shows up here instead of as another hoard log.
 * Threshold VALUES stay tunable (D5); what is pinned is that yes means
 * yes, and approval means queued.
 * </p>
 *
 * <p>
 * Why decision and queue are pinned separately: above 700 minerals every
 * queue add is throttled to 1-in-3 seconds (`tooManyOrdersInGeneral`), while
 * the LimitedBases shortcut needs 777 - so an end-to-end rich test would
 * depend on frame/throttle alignment instead of on the rule. The commander
 * retries every 67th frame in real games, which is how the order lands
 * there; here each half is deterministic on its own.
 * </p>
 */
@Tag("production")
public class SecondAndThirdNexusTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    /**
     * The harness defaults to the Terran race config (BASE = Command Center),
     * so without this every base count reads zero no matter how many Nexuses
     * are queued - the exact trap the first version of these tests fell into.
     */
    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void secondNexusDecidedWhenRich() {
        currentMinerals = 800;
        currentSupplyUsed = 20;
        currentSupplyTotal = 30;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
            fake(AUnitType.Protoss_Probe, 21, 22),
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertTrue(
                ProtossShouldExpand.shouldExpand(),
                "800 minerals on one base must say yes");
            assertEquals(
                "LimitedBases", ShouldExpand.reason,
                "and name the rule that said it");
        });
    }

    @Test
    public void approvedNexusOrderIsQueued() {
        currentMinerals = 500;
        currentSupplyUsed = 20;
        currentSupplyTotal = 30;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ShouldExpand.reason = "LimitedBases";

            assertTrue(
                AddToQueue.withTopPriority(AUnitType.Protoss_Nexus) != null,
                "an approved Nexus order must be accepted");
            assertTrue(
                CountInQueue.bases() >= 1,
                "and visible in the queue - approval without an order"
                    + " is the hoard");
        });
    }

    @Test
    public void thirdNexusDecidedWhenRichAndBig() {
        // genericThird path (supply + strength, no mineral/division traps):
        // LimitedBases would preempt at 777+, HasInProgress needs its own
        // shape - this pins the supply/strength rule saying yes.
        currentMinerals = 700;
        currentSupplyUsed = 130;
        currentSupplyTotal = 150;

        FakeUnit[] ours = new FakeUnit[2 + 42 + 6];
        ours[0] = fake(AUnitType.Protoss_Nexus, 20, 20);
        ours[1] = fake(AUnitType.Protoss_Nexus, 40, 40);
        for (int i = 0; i < 42; i++) {
            ours[2 + i] = fake(AUnitType.Protoss_Probe, 21 + (i % 6), 21 + (i / 6));
        }
        for (int i = 0; i < 6; i++) {
            ours[44 + i] = fake(AUnitType.Protoss_Zealot, 25 + i, 25);
        }

        world(5, fakeOurs(ours), fakeEnemies(fakeEnemy(
            AUnitType.Terran_Marine, 60, 10
        )), () -> {
            if (atlantis.game.A.now() != 3) return;

            // Fresh bank: the stub queue comes pre-seeded and its reservations
            // would otherwise decide this (up to the 500 cap), not the rule.
            atlantis.production.orders.production.queue.ReservedResources.reset();

            assertTrue(
                ProtossShouldExpand.shouldExpand(),
                "42 workers, two bases, strength up, 700 minerals must say"
                    + " yes via the third-base logic");
            assertEquals(
                "LetsDoIt", ShouldExpand.reason,
                "and name the rule that said it");
        });
    }
}
