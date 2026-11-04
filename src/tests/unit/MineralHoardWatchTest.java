package tests.unit;

import atlantis.production.dynamic.MineralHoardWatch;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner ask: one HIGH line when the bank first crosses 800 minerals in
 * a game, with the demand side next to it - and never again. Below the
 * line it stays silent; the per-game reset re-arms it.
 */
@Tag("production")
public class MineralHoardWatchTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void hermetic() {
        MineralHoardWatch.reset();
    }

    @AfterEach
    public void clean() {
        MineralHoardWatch.reset();
    }

    @Test
    public void silentBelowTheLine() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            currentMinerals = 799;

            assertFalse(MineralHoardWatch.check(),
                "799 is not over the line - silent");
            assertFalse(MineralHoardWatch.check(),
                "still silent: nothing announced, nothing to re-announce");
        });
    }

    @Test
    public void firesOnceThenNeverAgain() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            currentMinerals = 801;

            assertTrue(MineralHoardWatch.check(),
                "first frame over 800: the one HIGH line fires");
            assertFalse(MineralHoardWatch.check(),
                "second call in the same game: never again, no spam");
        });
    }

    @Test
    public void resetRearmsForTheNextGame() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            currentMinerals = 900;

            assertTrue(MineralHoardWatch.check(), "fires once");
            MineralHoardWatch.reset();
            assertTrue(MineralHoardWatch.check(),
                "after a per-game reset the next game announces its own line");
        });
    }
}
