package atlantis.production.constructions.commanders;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.APositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.AConsole;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;
import java.util.Iterator;

public class ConstructionStatusChanger extends Commander {
    @Override
    protected boolean handle() {
        for (Iterator<Construction> iterator = ConstructionRequests.constructions.iterator(); iterator.hasNext(); ) {
            Construction construction = iterator.next();

            checkForConstructionStatusChange(construction);
            OverdueConstructions.handleIfOverdue(construction);
        }
        return false;
    }

    private void checkForConstructionStatusChange(Construction construction) {
        AUnit building = construction.buildingUnit();

        // A construction whose building is GONE - when the building was actually seen.
        //
        // `buildingUnit()` is only ever set from `builder.buildUnit()` (below), which reads
        // a shared-memory field OpenBW does not fill (NEXT.md #53). So on OpenBW it stays
        // null for the whole construction, and the old form of this guard -
        // `(building == null || !building.isAlive())` - cancelled every construction 30
        // frames after it started, with the reason "was destroyed", while the building was
        // standing and going up. Measured: 5 such cancels in one run.
        //
        // The condition is now only about a building we HAVE seen and that has since died.
        // "We never learned which unit it is" is a fact about the engine, not about the
        // building, and must not be read as destruction.
        if (
            !We.zerg()
                && construction.status() == ConstructionOrderStatus.IN_PROGRESS
                && construction.startedAgo() >= 30
                && building != null
                && !building.isAlive()
        ) {
            construction.cancel(construction.buildingType() + " was destroyed");
            AConsole.errPrintln("Building destroyed - cancel construction");
            return;
        }

        // =========================================================
        AUnit builder = construction.builder();

        // ...change builder into building (it just happens, yeah, weird stuff)
        if (building == null || building.hp() <= 0) {
            if (builder != null) {

                // If builder has changed its type and became Zerg Extractor
                if (!builder.is(AtlantisRaceConfig.WORKER)) {

                    // Happens for Extractor
                    if (builder.buildType() == null || builder.buildType().equals(AUnitType.None)) {
                        building = builder;
                        construction.setBuildingUnit(builder);
                    }
                }

                // Builder did not change it's type so it's not Zerg Extractor case
                else {
                    AUnit buildUnit = builder.buildUnit();
                    if (buildUnit != null) {
                        building = buildUnit;
                        construction.setBuildingUnit(buildUnit);
                    }
                }
            }
        }

        // If playing as ZERG...
        if (We.zerg()) {
            handleZergConstructionsWhichBecameBuildings();
        }

        // FINISHED: the building type is standing where the construction asked for it, so
        // the construction is done and must be removed.
        //
        // This is the other half of the deadlock (NEXT.md #53): without it a completed
        // construction stays in `ConstructionRequests` forever, `BuilderManager` keeps
        // holding its worker, and that worker never returns to mining. Measured: 526 builder
        // frames on a Pylon whose status was still NOT_STARTED, and `GatherResources`
        // appearing 5 times against `BuilderManager` 1331.
        //
        // It cannot be written against `building.isCompleted()` on the `buildingUnit` the
        // legacy path uses - that is the null field again. The question asked instead is the
        // one the game answers: is a finished building of this type sitting on the reserved
        // tile?
        if (construction.status() != ConstructionOrderStatus.FINISHED
            && buildingFinishedAtSite(construction)) {
            construction.setStatus(ConstructionOrderStatus.FINISHED);
            ConstructionRequests.removeOrder(construction);
        }

        // A construction whose builder HAS STARTED BELONGING TO IT must be marked
        // IN_PROGRESS - otherwise the worker is held at a site it is already building, does
        // not return to mining, and the base deadlocks (NEXT.md #53).
        //
        // This is written against `isConstructing`, NOT against `buildUnit()`, and that is
        // the whole point: `buildUnit()` reads a shared-memory field that OpenBW does not
        // fill the way BWAPI expects (`UnitUpdate.cpp:333` converts through
        // `getUnitFromBWUnit`, the id lookup fails, the client reads null - measured: the
        // Probe reports `isConstructing=true`, `lastCommand=Build` and `distToBuild=0.3`
        // while `buildUnit()` returns null). `isConstructing` IS published correctly
        // (`UnitUpdate.cpp:359`), so the question "did this worker start?" has a usable
        // answer and does not need the broken field.
        //
        // The builder is also required to be at the site: a worker that has merely been
        // assigned and is still walking is NOT building yet, and marking it early would
        // stop `TravelToConstruct` from finishing the walk.
        //
        // TWO signals, and the second one is why this works at all.
        //
        // `isConstructing` alone is not enough, and the reason is the same class of defect
        // as everything else on this engine: it is computed from the unit's ORDER
        // (`UnitUpdate.cpp:359`), and its last clause is
        // `(!isCompleted && buildUnit != -1)` - the `buildUnit` field OpenBW leaves at -1
        // (NEXT.md #53). Measured 2026-10-11: a Probe standing 1.3 tiles from its own Pylon
        // site reported `isConstructing=false` with `order=PlayerGuard`, so the status never
        // advanced, the worker was held forever, and builders sat on `NOT_STARTED` for 701
        // frames against 38 of `IN_PROGRESS` - while four Pylons DID get built.
        //
        // The reliable signal is the one the game cannot fake: a building of this type now
        // stands at the reserved tile. That is exactly what `buildingFinishedAtSite` asks, it
        // is already used for the completion half, and it reads the live unit list rather than
        // engine fields this engine fills differently from StarCraft.
        //
        // Demote a stalled IN_PROGRESS back to NOT_STARTED (NEXT #70): the flip
        // above fires on the order-echo `isConstructing` (true from the Build
        // order while the worker walks/stands at the site), so a warp the
        // engine drops server-side still flips - and then nothing recovers it:
        // BuilderManager drives only NOT_STARTED while isBuilder still claims
        // the worker, and CancelTooLong requires NOT_STARTED too. Demoting
        // hands the existing travel/issue/cancel/requeue machinery back a live
        // construction: a retry if the drop was transient, a cancel + requeue
        // elsewhere if the site is bad.
        if (construction.status() == ConstructionOrderStatus.IN_PROGRESS) {
            demoteStalledInProgress(construction, builder);
        }

        if (construction.status().equals(ConstructionOrderStatus.NOT_STARTED)) {
            boolean engineSaysStarted = builder != null
                && builder.isConstructing()
                && builderAtConstructionSite(builder, construction);

            if (engineSaysStarted || buildingFinishedAtSite(construction)) {
                construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            }
        }

        // Building doesn't exist yet, means builder is travelling to the construction place
        else if (builder != null && !builder.isMoving()) {
            if (construction.buildPosition() == null) {
                APosition positionToBuild = APositionFinder.findPositionForNew(
                    construction.builder(), construction.buildingType(), construction
                );
                construction.setPositionToBuild(positionToBuild);
            }
        }

        // === Not started =========================================

        if ((builder == null || builder.isDead()) && !construction.hasStarted()) {
            builder = construction.assignOptimalBuilder();
//            System.err.println("Builder was NULL for " + building + " / now = " + builder);
        }
//        builder = assignBuilderToConstructionIfNeeded(construction);

        // =========================================================
        // Check if both building and builder are destroyed

        if (!We.protoss()) {
            if (
                (builder == null || builder.isDead())
                    && (building == null || building.isDead())
            ) {
                ErrorLog.printMaxOncePerMinute("Builder and building are dead for " + construction + ", cancel");
                construction.cancel(building + " and builder are dead");
            }
        }
    }

//    private void assignBuilderToConstructionIfNeeded(Construction construction) {
//        AUnit builder = construction.builder();
//
//        if (!construction.hasStarted() && builder == null) {
//            construction.assignOptimalBuilder();
//        }
//
//        return builder;
//    }

    /**
     * Frames an IN_PROGRESS builder may stand idle at its site with no building
     * before the construction is demoted. A healthy Protoss warp frees the
     * worker within a frame or two of the issue (the worker walks away, the
     * building warps without it); a Terran builder reports
     * {@code isConstructing} for the whole build. Only a dropped warp sits out
     * this long, and the measured one sat out 2400 frames.
     */
    private static final int STALLED_IN_PROGRESS_DEMOTE_FRAMES = 90;

    /**
     * Demote a dead IN_PROGRESS construction back to NOT_STARTED.
     *
     * <p>Public as a seam so the rule is assertable without running a game
     * (NEXT #70). Every exclusion is a healthy shape that must NOT demote: a
     * constructing or travelling worker, a non-worker builder (Zerg morph), a
     * worker that walked away from a warping building, a recently arrived
     * worker, and a site with a building on it.</p>
     *
     * @return true when the construction was demoted.
     */
    public static boolean demoteStalledInProgress(Construction construction, AUnit builder) {
        if (construction.status() != ConstructionOrderStatus.IN_PROGRESS) return false;
        if (builder == null || builder.isDead() || !builder.isWorker()) return false;
        if (builder.isConstructing() || builder.isMoving()) return false;
        if (construction.buildPosition() == null) return false;
        if (builder.distTo(construction.buildPosition()) > 3.5) return false;
        if (!builder.lastPositionChangedMoreThanAgo(STALLED_IN_PROGRESS_DEMOTE_FRAMES)) return false;
        if (Select.ourWithUnfinishedOfType(construction.buildingType())
            .inRadius(2.5, construction.buildPosition())
            .notEmpty()
        ) return false;

        if (atlantis.config.env.Env.builderLog()) {
            System.err.println("DEMOTE_STALLED_IN_PROGRESS " + construction.buildingType().name()
                + " at=" + construction.buildPosition()
                + " builder=#" + builder.id()
                + " stoodStillFor=" + builder.lastPositionChangedAgo() + "f");
        }

        construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
        return true;
    }

    /**
     * Is a building of this type standing on the construction's tile?
     *
     * <p>
     * Used for two questions, and the name says which half it was written for: the completion
     * check cares whether it is finished, the status check (below) only whether it EXISTS. Both
     * ask the live unit list rather than the construction's own {@code buildingUnit()}
     * reference, because that reference is only filled from {@code builder.buildUnit()} - the
     * shared-memory field OpenBW leaves empty ({@code NEXT.md} #53).
     * </p>
     *
     * <p>
     * Asked of the live unit list rather than of the construction's own {@code buildingUnit()}
     * reference, because that reference is only filled from {@code builder.buildUnit()} -
     * the shared-memory field OpenBW leaves empty ({@code NEXT.md} #53). The unit list is
     * the source that works on both engines.
     * </p>
     *
     * <p>
     * The radius allows for the engine shifting a building whose footprint overlaps the
     * reserved origin, which is why it is not an exact tile comparison.
     * </p>
     */
    private static boolean buildingFinishedAtSite(Construction construction) {
        if (construction.buildPosition() == null) return false;

        AUnit finished = Select.ourOfTypeWithUnfinished(construction.buildingType())
            .inRadius(2.5, construction.buildPosition())
            .nearestTo(construction.buildPosition());

        return finished != null && finished.isCompleted();
    }

    /**
     * Is the builder standing on (or beside) the tile it is building on?
     *
     * <p>
     * Used only to decide whether a started construction should be marked in progress, so a
     * generous radius is right: the engine moves the worker onto the site as the building
     * goes up, and refusing to notice because it is one tile off would leave the deadlock
     * in place. What must NOT count is a worker still walking to a distant site.
     * </p>
     */
    private static boolean builderAtConstructionSite(AUnit builder, Construction construction) {
        if (construction.buildPosition() == null) return false;

        return builder.distTo(construction.buildPosition()) <= 3.5;
    }

    /**
     * The moment zerg drone starts building a building we're not detecting it without this method. This
     * method looks for constructions for which builder.type and builder.builds.type is the same, meaning that
     * the drone actually became a building (sweet metamorphosis, yay!).
     */
    private void handleZergConstructionsWhichBecameBuildings() {
        if (We.zerg()) {
            ArrayList<Construction> allOrders = ConstructionRequests.all();
            if (!allOrders.isEmpty()) {
                for (Construction construction : allOrders) {
                    AUnit builder = construction.builder();
                    if (construction.status().equals(ConstructionOrderStatus.NOT_STARTED)) {
                        if (builder != null) {
                            if (builder.is(construction.buildingType())) {
                                construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
                            }
                        }
                    }
                }
            }
        }
    }
}
