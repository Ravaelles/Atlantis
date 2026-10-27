package tests.unit.sim;

import atlantis.units.AUnit;

/**
 * Test-scope learned gate (NEXT #185 S1): the trained net behind the
 * same boolean shape as {@link atlantis.combat.strategy.DuelKite#shouldYield}.
 * Exception-safe by construction - any failure answers "hold" (shoot),
 * never a blind yield. Production gating rides S3; this class proves
 * the vehicle sim -&gt; CSV -&gt; train -&gt; eval -&gt; gate, nothing more.
 */
public final class LearnedHoldEarlyGate {

    private final SimMLP net;

    public LearnedHoldEarlyGate(SimMLP net) {
        this.net = net;
    }

    /** Net verdict at 0.5, or hold on anything unexpected. */
    public boolean shouldYield(AUnit unit, AUnit threat) {
        if (unit == null || threat == null || net == null) return false;

        try {
            return net.predictProb(HoldEarlyDataset.features(unit, threat)) >= 0.5;
        } catch (Exception | Error ignored) {
            return false;
        }
    }
}
