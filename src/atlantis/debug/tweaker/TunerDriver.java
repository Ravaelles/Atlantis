package atlantis.debug.tweaker;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Thin CLI over the gradient loop (NEXT #160, step 6): reads the hall
 * of fame plus the latest batch manifest, offers the new rows, saves
 * the archive, and writes the next per-game env fragment
 * ({@code NAME=value} lines the batch runner feeds back as overrides).
 * All decisions live in {@link TunerProposer} (tested); this class only
 * moves files and parses flags.
 *
 * <p>Usage:
 * {@code TunerDriver --hof hof.txt --manifest OUTDIR/manifest.txt
 * --out OUTDIR/fragment.env [--step 0.05] [--radius 0.10] [--seed N]}
 *
 * <p>Stdout carries one machine line, {@code BEST=<fitness>}, for the
 * loop schedule (halve the step when the best stops improving), plus
 * human lines. A missing or empty archive is a cold start: no fragment
 * is written and the batch randomizes, exactly today's behavior.
 *
 * <p>The hall-of-fame file is one per (scenario, map): rows carry no
 * map tag, so sharing a file across maps would mix surfaces the
 * gradient cannot separate. The tune loop keeps it inside its own
 * OUTDIR for exactly this reason.
 */
public final class TunerDriver {

    private TunerDriver() {
    }

    public static void main(String[] args) throws Exception {
        String hofPath = flag(args, "--hof");
        String manifestPath = flag(args, "--manifest");
        String outPath = flag(args, "--out");
        if (hofPath == null || manifestPath == null || outPath == null) {
            System.err.println("usage: TunerDriver --hof HOF --manifest MANIFEST --out FRAGMENT [--step D] [--radius D] [--seed N]");
            System.exit(2);
        }
        TuningScenario scenario = new DuelHitsMinutesScenario();
        double step = number(args, "--step", -1);
        double radius = number(args, "--radius", -1);
        long seed = (long) number(args, "--seed", System.nanoTime());
        double effStep = step >= 0 ? step : scenario.gradientStep();
        double effRadius = radius >= 0 ? radius : scenario.noiseRadius();

        TunerHallOfFame hallOfFame = new TunerHallOfFame(50);

        Path hofFile = Paths.get(hofPath);
        if (Files.exists(hofFile)) {
            hallOfFame.load(new String(Files.readAllBytes(hofFile), StandardCharsets.UTF_8));
        }
        String manifest = new String(Files.readAllBytes(Paths.get(manifestPath)), StandardCharsets.UTF_8);
        String manifestMap = TunerBatchManifest.parseMap(manifest);
        if (manifestMap != null && !manifestMap.equals(scenario.mapFile())) {
            System.err.println("WARNING: manifest played " + manifestMap
                + " but the scenario tunes " + scenario.mapFile()
                + " - slopes will mix surfaces");
        }
        for (TunerBatchManifest.Game game : TunerBatchManifest.parseGames(manifest)) {
            hallOfFame.offer(game.row, game.vector);
        }
        Files.write(hofFile, hallOfFame.save().getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        Map<String, Double> proposal = TunerProposer.propose(scenario, hallOfFame, effStep, effRadius, seed);
        if (proposal.isEmpty()) {
            System.out.println("BEST=unknown (cold start, no rows yet)");
            System.out.println("no proposal: archive empty, batch randomizes");

            return;
        }

        List<String> lines = new ArrayList<>();
        java.util.Set<String> ints = ParamTweakerFactory.integerParams();
        Map<String, double[]> bounds = new DuelHitsMinutesScenario().paramBounds();
        for (Map.Entry<String, Double> param : proposal.entrySet()) {
            lines.add(param.getKey() + "=" + TunerGradient.formatValue(
                param.getKey(), param.getValue(), ints, bounds.get(param.getKey())));
        }
        Path outFile = Paths.get(outPath);
        if (outFile.getParent() != null) Files.createDirectories(outFile.getParent());
        Files.write(outFile, (String.join("\n", lines) + "\n").getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        List<TunerHallOfFame.Entry> ranked = TunerProposer.rank(scenario, hallOfFame.entries());
        double best = scenario.fitness(ranked.get(0).row());
        System.out.println("BEST=" + best);
        System.out.println("STEP=" + effStep);
        System.out.println("RADIUS=" + effRadius);
        System.out.println("champion: " + ranked.get(0).row().game() + " fitness=" + best);
        System.out.println("proposal: " + proposal.size() + " params -> " + outPath);
    }

    private static String flag(String[] args, String name) {
        for (int i = 0; i + 1 < args.length; i++) {
            if (args[i].equals(name)) return args[i + 1];
        }
        return null;
    }

    private static double number(String[] args, String name, double fallback) {
        String raw = flag(args, name);
        if (raw == null) return fallback;

        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
