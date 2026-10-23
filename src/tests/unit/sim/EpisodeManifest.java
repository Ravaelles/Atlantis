package tests.unit.sim;

import java.util.List;

import atlantis.units.AUnit;

/**
 * In-memory episode manifest (NEXT #185): per-tick feature rows for
 * offline analysis and the future learner. No filesystem - tests
 * assert on the text; batch harnesses decide where manifests live.
 * Header first, then one CSV row per snapshot.
 */
public final class EpisodeManifest {

    private EpisodeManifest() {
    }

    public static final String HEADER = "frame,unit," + "hpFrac,shieldFrac,cooldownFrac,nearestFoeDist,foesMelee2,foesNear9,friendsNear";

    public static String render(int frame, AUnit unit) {
        return frame + "," + (unit == null ? -1 : unit.id()) + ","
            + BattleFeatures.snapshot(unit).toCsv();
    }

    public static String renderAll(List<String> rows) {
        StringBuilder out = new StringBuilder(HEADER).append('\n');
        for (String r : rows) out.append(r).append('\n');

        return out.toString();
    }
}
