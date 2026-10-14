package tests.unit;

import atlantis.map.scout.ScoutEnemyBasesWithoutBase;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static tests.unit.AbstractTestWithUnits.fake;
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
}
