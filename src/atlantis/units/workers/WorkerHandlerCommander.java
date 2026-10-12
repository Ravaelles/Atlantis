package atlantis.units.workers;

import atlantis.architecture.Commander;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

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
     * Behind {@code BUILDER_LOG=1} and once per second, so a normal run is silent and the line
     * is cheap. 10 frames is the owner's threshold: a gathering worker changes position far more
     * often than that, so a 10-frame standstill is anomalous rather than a pause.
     * </p>
     */
    private static void reportStuckWorker(AUnit worker) {
        if (!Env.builderLog()) return;
        if (!A.everyNthGameFrame(30)) return;
        if (worker.lastPositionChangedAgo() < 10) return;

        AUnit mineral = Select.all().minerals().nearestTo(worker);

        // NEXT #63: on a stuck worker separate OUR flag (action) from the ENGINE's
        // answer. A worker with NO_COMMAND that keeps re-issuing Gather every 2nd
        // frame (throttle) while the engine refuses needs canGather/canRightClick
        // to name the refusing term; a worker with an accepted Gather standing on
        // the patch (distToMineral=0.0) is mining, not stuck - the 10-frame
        // standstill threshold false-positives on miners pausing on the patch.
        String engineAnswers = "";
        try {
            boolean canGather = mineral != null && worker.u() != null && mineral.u() != null
                && worker.u().canGather(mineral.u());
            boolean canRightClick = mineral != null && worker.u() != null && mineral.u() != null
                && worker.u().canRightClick(mineral.u());
            // NEXT #63: worker-side vs target-side. Both-false gather+rightClick
            // fits two different faults: our worker not commandable (exists,
            // completed, canCommand) or its ASSIGNED target unknown to the
            // client (!exists / not completed) - print both sides, and the
            // assigned target id (it need not be the nearest mineral sampled).
            AUnit assigned = worker.target();
            String targetSide;
            if (assigned == null || assigned.u() == null) {
                targetSide = "assignedTarget=none";
            } else {
                targetSide = "assignedTarget=#" + assigned.id() + ":" + assigned.type().name()
                    + " exists=" + assigned.u().exists()
                    + " completed=" + assigned.u().isCompleted()
                    + " visible=" + assigned.u().isVisible();
            }
            String workerSide = worker.u() == null ? "workerU=null"
                : "workerExists=" + worker.u().exists()
                + " workerCompleted=" + worker.u().isCompleted()
                + " workerCanCommand=" + worker.u().canCommand();
            engineAnswers = " canGather=" + canGather + " canRightClick=" + canRightClick
                + " order=" + (worker.u() == null ? "no-u" : worker.u().getOrder())
                + " tileWalkable=" + atlantis.map.MapTiles.isWalkable(worker)
                + " " + workerSide + " " + targetSide;
        } catch (Throwable t) {
            engineAnswers = " engineProbeThrew=" + t.getClass().getSimpleName();
        }

        System.err.println("STUCK_WORKER @" + A.now()
            + " unit=#" + worker.id() + " " + worker.type().name() + " @[" + worker.tx() + "," + worker.ty() + "]"
            + " stoodStillFor=" + worker.lastPositionChangedAgo() + "f"
            + " manager=" + (worker.manager() == null ? "null" : worker.manager().getClass().getSimpleName())
            + " lastCommand=" + worker.lastCommandName()
            + " lastCommandIssuedAgo=" + worker.lastCommandIssuedAgo()
            + " action=" + worker.action().name()
            + " isIdle=" + worker.isIdle()
            + " isStopped=" + worker.isStopped()
            + " isGathering=" + worker.isGatheringResources()
            + " isBuilder=" + worker.isBuilder()
            + " distToMineral=" + (mineral == null ? "none" : A.digit(worker.distTo(mineral)))
            + " mineralsNear=" + Select.all().minerals().countInRadius(8, worker)
            + " bases=" + Select.ourBases().count() + engineAnswers);
    }
}
