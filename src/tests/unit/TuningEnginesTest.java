package tests.unit;

import atlantis.debug.tweaker.TuningEngine;
import atlantis.debug.tweaker.TuningEngines;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #160, step 6: the loop addresses engines by name and asks them
 * for paths and flags - wine tunes today, the others fail loudly with
 * their reason instead of pretending.
 */
@Tag("infra")
public class TuningEnginesTest {

    @Test
    public void registryKnowsAllThree() {
        assertEquals(3, TuningEngines.all().size());
        assertTrue(TuningEngines.fromName("wine").isPresent());
        assertTrue(TuningEngines.fromName("openbw").isPresent());
        assertTrue(TuningEngines.fromName("sim").isPresent());
        assertTrue(!TuningEngines.fromName("toaster").isPresent());
        assertTrue(!TuningEngines.fromName(null).isPresent());
    }

    @Test
    public void wineIsTheTuningEngine() {
        TuningEngine wine = TuningEngines.fromName("wine").get();

        assertEquals("scripts/run-wine-batch.sh", wine.batchScript());
        assertEquals("bwapi-data/AI/ENV-WINE-FAST", wine.defaultEnv());
        assertTrue(wine.isLive());
        assertTrue(wine.supportsTuning(), "per-game envs plus readable tweaker rows");
    }

    @Test
    public void openbwPlaysButDoesNotTune() {
        TuningEngine openbw = TuningEngines.fromName("openbw").get();

        assertEquals("scripts/run-openbw-ums.sh", openbw.batchScript());
        assertTrue(openbw.isLive(), "it plays, just not duels");
        assertTrue(!openbw.supportsTuning(),
            "no tweaker rows (killed before OnGameEnd) and no ticking duel map");
    }

    @Test
    public void simIsWired() {
        TuningEngine sim = TuningEngines.fromName("sim").get();

        assertTrue(!sim.isLive(), "no game process at all");
        assertTrue(sim.supportsTuning(), "sim batch runner plays duels in-JVM (SimTunerBatch)");
        assertEquals("scripts/run-sim-batch.sh", sim.batchScript());
        assertEquals("", sim.defaultEnv(), "no env files where there is no game");
        assertTrue(new java.io.File(sim.batchScript()).exists(), "the runner script exists");
    }
}
