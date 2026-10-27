package tests.unit.sim;

import atlantis.production.v2.EconomyModel;
import atlantis.units.actions.Actions;
import tests.fakes.FakeUnit;

import java.util.HashMap;
import java.util.Map;

/**
 * Sim income: who mines, who pumps gas, how much lands per frame.
 *
 * <p>Assignment is the sim's, not the bot's: the test scenario has no
 * worker manager running, so new probes join the line and the first
 * refinery takes three miners on completion - the same shape the live
 * game converges to. Rates come from {@link EconomyModel}, the exact
 * function the v2 timeline plans against, so plan and sim cannot
 * disagree about income. First-trip delay ({@code NEW_WORKER_FIRST_TRIP_FRAMES})
 * applies to fresh miners in both modes: a probe that just hatched or
 * just walked to the line earns nothing yet.</p>
 */
public final class SimMining {

    /** Sim-owned miner state; the bot reads it through stub actions. */
    public enum Job {
        MINING,
        GAS,
        BUILDING,
        IDLE
    }

    private final Map<Integer, Job> jobs = new HashMap<>();
    private final Map<Integer, Integer> jobSince = new HashMap<>();

    public void assign(FakeUnit probe, Job job, int frame) {
        jobs.put(probe.id(), job);
        jobSince.put(probe.id(), frame);
        if (job == Job.MINING) probe.setAction(Actions.GATHER_MINERALS);
        else if (job == Job.GAS) probe.setAction(Actions.GATHER_GAS);
    }

    public Job jobOf(FakeUnit probe) {
        Job job = jobs.get(probe.id());

        return job == null ? Job.IDLE : job;
    }

    /** Probes earning minerals this frame (tenure past the first trip). */
    public int effectiveMiners(SimUnitRegistry units, int frame) {
        int count = 0;
        for (FakeUnit probe : units.ours()) {
            if (probe == null || !probe.isAlive()) continue;
            if (!probe.type().isWorker()) continue;
            if (jobOf(probe) != Job.MINING) continue;
            Integer since = jobSince.get(probe.id());
            if (since != null && frame - since < EconomyModel.NEW_WORKER_FIRST_TRIP_FRAMES) continue;
            count++;
        }

        return count;
    }

    public int gasMiners(SimUnitRegistry units) {
        int count = 0;
        for (FakeUnit probe : units.ours()) {
            if (probe == null || !probe.isAlive()) continue;
            if (!probe.type().isWorker()) continue;
            if (jobOf(probe) == Job.GAS) count++;
        }

        return count;
    }

    /** Applies one frame of income to the bank. */
    public void earn(SimBank bank, SimUnitRegistry units, int frame, int bases) {
        bank.earn(
            EconomyModel.mineralRate(effectiveMiners(units, frame), bases),
            gasMiners(units) * EconomyModel.GAS_PER_WORKER_FRAME);
    }

    /** Nearest idle/mining probes, for builder and gas reassignment. */
    public java.util.List<FakeUnit> freeProbes(SimUnitRegistry units, int limit) {
        java.util.List<FakeUnit> out = new java.util.ArrayList<>();
        for (FakeUnit probe : units.ours()) {
            if (out.size() >= limit) break;
            if (probe == null || !probe.isAlive() || !probe.isCompleted()) continue;
            if (!probe.type().isWorker()) continue;
            Job job = jobOf(probe);
            if (job == Job.MINING || job == Job.IDLE) out.add(probe);
        }

        return out;
    }
}
