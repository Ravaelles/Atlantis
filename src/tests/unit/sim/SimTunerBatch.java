package tests.unit.sim;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.debug.tweaker.DuelHitsMinutesScenario;
import atlantis.debug.tweaker.ParamTweakerFactory;
import atlantis.debug.tweaker.TunerBatchManifest;
import atlantis.debug.tweaker.TunerObjective;
import atlantis.debug.tweaker.TuningScenario;
import atlantis.units.AUnitType;
import tests.fakes.FakeUnit;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemy;

/**
 * Sim batch runner (NEXT #160-sim): the wine batch contract without Wine.
 *
 * <p>Usage mirrors {@code scripts/run-wine-batch.sh} so the tune driver
 * cannot tell them apart: {@code SimTunerBatch MAP N OUTDIR
 * [KEY=VALUE ...]}. Only the duel scenario map plays
 * ({@code 1Drag_v_1Zeal.scm}); anything else fails loudly. Each game
 * applies its vector through {@code ParamTweakerFactory.apply} (game
 * units, same as env files and tuner fragments), plays replicates of
 * the scripted duel, and lands the wine-contract artifacts:
 * per-game {@code tweaker.csv} (same ;-separated layout as the bot's),
 * {@code batch.csv} summary lines, {@code manifest.txt} via
 * {@code TunerBatchManifest.render} (digest-compatible, feeds
 * {@code TunerDriver} unchanged).</p>
 *
 * <p>Sim analogues, documented not hidden: hits = our HP+shields lost
 * (the twin stream does not exist in sim), seconds = ticks/24, shots =
 * both sides' fired shots, score = the evaluator's shape (win, kills,
 * losses, time). Spawn jitter per replicate is the sim's stand-in for
 * duel noise.</p>
 */
public final class SimTunerBatch {

    private static final int TILE = 32;
    private static final int MAX_TICKS = 900;

    private SimTunerBatch() {
    }

    public static void main(String[] args) throws Exception {
        SimBatchBootstrap.install();
        System.exit(run(args));
    }

    static int run(String[] args) throws Exception {
        String map = null;
        String outdir = null;
        int games = 0;
        Map<String, Double> overrides = new LinkedHashMap<>();
        for (String arg : args) {
            if (arg.equals("--help") || arg.equals("-h")) {
                System.out.println("usage: SimTunerBatch MAP N OUTDIR [KEY=VALUE ...]");

                return 0;
            }
            if (arg.startsWith("--env=") || arg.equals("--headless") || arg.equals("--hidden")) {
                continue;
            }
            if (arg.contains("=")) {
                int eq = arg.indexOf('=');
                overrides.put(arg.substring(0, eq), Double.parseDouble(arg.substring(eq + 1)));
                continue;
            }
            if (map == null) map = arg;
            else if (games == 0) games = Integer.parseInt(arg);
            else if (outdir == null) outdir = arg;
            else {
                System.err.println("ignoring extra arg: " + arg);
            }
        }
        if (map == null || games <= 0 || outdir == null) {
            System.err.println("usage: SimTunerBatch MAP N OUTDIR [KEY=VALUE ...]");

            return 2;
        }

        TuningScenario scenario = new DuelHitsMinutesScenario();
        String mapFile = java.nio.file.Paths.get(map).getFileName().toString();
        if (!mapFile.equals(scenario.mapFile())) {
            System.err.println("sim tunes only the duel scenario (" + scenario.mapFile()
                + "), got: " + map + " (use its file name, any directory is fine)");

            return 2;
        }
        if (!scenario.frozenParams().isEmpty()) {
            System.err.println("sim batch does not implement frozen params yet: "
                + scenario.frozenParams());

            return 2;
        }

        Path out = Paths.get(outdir);
        Files.createDirectories(out);
        Path csv = out.resolve("batch.csv");
        if (!Files.exists(csv)) {
            Files.write(csv, "game,result,seconds,killed,lost,hits,shots,score\n".getBytes(StandardCharsets.UTF_8));
        }

        List<TunerBatchManifest.Game> manifestGames = new ArrayList<>();
        Map<String, double[]> bounds = ParamTweakerFactory.bounds();
        // A/B wiring switch (NEXT #186): pulled out before the vector
        // validation (it is not a tuner knob and must never enter the
        // registry or the digest-compat row shapes). try/finally: a
        // leaked true would silently run every later batch unwired.
        Double wiringOverride = overrides.remove("HoldEarlyDisabled");
        atlantis.battle.strategy.DuelKite.holdEarlyDisabledForAB =
            wiringOverride != null && wiringOverride >= 0.5;
        if (atlantis.battle.strategy.DuelKite.holdEarlyDisabledForAB) {
            System.out.println("[sim-batch] HoldEarlyDisabled=1 (A/B arm: pre-wiring formula)");
        }
        try {
        for (int i = 1; i <= games; i++) {
            Map<String, Double> vector;
            try {
                vector = vectorForGame(bounds, overrides);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());

                return 2;
            }
            applyVector(vector);
            DuelResult result = playDuel(i);

            String dir = "game-" + i;
            Path gameDir = out.resolve(dir);
            Files.createDirectories(gameDir);
            String tweakerRow = tweakerRow(result, vector, bounds);
            Files.write(gameDir.resolve("tweaker.csv"),
                (tweakerHeader(bounds) + "\n" + tweakerRow + "\n").getBytes(StandardCharsets.UTF_8));

            String batchLine = i + "," + (result.win ? "W" : "L") + "," + result.seconds
                + "," + result.killed + "," + result.lost + "," + result.hits
                + "," + result.shots + "," + result.score + "\n";
            Files.write(csv, batchLine.getBytes(StandardCharsets.UTF_8),
                java.nio.file.StandardOpenOption.APPEND);
            manifestGames.add(new TunerBatchManifest.Game(
                dir, new TunerObjective.Row(dir, result.win, result.hits, result.seconds), vector));
            System.out.println("[sim-batch] game " + i + "/" + games + ": "
                + (result.win ? "WIN" : "LOSS") + " hits=" + result.hits
                + " seconds=" + result.seconds);
        }

        Files.write(out.resolve("manifest.txt"),
            TunerBatchManifest.render(scenario.mapFile(), "sim", manifestGames).getBytes(StandardCharsets.UTF_8));
        } finally {
            atlantis.battle.strategy.DuelKite.holdEarlyDisabledForAB = false;
        }
        System.out.println("[sim-batch] done: " + games + " games, manifest: "
            + out.resolve("manifest.txt"));

        return 0;
    }

    private static final class DuelResult {
        final boolean win;
        final int seconds;
        final int killed;
        final int lost;
        final int hits;
        final int shots;
        final int score;

        DuelResult(boolean win, int seconds, int killed, int lost, int hits, int shots) {
            this.win = win;
            this.seconds = seconds;
            this.killed = killed;
            this.lost = lost;
            this.hits = hits;
            this.shots = shots;
            this.score = (win ? 0 : -500) + killed * 10 - lost * 40 - seconds;
        }
    }

    private static Map<String, Double> vectorForGame(
        Map<String, double[]> bounds, Map<String, Double> overrides
    ) {
        Map<String, Double> vector = new LinkedHashMap<>();
        for (Map.Entry<String, double[]> bound : bounds.entrySet()) {
            String name = bound.getKey();
            if (overrides.containsKey(name)) {
                vector.put(name, overrides.get(name));
                continue;
            }
            double[] range = bound.getValue();
            double value = range[0] + Math.random() * (range[1] - range[0]);
            if (ParamTweakerFactory.integerParams().contains(name)) value = Math.round(value);
            vector.put(name, value);
        }
        for (String name : overrides.keySet()) {
            if (!bounds.containsKey(name)) throw new IllegalArgumentException("unknown tuner param: " + name);
        }
        return vector;
    }

    private static void applyVector(Map<String, Double> vector) {
        BattleCommander.clearForTests();
        for (Map.Entry<String, Double> entry : vector.entrySet()) {
            ParamTweakerFactory.apply(entry.getKey(), entry.getValue());
        }
    }

    private static DuelResult playDuel(int replicate) {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10, 20);
        FakeUnit zealot = fakeEnemy(AUnitType.Protoss_Zealot, 13 + (replicate % 3), 20);
        int startHp = dragoon.hp + dragoon.shields;

        SimArena arena = new SimArena(60 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(new FakeUnit[] { dragoon }, new FakeUnit[] { zealot }, arena);
        int ticks = 0;
        try {
            BattleCommander.clearForTests();
            for (ticks = 0; ticks < MAX_TICKS; ticks++) {
                final FakeUnit foe = zealot;
                final FakeUnit ours = dragoon;
                world.tick(() -> {
                    if (ours.hp > 0) {
                        BattleCommander.act(ours, FightOrFlight.Tactic.FIGHT, null, ours);
                    }
                    DragoonsVsZerglingsTest.scriptChaser(foe, ours);
                });
                if (zealot.hp <= 0 || dragoon.hp <= 0) break;
            }
        } finally {
            world.close();
        }

        boolean win = zealot.hp <= 0;
        int seconds = Math.max(1, ticks / 24);
        int killed = win ? 1 : 0;
        int lost = dragoon.hp <= 0 ? 1 : 0;
        int hits = Math.max(0, startHp - (dragoon.hp + dragoon.shields));

        return new DuelResult(win, seconds, killed, lost, hits, world.shotsFired());
    }

    private static String tweakerHeader(Map<String, double[]> bounds) {
        StringBuilder header = new StringBuilder("Date;W/L;Seconds;Killed;Lost;HitsTaken;ShotsFired;Score");
        for (String name : bounds.keySet()) header.append(';').append(name);

        return header.toString();
    }

    private static String tweakerRow(
        DuelResult result, Map<String, Double> vector, Map<String, double[]> bounds
    ) {
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        StringBuilder row = new StringBuilder(date + ";" + (result.win ? "W" : "L")
            + ";" + result.seconds + ";" + result.killed + ";" + result.lost
            + ";" + result.hits + ";" + result.shots + ";" + result.score);
        for (String name : bounds.keySet()) row.append(';').append(vector.get(name));

        return row.toString();
    }
}
