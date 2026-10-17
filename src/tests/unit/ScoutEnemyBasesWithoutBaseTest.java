package tests.unit;

import atlantis.map.scout.ScoutEnemyBasesWithoutBase;
import atlantis.map.scout.ScoutState;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Baseless-base visits need an enemy anchor: with no known enemy base there
 * is no "near the enemy" to scout. Needs a stub world - the enemy-info layer
 * requires game state even to answer "nothing known".
 */
public class ScoutEnemyBasesWithoutBaseTest extends AbstractTestWithWorld {

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

    @AfterEach
    public void resetVisitMemory() {
        ScoutState.reset();
    }

    @Test
    public void noAnchorNoVisit() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertFalse(
                new ScoutEnemyBasesWithoutBase(fake(AUnitType.Protoss_Probe, 10)).applies(),
                "no known enemy base means no baseless locations near it");
        });
    }

    @Test
    public void arrivalRotatesToTheNextSite() {
        FakeUnit scout = fake(AUnitType.Protoss_Probe, 21, 21);
        FakeUnit enemyDepot = fake(AUnitType.Zerg_Hatchery, 116, 68);
        world(5, fakeOurs(scout), fakeEnemies(enemyDepot), () -> {
            if (atlantis.game.A.now() != 3) return;

            ScoutEnemyBasesWithoutBase first = new ScoutEnemyBasesWithoutBase(scout);
            assertTrue(first.applies(), "a known enemy base anchors baseless visits");
            String firstTarget = first.targetKeyForTest();
            assertNotNull(firstTarget);

            // Stand on the first target: arrival marks it visited, so the
            // next pick must be a different site (Wine game: the scout kept
            // returning to the same tiles instead).
            scout.position = atlantis.map.position.APosition.create(
                Integer.parseInt(firstTarget.split(",")[0]) / 32,
                Integer.parseInt(firstTarget.split(",")[1]) / 32);
            ScoutEnemyBasesWithoutBase second = new ScoutEnemyBasesWithoutBase(scout);
            if (second.applies()) {
                assertNotEquals(
                    firstTarget, second.targetKeyForTest(),
                    "an arrived site stays visited - the scout rotates, it does not bounce back");
            }
        });
    }

    @Test
    public void standstillAbandonsTheSite() {
        FakeUnit scout = fake(AUnitType.Protoss_Probe, 21, 21);
        FakeUnit enemyDepot = fake(AUnitType.Zerg_Hatchery, 116, 68);
        // One give-up per site per 240 tracked frames: four nearby sites
        // need ~1000 frames to all be abandoned. applies() runs every frame
        // like the live chain does.
        world(1200, fakeOurs(scout), fakeEnemies(enemyDepot), () -> {
            int now = atlantis.game.A.now();
            boolean applies = new ScoutEnemyBasesWithoutBase(scout).applies();
            if (now == 3) {
                assertTrue(applies, "a known enemy base anchors baseless visits");
            }
            else if (now == 1150) {
                assertFalse(
                    applies,
                    "a scout that never moved in 1100+ frames is not 'about to arrive' - "
                        + "the unarrivable sites are abandoned so the chain falls through");
            }
        });
    }
}
