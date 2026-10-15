package atlantis.units.workers;

import atlantis.architecture.Commander;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

public class WorkerHandlerCommander extends Commander {
    @Override
    protected boolean handle() {
        for (AUnit worker : Select.ourWorkers().list()) {
            (new WorkerManager(worker)).invokeFrom(this);

            reportStuckWorker(worker);
        }
        return false;
    }

    /**
     * Print a worker that has not moved for 10 frames, with what is supposedly handling it.
     *
     * <p>
     * Owner's report (2026-10-11): "many workers just stand there doing nothing after being
     * produced - in that game it happened after 10-12 of them, here even the first few (5, 6)
     * stand still, and there is NOTHING in the log". That is the defining property of this
     * class of bug: a worker that is never given an order is invisible in every existing
     * diagnostic, because nothing throws and no manager reports a refusal - the worker is
     * simply never picked up.
     * </p>
     *
     * <p>
     * Measured 2026-10-11 (OpenBW, {@code BUILDER_LOG=1}): a Probe reported
     * {@code manager=GatherResources lastCommand=NO_COMMAND lastCommandIssuedAgo=1 isIdle=true
     * isStopped=true isGathering=true distToMineral=4.4}, frozen at one tile from frame 30 to
     * the end of the game - the owner's "workers just stand there doing nothing, and the log
     * says nothing". Four candidate causes were checked and are NOT yet proven:
     * </p>
     *
     * <ul>
     *   <li><b>stale {@code isGathering*} flags</b> - a {@code stalledWhileClaimingToGather}
     *       relaxation of both gates was written on the strength of that reading and did NOT
     *       change the behaviour; reverted, so the theory is open, not accepted;</li>
     *   <li><b>{@code TreeSet} identity</b> - {@code Select.ourWorkers()} collects into a
     *       {@code TreeSet} ordered by {@code AUnit.compareTo}, which compares {@code hashCode()}
     *       and would silently drop colliding workers. Ruled OUT: {@code hashCode()} is
     *       {@code id()} and ids are unique per game;</li>
     *   <li><b>no manager reached</b> - the worker IS iterated here every frame, so it is
     *       reached;</li>
     *   <li><b>a declining gate</b> - the remaining candidate, and the first clause of
     *       {@code applies()}'s own rule ("has not changed position for 10 frames") cannot be
     *       satisfied while the manager keeps declaring it handled.</li>
     * </ul>
     *
     * <p>
     * Printed AFTER the manager chain ran, so {@code worker.manager()} is what actually handled
     * it this frame (or {@code DoNothing}, which is the initial value and therefore the tell
     * that no manager claimed the worker at all). Alongside it: the last command, whether the
     * engine thinks it is idle, and its distance to the nearest mineral - a worker standing
     * next to minerals and doing nothing is a manager problem, one far from any is a movement
     * problem.
     * </p>
     *
     * <p>
     * JSON-only since 2026-10-15 (owner: no red spam in the game log):
     * the row lands in {@code EVENTS.jsonl} under {@code STUCK_WORKER},
     * no console line. Behind {@code BUILDER_LOG=1} and once per second,
     * so a normal run is silent and the row is cheap. 10 frames is the
     * owner's threshold: a gathering worker changes position far more
     * often than that, so a 10-frame standstill is anomalous rather
     * than a pause.
     * </p>
     */
    private static void reportStuckWorker(AUnit worker) {
        if (!Env.builderLog()) return;
        if (!A.everyNthGameFrame(30)) return;
        if (worker.lastPositionChangedAgo() < 10) return;

        EventLog.key(LogKey.STUCK_WORKER)
            .param("unit", worker.id())
            .param("type", worker.type().name())
            .param("tx", worker.tx())
            .param("ty", worker.ty())
            .param("stoodStillForF", worker.lastPositionChangedAgo())
            .param("manager", worker.manager() == null ? "-" : worker.manager().getClass().getSimpleName())
            .param("lastCommand", worker.lastCommandName())
            .param("lastCommandIssuedAgo", worker.lastCommandIssuedAgo())
            .param("action", worker.action().name())
            .param("idle", worker.isIdle())
            .param("stopped", worker.isStopped())
            .param("gathering", worker.isGatheringResources())
            .param("builder", worker.isBuilder())
            .log(A.now(), A.minSec());
    }
}
