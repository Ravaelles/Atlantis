package tests.unit.sim;

import bwapi.Race;
import tests.unit.AbstractTestWithUnits;

/**
 * Standalone bootstrap for the sim batch runner: the same world the
 * JUnit sim scenarios get (game mock, AGame clock, race configs,
 * fake ports), installed outside JUnit. {@code SimTunerBatch.main}
 * calls {@code install()} once; the in-suite test gets it from the
 * harness instead, so {@code run()} stays harness-agnostic.
 */
public final class SimBatchBootstrap extends AbstractTestWithUnits {

    private SimBatchBootstrap() {
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    public Race initEnemyRace() {
        return Race.Protoss;
    }

    public static void install() {
        new SimBatchBootstrap().setUp();
    }
}
