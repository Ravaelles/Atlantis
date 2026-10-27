package tests.unit;

import atlantis.config.env.Env;
import atlantis.debug.profiler.RealTime;
import atlantis.game.AGame;
import atlantis.information.decisions.ForceExitWhenDuelUndecidable;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 1Drag_v_1Zeal on OpenBW, 2026-10-21 (four live runs): the slot-3
 * zealot is known, listed AND visible from frame 24, hits us in the
 * first half wall-second, takes ~11k shots for zero kills, and at
 * ~frame 336 the map removes it without a death. From then on there
 * is nothing to engage for 19 wall seconds - the game burns the whole
 * bot cap for a DRAW it already is. The exit fires once the duel is
 * provably over (no units listed, contact proven, nothing killed,
 * past the trigger-setup grace). Only {@code applies()} is pinned
 * here - {@code handle()} ends the game and is proven by a run, never
 * by a stub.
 */
@Tag("production")
public class ForceExitWhenDuelUndecidableTest extends AbstractTestWithWorld {

    /**
     * The dragoon the world built for us. Worlds rebuild the roster per
     * run, so it is looked up, not stored.
     */
    private static FakeUnit dragoon() {
        return (FakeUnit) atlantis.units.select.Select.ourCombatUnits().first();
    }

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
    public void resetStatics() {
        Env.setLocalForTest(false);
        AUnit._totalHitCount = 0;
        RealTime.gameStarted = RealTime.currentTimestamp();
    }

    private void oldUmsLocalGame() {
        Env.setLocalForTest(true);
        // AGame is mockStatic'd in tests: stub the seam, the real setter
        // never runs here (live it is the only writer).
        aGame.when(AGame::isUms).thenReturn(true);
        RealTime.gameStarted = RealTime.currentTimestamp() - 20000;
    }

    @Test
    public void removedAttackerPastGraceTripsTheExit() {
        oldUmsLocalGame();

        world(50, fakeOurs(
            fake(AUnitType.Protoss_Dragoon, 20, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 24) return;

            dragoon().increaseHitCount();
            assertTrue(
                new ForceExitWhenDuelUndecidable().applies(),
                "no enemy units listed, contact proven, nothing killed, "
                    + "past the grace: the duel is over, waiting only burns the cap");
        });
    }

    @Test
    public void listedEnemiesNeverTripTheExit() {
        oldUmsLocalGame();

        world(50, fakeOurs(
            fake(AUnitType.Protoss_Dragoon, 20, 20)
        ), fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 25, 20)
        ), () -> {
            if (atlantis.game.A.now() != 24) return;

            dragoon().increaseHitCount();
            assertFalse(
                new ForceExitWhenDuelUndecidable().applies(),
                "a listed enemy is engageable by construction - even a cloaked "
                    + "one with a known owner stays listed and blocks this exit");
        });
    }

    @Test
    public void withoutContactTheExitStaysShut() {
        oldUmsLocalGame();

        world(50, fakeOurs(
            fake(AUnitType.Protoss_Dragoon, 20, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 24) return;

            assertFalse(
                new ForceExitWhenDuelUndecidable().applies(),
                "no hits taken is 'enemy not met yet', not an undecidable duel");
        });
    }

    @Test
    public void ourKillsMeanARealFight() {
        oldUmsLocalGame();

        world(50, fakeOurs(
            fake(AUnitType.Protoss_Dragoon, 20, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 24) return;

            FakeUnit dragoon = dragoon();
            dragoon.increaseHitCount();
            dragoon.setKillCount(2);
            assertFalse(
                new ForceExitWhenDuelUndecidable().applies(),
                "a wiped wave with reinforcements on the way is a real fight, "
                    + "never this exit");
        });
    }

    @Test
    public void youngGameNeverTripsTheExit() {
        Env.setLocalForTest(true);
        aGame.when(AGame::isUms).thenReturn(true);

        world(50, fakeOurs(
            fake(AUnitType.Protoss_Dragoon, 20, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 24) return;

            dragoon().increaseHitCount();
            assertFalse(
                new ForceExitWhenDuelUndecidable().applies(),
                "trigger setup and scripted removals run in the first seconds - "
                    + "absent data must never trip an exit (the M&M lesson)");
        });
    }

    @Test
    public void nonlocalRunNeverTripsTheExit() {
        // Explicit false: setUp() re-reads the repo ENV (LOCAL=true) before
        // every test, so relying on the default would test the ENV file.
        Env.setLocalForTest(false);
        aGame.when(AGame::isUms).thenReturn(true);
        RealTime.gameStarted = RealTime.currentTimestamp() - 20000;

        world(50, fakeOurs(
            fake(AUnitType.Protoss_Dragoon, 20, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 24) return;

            dragoon().increaseHitCount();
            assertFalse(
                new ForceExitWhenDuelUndecidable().applies(),
                "the exit is a local-run bound, like its sibling");
        });
    }

    @Test
    public void meleeRunNeverTripsTheExit() {
        Env.setLocalForTest(true);
        RealTime.gameStarted = RealTime.currentTimestamp() - 20000;

        world(50, fakeOurs(
            fake(AUnitType.Protoss_Dragoon, 20, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 24) return;

            dragoon().increaseHitCount();
            assertFalse(
                new ForceExitWhenDuelUndecidable().applies(),
                "melee hides armies in fog all the time - this exit is UMS-only");
        });
    }
}
