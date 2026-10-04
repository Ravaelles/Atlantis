package tests.acceptance.protoss;

import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.protoss.ProtossDynamicUnitProductionCommander;
import atlantis.production.dynamic.protoss.units.ProduceDragoon;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.AUnitType;
import atlantis.util.Counter;
import atlantis.util.Options;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;
import tests.fakes.FakeUnitData;

import static atlantis.units.AUnitType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * With a rich mid-game economy - 700 minerals, 300 gas, 110/116 supply - the
 * Protoss dynamic unit production has to actually order something. Both tests
 * below pin the same situation from two angles:
 *
 * <ol>
 * <li>{@link ProduceDragoon#dragoon()} on its own, and</li>
 * <li>the whole {@link ProtossDynamicUnitProductionCommander}, i.e. that it
 * {@link ProtossDynamicUnitProductionCommander#applies()} and that a
 * forced {@code handle()} reaches the dragoon production.</li>
 * </ol>
 *
 * A Dragoon (125 minerals / 50 gas) is the cheapest thing that could come out of
 * this economy, and it is the one the commander tries first, so "a Dragoon was
 * ordered" is the weakest possible claim that can still fail.
 *
 * <p>Note the supply: {@code options} is read by {@code MockEverything} during
 * {@code @BeforeEach}, i.e. <b>before</b> the test method runs, so setting
 * {@code options} here alone leaves {@code A.supplyUsed()} at 0. The supply has
 * to be applied with {@code initSupply(...)} inside the world, which is what the
 * tests below do.</p>
 */
public class ProtossDynamicUnitProductionTest extends WorldStubForTests {

    private static final int MINERALS = 700;
    private static final int GAS = 300;
    private static final int SUPPLY_USED = 110;
    private static final int SUPPLY_TOTAL = 116;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    // =========================================================

    @Test
    public void produceDragoonSaysYesWith700MineralsAnd300Gas() {
        richEconomy();

        world(1, richOurs(), fakeEnemies(), () -> {
            initSupply(SUPPLY_USED, SUPPLY_TOTAL);
            assertEquals(SUPPLY_USED, A.supplyUsed(), "the supply the world was set up with");

            int trained = FakeUnitData.TRAIN.size();

            boolean produced = ProduceDragoon.dragoon();

            assertTrue(produced,
                "ProduceDragoon.dragoon() should produce a Dragoon at " + MINERALS + "/" + GAS
                    + " minerals/gas, supply " + SUPPLY_USED + "/" + SUPPLY_TOTAL);
            assertEquals(1, FakeUnitData.TRAIN.size() - trained);
            assertEquals(Protoss_Dragoon, FakeUnitData.TRAIN.get(FakeUnitData.TRAIN.size() - 1));
        });
    }

    @Test
    public void commanderAppliesAndProducesADragoon() {
        richEconomy();

        world(7, richOurs(), fakeEnemies(), () -> {
            initSupply(SUPPLY_USED, SUPPLY_TOTAL);

            ProtossDynamicUnitProductionCommander commander = new ProtossDynamicUnitProductionCommander();

            assertTrue(commander.applies(),
                "The Protoss dynamic unit production commander has to apply when we are Protoss");

            int trained = FakeUnitData.TRAIN.size();

            // handle() only runs on every 7th frame, so give it a world long
            // enough to reach one and assert there.
            commander.forceHandle();

            if (A.now() != 7) return;

            Counter<AUnitType> produced = producedSince(trained);
            assertTrue(produced.getValueFor(Protoss_Dragoon) > 0,
                "The commander should have produced a Dragoon (got " + produced.keys()
                    + ", reason: " + ProtossDynamicUnitProductionCommander.reason + ")");
        });
    }

    /**
     * B-22 follow-up: reserving minerals for a Nexus we are about to expand
     * with must not stop us from making combat units, as long as there are
     * minerals to go around.
     *
     * <p>The report this reproduces: at 4:13 a Nexus is added to the queue and
     * from then on no more Dragoons, ever. The suspected mechanism is
     * {@link ReservedResources} - the Nexus reserves 400 minerals, and
     * {@code freeToSpendResources()} answers
     * {@code reservedMinerals > 0 && !A.hasMinerals(150 + reservedMinerals)}
     * with "no". With 400 reserved that needs 550 minerals to get past, and with
     * 500 reserved (the {@code MAX_VALUE} clamp) it needs 650. A Nexus costs 400,
     * so at 800 minerals the bot is sitting on twice what the base costs and
     * still refuses to build.</p>
     *
     * <p>The reservation is set up the way the real one happens: a NOT_STARTED
     * Nexus in {@link ConstructionRequests} (which is what lets
     * {@code ReservedResources} keep more than
     * {@code MAX_VALUE_WITHOUT_BASE}) plus the minerals it costs.</p>
     *
     * <p>Measured in this world, so the test is not just asserting what the code
     * happens to do: at supply 26 the queueing alone brings
     * {@code ReservedResources} to 500, so the {@code MissingMinerals} branch
     * wants {@code 150 + 410 = 560} minerals before it will spend anything.
     * From 520 minerals up that branch is never even reached - the earlier
     * {@code A.hasMinerals(500)} check answers "yes" first, so the bot streams
     * units and the reservation is irrelevant. Between 400 and 500 minerals the
     * reservation is what stops it.</p>
     *
     * <p>So: at the reported 800 minerals and 116 supply this test passes, and
     * that is the answer to the question - a reserved Nexus by itself does not
     * silence a rich Protoss. The values that do expose the branch are the
     * 400-500 band at low supply, and that is the band worth a follow-up.</p>
     */
    @Test
    public void producesDragoonsEvenWithMineralsReservedForANexus() {
        richEconomy();
        ReservedResources.reset();
        ConstructionRequests.constructions.clear();

        final int reservedForNexus = Protoss_Nexus.mineralPrice();

        world(7, richOurs(), fakeEnemies(), () -> {
            initSupply(SUPPLY_USED, SUPPLY_TOTAL);

            if (A.now() == 7) {
                // The state "we asked for a natural": a Nexus that has not
                // started yet, and the minerals it will take reserved.
                ConstructionRequests.constructions.add(new Construction(Protoss_Nexus));
                ReservedResources.reserveMinerals(reservedForNexus, "nexus");

                assertEquals(reservedForNexus, ReservedResources.minerals(),
                    "a queued Nexus reserves its own mineral price");

                int trained = FakeUnitData.TRAIN.size();

                new ProtossDynamicUnitProductionCommander().forceHandle();

                Counter<AUnitType> produced = producedSince(trained);
                assertTrue(produced.getValueFor(Protoss_Dragoon) > 0,
                    MINERALS + " minerals and a Nexus costing only " + reservedForNexus
                        + " reserved should still produce Dragoons (got " + produced.keys()
                        + ", reason: " + ProtossDynamicUnitProductionCommander.reason + ")");
            }
        });
    }

    // =========================================================

    private void richEconomy() {
        currentMinerals = MINERALS;
        currentGas = GAS;
        options = Options.create()
                .set("supplyUsed", SUPPLY_USED)
                .set("supplyTotal", SUPPLY_TOTAL);
    }

    /**
     * A mid-game Protoss base with what the dragoon production insists on: a
     * free Gateway to train in and a Cybernetics Core to research with. The
     * supply numbers are set independently of the units, like they are in a real
     * game - 110/116 is a full book of Pylons.
     */
    private FakeUnit[] richOurs() {
        return fakeOurs(
            fake(Protoss_Nexus, 10),
            fake(Protoss_Pylon, 11),
            fake(Protoss_Pylon, 12),
            fake(Protoss_Pylon, 13),
            fake(Protoss_Pylon, 14),
            fake(Protoss_Pylon, 15),
            fake(Protoss_Gateway, 16),
            fake(Protoss_Gateway, 17),
            fake(Protoss_Cybernetics_Core, 18),
            fake(Protoss_Templar_Archives, 19),
            fake(Protoss_Assimilator, 20),
            fake(Protoss_Photon_Cannon, 21),
            fake(Protoss_Probe, 22),
            fake(Protoss_Probe, 23),
            fake(Protoss_Zealot, 24),
            fake(Protoss_Dragoon, 25)
        );
    }

    private Counter<AUnitType> producedSince(int trainedBefore) {
        Counter<AUnitType> produced = new Counter<>();
        for (int i = trainedBefore; i < FakeUnitData.TRAIN.size(); i++) {
            produced.incrementValueFor(FakeUnitData.TRAIN.get(i));
        }
        return produced;
    }
}