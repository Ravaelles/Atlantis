package tests.unit.sim;

import atlantis.battle.BattleCommander;
import atlantis.debug.tweaker.DuelHitsMinutesScenario;
import atlantis.debug.tweaker.ParamTweakerFactory;
import atlantis.debug.tweaker.TunerBatchManifest;
import atlantis.debug.tweaker.TunerDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.unit.AbstractTestWithUnits;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sim batch runner (NEXT #160-sim): a 2-game batch lands the wine
 * contract (tweaker.csv per game, batch.csv, manifest.txt) and the
 * tune driver consumes the manifest unchanged (hall of fame +
 * fragment). Plus the applier pin: every bounds key applies, unknown
 * names fail loudly instead of tuning a different vector than the
 * manifest records.
 */
@Tag("infra")
public class SimTunerBatchTest extends AbstractTestWithUnits {

    @AfterEach
    public void resetTuning() {
        BattleCommander.clearForTests();
    }

    @Test
    public void everyBoundKeyApplies() {
        for (Map.Entry<String, double[]> bound : ParamTweakerFactory.bounds().entrySet()) {
            ParamTweakerFactory.apply(bound.getKey(), bound.getValue()[0]);
        }
    }

    @Test
    public void unknownParamFailsLoudly() {
        assertThrows(IllegalArgumentException.class,
            () -> ParamTweakerFactory.apply("NoSuchParam", 1.0));
    }

    @Test
    public void appliedMinReadsBack() {
        ParamTweakerFactory.apply("KiteStepTiles", 1.5);
        assertEquals(1.5,
            atlantis.battle.kite.CombatTuning.current().getKiteStepTiles(), 0.0001);

        ParamTweakerFactory.apply("DragoonLowHp", 30);
        assertEquals(30, atlantis.battle.kite.CombatTuning.current().getDragoonLowHp());

        ParamTweakerFactory.apply("EscapeStaticWeight", 5.0);
        assertEquals(5.0,
            atlantis.battle.escape.EscapeTuning.current().getStaticWeight(), 0.0001);
    }

    @Test
    public void batchLandsTheWineContractAndTheDriverAcceptsIt() throws Exception {
        Path outdir = Files.createTempDirectory("sim-tuner-batch-test");

        int exit = SimTunerBatch.run(new String[] {
            new DuelHitsMinutesScenario().mapFile(), "2", outdir.toString(),
            "KiteStepTiles=2.0", "EscapeStaticWeight=10.0"
        });
        assertEquals(0, exit, "batch runs");

        Path csv = outdir.resolve("batch.csv");
        List<String> rows = Files.readAllLines(csv, StandardCharsets.UTF_8);
        assertEquals(3, rows.size(), "header + 2 games: " + rows);
        assertEquals("game,result,seconds,killed,lost,hits,shots,score", rows.get(0));
        for (int i = 1; i <= 2; i++) {
            assertTrue(rows.get(i).startsWith(i + ",W,") || rows.get(i).startsWith(i + ",L,"),
                "game row verdict: " + rows.get(i));
            Path tweaker = outdir.resolve("game-" + i).resolve("tweaker.csv");
            assertTrue(Files.exists(tweaker), "per-game tweaker row: " + tweaker);
            List<String> tweakerRows = Files.readAllLines(tweaker, StandardCharsets.UTF_8);
            assertEquals(2, tweakerRows.size(), "header + one row: " + tweaker);
            assertTrue(tweakerRows.get(0).startsWith("Date;W/L;Seconds;Killed;Lost;HitsTaken;ShotsFired;Score;"),
                "wine layout: " + tweakerRows.get(0));
            assertTrue(columnEquals(tweakerRows.get(0), tweakerRows.get(1), "KiteStepTiles", "2.0"),
                "override vector recorded, not randomized: " + tweakerRows.get(1));
            assertTrue(columnEquals(tweakerRows.get(0), tweakerRows.get(1), "EscapeStaticWeight", "10.0"),
                "override vector recorded, not randomized: " + tweakerRows.get(1));
        }

        Path manifest = outdir.resolve("manifest.txt");
        String text = new String(Files.readAllBytes(manifest), StandardCharsets.UTF_8);
        assertTrue(new DuelHitsMinutesScenario().mapFile().equals(TunerBatchManifest.parseMap(text)),
            "manifest names the map");
        List<TunerBatchManifest.Game> games = TunerBatchManifest.parseGames(text);
        assertEquals(2, games.size(), "both games parse");
        String rerendered = TunerBatchManifest.render(
            TunerBatchManifest.parseMap(text), "sim", games);
        assertEquals(text, rerendered, "digest-stable round trip");

        Path hof = outdir.resolve("halloffame.txt");
        Path fragment = outdir.resolve("fragment.env");
        TunerDriver.main(new String[] {
            "--hof", hof.toString(), "--manifest", manifest.toString(), "--out", fragment.toString()
        });
        assertTrue(Files.exists(fragment), "driver wrote the next fragment");
        assertTrue(new String(Files.readAllBytes(fragment), StandardCharsets.UTF_8)
                .contains("KiteStepTiles="),
            "fragment carries param values");
    }

    @Test
    public void unknownMapFailsLoudly() throws Exception {
        Path outdir = Files.createTempDirectory("sim-tuner-batch-map-test");

        int exit = SimTunerBatch.run(new String[] { "NoSuchMap.scm", "1", outdir.toString() });

        assertEquals(2, exit, "only the duel scenario tunes");
    }

    @Test
    public void unknownBatchParamFailsLoudly() throws Exception {
        Path outdir = Files.createTempDirectory("sim-tuner-batch-param-test");

        int exit = SimTunerBatch.run(
            new String[] { new DuelHitsMinutesScenario().mapFile(), "1", outdir.toString(), "Nope=1.0" });

        assertEquals(2, exit, "typo in a param name must not tune silently");
    }

    private static boolean columnEquals(String header, String row, String column, String expected) {
        String[] names = header.split(";");
        String[] values = row.split(";");
        for (int i = 0; i < names.length && i < values.length; i++) {
            if (names[i].equals(column)) return values[i].equals(expected);
        }
        return false;
    }
}
