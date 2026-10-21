package atlantis.debug.tweaker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Finite-difference gradient over tuner history (NEXT #160, step 6):
 * which direction each param should grow to lower the fitness, and by
 * how much. One least-squares slope per param over evaluated vectors -
 * coordinate descent with a span-fraction step, not a Newton method:
 * game outcomes are noisy and the surface is not smooth, so the slope
 * only votes on the SIGN of the step while the schedule owns the size.
 * Frozen params are never touched; everything is clamped back into
 * bounds, so a wild slope can waste a batch but never break a game.
 *
 * <p>Pure tooling, no game-loop callers. Deterministic given the same
 * history and seed.
 */
public final class TunerGradient {

    private TunerGradient() {
    }

    /** One evaluated point: the vector that played and the fitness it scored. */
    public static final class Sample {
        public final Map<String, Double> vector;
        public final double fitness;

        public Sample(Map<String, Double> vector, double fitness) {
            this.vector = vector;
            this.fitness = fitness;
        }
    }

    /**
     * Least-squares slope of fitness over each param: positive means
     * growing the param grows the fitness (so descend by shrinking
     * it). Params with no variance in the history, non-finite fitness,
     * or fewer than two usable samples score exactly zero - no signal,
     * no step.
     */
    public static Map<String, Double> slopes(List<Sample> history, Set<String> params) {
        Map<String, Double> slopes = new LinkedHashMap<>();
        if (params != null) {
            for (String param : params) slopes.put(param, 0.0);
        }
        if (history == null || params == null) return slopes;

        List<Sample> usable = new ArrayList<>();
        for (Sample sample : history) {
            if (sample == null || sample.vector == null) continue;
            if (!Double.isFinite(sample.fitness)) continue;
            boolean complete = true;
            for (String param : params) {
                Double value = sample.vector.get(param);
                if (value == null || !Double.isFinite(value)) {
                    complete = false;
                    break;
                }
            }
            if (complete) usable.add(sample);
        }
        if (usable.size() < 2) return slopes;

        double meanFitness = 0;
        for (Sample sample : usable) meanFitness += sample.fitness;
        meanFitness /= usable.size();

        for (String param : params) {
            double meanParam = 0;
            for (Sample sample : usable) meanParam += sample.vector.get(param);
            meanParam /= usable.size();

            double covariance = 0;
            double variance = 0;
            for (Sample sample : usable) {
                double dx = sample.vector.get(param) - meanParam;
                covariance += dx * (sample.fitness - meanFitness);
                variance += dx * dx;
            }
            if (variance > 0) slopes.put(param, covariance / variance);
        }
        return slopes;
    }

    /**
     * One descent step from the champion against the slopes: every
     * unfrozen param moves {@code step} of its span opposite the slope
     * sign, then clamps into bounds. Zero slopes hold their ground.
     * Params missing from bounds ride at champion values - the bound
     * table owns the searchable space, not the history.
     */
    public static Map<String, Double> descend(
        Map<String, Double> champion, Map<String, Double> slopes,
        Map<String, double[]> bounds, Set<String> frozen, double step
    ) {
        LinkedHashMap<String, Double> next = new LinkedHashMap<>();
        if (champion == null) return next;
        for (Map.Entry<String, Double> entry : champion.entrySet()) {
            String param = entry.getKey();
            double value = entry.getValue();
            double[] bound = bounds == null ? null : bounds.get(param);
            if (bound == null || bound.length < 2 || bound[1] <= bound[0]
                || step <= 0 || (frozen != null && frozen.contains(param))) {
                next.put(param, value);
                continue;
            }
            double slope = slopes == null || slopes.get(param) == null ? 0 : slopes.get(param);
            if (slope > 0) value -= step * (bound[1] - bound[0]);
            else if (slope < 0) value += step * (bound[1] - bound[0]);
            next.put(param, clamp(value, bound[0], bound[1]));
        }
        return next;
    }

    /**
     * Exploration noise around a proposal: every unfrozen param shifts
     * uniformly in {@code +/- radius * span}, clamped into bounds.
     * Same semantics as the hall-of-fame radius mutation, owned here so
     * the proposal (descend + jitter) is one deterministic function of
     * history plus seed.
     */
    public static Map<String, Double> jitter(
        Map<String, Double> vector, Map<String, double[]> bounds,
        Set<String> frozen, double radius, Random random
    ) {
        LinkedHashMap<String, Double> out = new LinkedHashMap<>();
        if (vector == null) return out;
        for (Map.Entry<String, Double> entry : vector.entrySet()) {
            String param = entry.getKey();
            double value = entry.getValue();
            double[] bound = bounds == null ? null : bounds.get(param);
            if (bound == null || bound.length < 2 || bound[1] <= bound[0]
                || radius <= 0 || random == null
                || (frozen != null && frozen.contains(param))) {
                out.put(param, value);
                continue;
            }
            double shift = (random.nextDouble() * 2 - 1) * radius * (bound[1] - bound[0]);
            out.put(param, clamp(value + shift, bound[0], bound[1]));
        }
        return out;
    }

    /**
     * Full proposal: descend against the history slopes, then jitter
     * for exploration. With fewer than two usable samples there is no
     * slope to descend, so the champion only jitters (cold start =
     * exploration around the best known point).
     */
    public static Map<String, Double> propose(
        Map<String, Double> champion, List<Sample> history,
        Map<String, double[]> bounds, Set<String> frozen,
        double step, double radius, Random random
    ) {
        if (champion == null || champion.isEmpty()) return new LinkedHashMap<String, Double>();
        Map<String, Double> afterDescent = champion;
        if (history != null && bounds != null) {
            java.util.Set<String> params = bounds.keySet();
            Map<String, Double> slopeMap = slopes(history, params);
            boolean anySignal = false;
            for (double slope : slopeMap.values()) {
                if (slope != 0) {
                    anySignal = true;
                    break;
                }
            }
            if (anySignal) afterDescent = descend(champion, slopeMap, bounds, frozen, step);
        }
        return jitter(afterDescent, bounds, frozen, radius, random);
    }

    /**
     * Env-file rendering for one value: integral for int params (the
     * factory parses those with {@code Integer.parseInt}, which rejects
     * decimals and silently randomizes), plain otherwise. Clamp-round-
     * clamp: rounding a clamped double can never leave integral bounds,
     * but a hand-fed value can, and the render boundary is exactly
     * where a silent-randomize trap hides - so the clamp runs here too.
     */
    public static String formatValue(String param, double value, Set<String> intParams, double[] bounds) {
        if (intParams != null && intParams.contains(param)) {
            long rounded = Math.round(clamp(value,
                bounds != null && bounds.length >= 2 ? bounds[0] : Double.NEGATIVE_INFINITY,
                bounds != null && bounds.length >= 2 ? bounds[1] : Double.POSITIVE_INFINITY));
            if (bounds != null && bounds.length >= 2) {
                rounded = Math.max((long) bounds[0], Math.min((long) bounds[1], rounded));
            }
            return Long.toString(rounded);
        }
        return Double.toString(value);
    }

    /** Backwards-compatible render without bounds (bounds live with the caller). */
    public static String formatValue(String param, double value, Set<String> intParams) {
        return formatValue(param, value, intParams, null);
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
