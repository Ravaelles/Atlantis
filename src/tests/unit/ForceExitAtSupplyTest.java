package tests.unit;

import atlantis.config.env.Env;
import atlantis.production.ForceExitAtSupply;
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
 * Debug-run horizon, not a hard limit (owner 2026-10-19): the game ends
 * once our supply reaches the configured level, because defects already
 * show by supply 25-30 and long games only grow the log. Only
 * {@code applies()} is pinned here - {@code handle()} ends the game and
 * is proven by a run, never by a stub.
 */
@Tag("production")
public class ForceExitAtSupplyTest extends AbstractTestWithWorld {

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
    public void resetFlag() {
        Env.setForceEndAtSupplyForTest(0);
    }

    @Test
    public void endsAtHorizon() {
        currentSupplyUsed = 50;
        currentSupplyTotal = 60;
        Env.setForceEndAtSupplyForTest(50);

        world(60, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 30) return;

            assertTrue(new ForceExitAtSupply().applies(),
                "supply 50 with horizon 50 ends the debug run");
        });
    }

    @Test
    public void playsOnBelowHorizon() {
        currentSupplyUsed = 49;
        currentSupplyTotal = 60;
        Env.setForceEndAtSupplyForTest(50);

        world(60, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 30) return;

            assertFalse(new ForceExitAtSupply().applies(),
                "supply 49 keeps playing");
        });
    }

    @Test
    public void disabledByDefault() {
        currentSupplyUsed = 200;
        currentSupplyTotal = 200;
        Env.setForceEndAtSupplyForTest(0);

        world(60, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 30) return;

            assertFalse(new ForceExitAtSupply().applies(),
                "flag 0 disables the horizon - normal games unaffected");
        });
    }
}
