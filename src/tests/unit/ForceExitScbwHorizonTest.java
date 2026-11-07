package tests.unit;

import atlantis.config.env.Env;
import atlantis.debug.profiler.RealTime;
import atlantis.game.A;
import atlantis.information.decisions.ForceExitLocallyAfterSomeTime;
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
 * The scbw slot bounds its runs with FORCE_END_GAME_AFTER_REAL_SECONDS so
 * the match ends through the engine (leaveGame) and the replay is
 * flushed; a crashed game saves nothing. The opt-in is the key's
 * presence, never backend inference: an explicit horizon fires without
 * LOCAL, the same horizon stays silent without it (OpenBW tiers never
 * set the key - their gates own the end). Only {@code applies()} is
 * pinned here - {@code handle()} ends the game and is proven by a run,
 * never by a stub.
 */
@Tag("production")
public class ForceExitScbwHorizonTest extends AbstractTestWithWorld {

    private final ForceExitLocallyAfterSomeTime exit = new ForceExitLocallyAfterSomeTime();

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
        Env.setRealSecondsLimitExplicitForTest(false);
        ForceExitLocallyAfterSomeTime.realSecondsLimit = 60 * 10;
        RealTime.gameStarted = RealTime.currentTimestamp();
    }

    @Test
    public void explicitHorizonFiresWithoutLocal() {
        Env.setLocalForTest(false);
        Env.setRealSecondsLimitExplicitForTest(true);
        ForceExitLocallyAfterSomeTime.realSecondsLimit = 100;
        RealTime.gameStarted = RealTime.currentTimestamp() - 200000;

        world(300, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20)
        ), fakeEnemies(), () -> {
            if (A.now() != 300) return;
            assertTrue(exit.applies(),
                "scbw slot with an explicit horizon ends the run without LOCAL");
        });
    }

    @Test
    public void sameHorizonSilentWithoutOptIn() {
        Env.setLocalForTest(false);
        Env.setRealSecondsLimitExplicitForTest(false);
        ForceExitLocallyAfterSomeTime.realSecondsLimit = 100;
        RealTime.gameStarted = RealTime.currentTimestamp() - 200000;

        world(300, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20)
        ), fakeEnemies(), () -> {
            if (A.now() != 300) return;
            assertFalse(exit.applies(),
                "no opt-in, no exit: OpenBW tiers keep owning their end");
        });
    }

    @Test
    public void localPathUnchanged() {
        Env.setLocalForTest(true);
        Env.setRealSecondsLimitExplicitForTest(false);
        ForceExitLocallyAfterSomeTime.realSecondsLimit = 100;
        RealTime.gameStarted = RealTime.currentTimestamp() - 200000;

        world(300, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20)
        ), fakeEnemies(), () -> {
            if (A.now() != 300) return;
            assertTrue(exit.applies(),
                "LOCAL with a past horizon still ends the run");
        });
    }
}
