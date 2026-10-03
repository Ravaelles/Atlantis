package tests.acceptance;

import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.Strategy;
import atlantis.production.dynamic.terran.tech.TerranInfantryWeapons;
import atlantis.util.Options;
import bwapi.UpgradeType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeResearch;
import tests.fakes.FakeUnit;

import static atlantis.units.AUnitType.Terran_Barracks;
import static atlantis.units.AUnitType.Terran_Command_Center;
import static atlantis.units.AUnitType.Terran_Marine;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What our player has researched is a property of the world, and a test has to be
 * able to say it - which it could not until {@code ATech.Source} replaced the
 * static mock of the whole {@code ATech} facade. That mock answered
 * {@code getUpgradeLevel(anything)} with 0 for every test in the suite, so the
 * upgrade levels a Terran bio build is supposed to react to were unreachable.
 *
 * <p>Both assertions are about the same world: two bases, twelve marines, bio, and
 * 1000/500 in the bank. Only the researched level differs, and it is the only
 * thing that can flip the answer - which is what makes the second one a test of
 * the port rather than of the doctrine's other conditions.</p>
 */
public class TerranInfantryWeaponsTest extends WorldStubForTests {
    @Test
    public void infantryWeaponsUpgradeIsPlannedOnceThereAreTwelveMarines() {
        world(1, bioWithTwelveMarines(), fakeExampleEnemies(), () -> {
            bioWithAQueue();

            assertEquals(0, TerranInfantryWeapons.upgradeLevel(),
                "the stub world starts with nothing researched");

            assertTrue(new TerranInfantryWeapons().applies(),
                "two bases, twelve marines, bio and 550/250 in the bank: the upgrade "
                    + "is worth starting");
        });
    }

    @Test
    public void maxedUpgradeLevelsStopTheInfantryWeaponsUpgrade() {
        FakeResearch.withUpgradeLevel(TerranInfantryWeapons.what(), 3);

        world(1, bioWithTwelveMarines(), fakeExampleEnemies(), () -> {
            bioWithAQueue();

            assertEquals(3, TerranInfantryWeapons.upgradeLevel(),
                "production reads the level the test declared");

            assertFalse(new TerranInfantryWeapons().applies(),
                "the doctrine only plans levels 0-2, and the level is what says so - "
                    + "with the same world as the test above");
        });
    }

    // =========================================================

    /**
     * The doctrine asks whether the upgrade is already planned, which means a
     * queue has to exist - so the harness' own build order is initialised first
     * and only the strategy is swapped for a fresh bio one. Reusing a static
     * strategy singleton and calling setGoingBio() on it would hand "we go bio"
     * to every later test in the JVM.
     */
    private void bioWithAQueue() {
        initQueue();

        // Same build order as the harness' own test strategy - Strategy.setTo()
        // re-initialises the queue from the file named after the strategy - but a
        // fresh object, so "we go bio" does not outlive this test.
        Strategy.setTo(new AStrategy()
            .setTerran()
            .setGoingBio()
            .setName("Terran strategy for Tests"));
    }

    private FakeUnit[] bioWithTwelveMarines() {
        currentMinerals = 1000;
        currentGas = 500;

        FakeUnit[] our = new FakeUnit[2 + 2 + 12];
        our[0] = fake(Terran_Command_Center, 10);
        our[1] = fake(Terran_Command_Center, 30);
        our[2] = fake(Terran_Barracks, 12);
        our[3] = fake(Terran_Barracks, 14);
        for (int i = 0; i < 12; i++) {
            our[4 + i] = fake(Terran_Marine, 20 + i);
        }

        return fakeOurs(our);
    }
}