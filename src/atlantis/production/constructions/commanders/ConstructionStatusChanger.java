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
import atlantis.util.log.LogLevel;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

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
            AConsole.errPrintln(LogLevel.HIGH, "Building destroyed - cancel construction");

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
        //
        // NEXT #57: not for unworked adoptions. A freshly accepted construction
        // whose builder never issued is still owed - finishing it off because
        // an earlier building stands within 2.5 tiles drops demand the queue
        // believes is covered, and the per-frame re-request that follows is
        // the same-tile accept loop. Attempted or started work keeps the
        // proximity finish; builderless rows keep it too (no work pending).
        if (shouldProximityFinish(construction)) {
            construction.setStatus(ConstructionOrderStatus.FINISHED);
            // Named, not silent: this removal used to leave no twin, so a
            // re-request loop (same tile, totalOfType pinned at 1) read as
            // "constructions never advance past PREPARING" with no author.
            // The covering tile names WHICH standing building the site was
            // finished against (NEXT #150: 147 Gateway finishes against an
            // unidentified coverer) - the type+tile alone cannot tell a
            // same-tile duplicate from a footprint graze or a misread
            // warping unit.
            EventLog.key(LogKey.CONSTRUCTION_CANCEL)
                .param("type", construction.buildingType().name())
                .param("cid", construction.id())
                .param("oid", construction.productionOrder() == null ? null : construction.productionOrder().id())
                .param("reason", "proximity finish: standing " + construction.buildingType().name()
                    + " covers " + construction.buildPosition())
                .param("tile", construction.buildPosition() == null ? "-"
                    : construction.buildPosition().toString())
                .param("covering", coveringBuildingTile(construction))
                .log(A.now(), A.minSec());
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
        // STARTING: a NOT_STARTED construction with a live assigned builder
        // that has not started constructing is travelling, not waiting.
        // Named here (owner 2026-10-19), in one place: producers keep
        // writing NOT_STARTED, the changer refines it the next frame.
        // Runs after the proximity finish above (which exempts
        // builder-held NOT_STARTED rows) and before the advance below
        // (which takes both). A constructing builder or a standing
        // building is the advancer's case, not this one.
        refineToStartingIfTravelling(construction, builder);

        if (construction.status() == ConstructionOrderStatus.IN_PROGRESS) {
            demoteStalledInProgress(construction, builder);
        }

        if (construction.status().equals(ConstructionOrderStatus.NOT_STARTED)
            || construction.status().equals(ConstructionOrderStatus.STARTING)) {
            advanceToInProgressIfStarted(construction, builder);
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

        // A dead or missing builder on a STARTING construction re-arms it:
        // hasStarted() is true for STARTING (travelling counts as started),
        // so without the explicit clause a builder that dies en route would
        // hold the site forever. The status stays STARTING when a new
        // builder is found (still travelling), and only demote paths clear.
        if ((builder == null || builder.isDead())
            && (!construction.hasStarted()
                || construction.status() == ConstructionOrderStatus.STARTING)) {
            builder = construction.assignOptimalBuilder();
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
     * Refine a NOT_STARTED construction with a live assigned builder that
     * has not started constructing into STARTING (travelling). The seam
     * mirrors advance/demote: the rule pins without running a game.
     *
     * @return true when the construction refined to STARTING.
     */
    public static boolean refineToStartingIfTravelling(Construction construction, AUnit builder) {
        if (construction.status() != ConstructionOrderStatus.NOT_STARTED) return false;
        if (construction.buildPosition() == null) return false;
        if (builder == null || builder.isDead() || builder.isConstructing()) return false;
        if (buildingFinishedAtSite(construction)) return false;

        construction.setStatus(ConstructionOrderStatus.STARTING);

        return true;
    }

    /**
     * Advance a NOT_STARTED construction whose builder has started (or
     * whose building already stands on the tile) to IN_PROGRESS.
     *
     * <p>Public as a seam: the status advance is assertable without running
     * a game (NEXT #150 - a Gateway that stays NOT_STARTED forever while
     * its builder works is exactly this transition not firing), the same
     * way {@link #demoteStalledInProgress} pins the way back.</p>
     *
     * @return true when the status advanced.
     */
    public static boolean advanceToInProgressIfStarted(Construction construction, AUnit builder) {
        if (construction.status() != ConstructionOrderStatus.NOT_STARTED
            && construction.status() != ConstructionOrderStatus.STARTING) return false;

        boolean engineSaysStarted = builder != null
            && builder.isConstructing()
            && builderAtConstructionSite(builder, construction);

        if (engineSaysStarted || buildingFinishedAtSite(construction)) {
            construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);

            return true;
        }
        return false;
    }

    /**
     * Frames an IN_PROGRESS builder may stand idle at its site with no building
     * before the construction is demoted. A healthy Protoss warp frees the
     * worker within a frame or two of the issue (the worker walks away, the
     * building warps without it); a Terran builder reports
     * {@code isConstructing} for the whole build. Only a dropped warp sits out
     * this long, and the measured one sat out 2400 frames.
     */
    /**
     * Frames after an engine build attempt during which a stall demote is
     * suppressed. A healthy warp shows its building within about a minute of
     * the accept; a dead one still demotes after this window.
     */
    private static final int STALLED_IN_PROGRESS_DEMOTE_FRAMES = 90;

    /**
     * Frames after an engine build attempt during which demote holds fire even
     * for a standstill. Covers accept-to-entity latency with margin; the
     * measured misfire demoted 7 frames after an accepted attempt on a 258f
     * pre-attempt standstill clock.
     */
    private static final int DEMOTE_HOLD_AFTER_ATTEMPT_FRAMES = 150;

    /**
     * Frames after IN_PROGRESS start past which a still-unfinished warp
     * with a dead builder is named frozen. Longest legal Protoss warp is
     * 1800 frames (Nexus, measured jbwapi-2.2.0), so 2400 leaves margin
     * for accept latency without ever catching a healthy warp.
     */
    private static final int FROZEN_WARP_OBSERVED_FRAMES = 2400;

    /**
     * HP-observation window for the frozen-warp progress signal (NEXT
     * #150 WO-150b): a warping building whose HP does not move inside
     * this window is stalled, not progressing. Reporting granularity
     * only - no demote reads it (the WO forbids deciding on the twin;
     * the signal first proves itself in live twins).
     */
    private static final int FROZEN_WARP_STATIC_FRAMES = 240;

    /**
     * Demote a dead IN_PROGRESS construction back to NOT_STARTED.
     *
     * <p>Public as a seam so the rule is assertable without running a game
     * (NEXT #70). Every exclusion is a healthy shape that must NOT demote: a
     * constructing or travelling worker, a non-worker builder (Zerg morph), a
     * worker that walked away from a warping building, a recently arrived
     * worker, a young IN_PROGRESS, a recent build attempt, and a site with a
     * building on it.</p>
     *
     * @return true when the construction was demoted.
     */
    public static boolean demoteStalledInProgress(Construction construction, AUnit builder) {
        if (construction.status() != ConstructionOrderStatus.IN_PROGRESS) return false;
        if (construction.buildPosition() == null) return false;

        AUnit warping = warpingNearSite(construction);
        if (warping != null) {
            construction.noteWarpHp(A.now(), warping.hp());
            twinFrozenWarp(construction, builder, warping);

            return false;
        }
        if (construction.lastBuildAttemptAgo() < DEMOTE_HOLD_AFTER_ATTEMPT_FRAMES) return false;

        // No builder at all (measured on Wine: setStatus(IN_PROGRESS) releases
        // the Protoss builder the moment the status flips - correct for a real
        // warp, fatal for a refused attempt's order echo, which leaves
        // IN_PROGRESS + no builder + no building + no recovery). Re-arm once
        // old enough that a real warp would have shown its building; the
        // NOT_STARTED state reassigns a builder on the next pass.
        if (builder == null || builder.isDead() || !builder.isWorker()) {
            if (construction.startedAgo() < STALLED_IN_PROGRESS_DEMOTE_FRAMES) return false;

            logDemote(construction, "builder gone");
            twinDemote(construction, "builder gone");
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);

            return true;
        }

        // B134 cid60 shape (NEXT #150): live builder walked away, no warping
        // entity, IN_PROGRESS forever - the 3.5-tile / movement / standstill
        // shields below hold young travellers (pinned), the age-cap re-arms
        // ancient entries regardless of where the builder went. Threshold is
        // the frozen-warp age (2400f > longest legal 1800f Nexus warp +
        // accept-latency margin), so no healthy warp trips it: a real warp
        // shows its entity within a minute and exits through the warping
        // branch above. Terran constructing stays shielded (builder works
        // the whole build).
        if (construction.startedAgo() >= FROZEN_WARP_OBSERVED_FRAMES) {
            if (builder.isConstructing()) return false;

            logDemote(construction, "stalledAge=" + construction.startedAgo() + "f");
            twinDemote(construction, "stalledAge=" + construction.startedAgo() + "f");
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);

            return true;
        }

        if (builder.isConstructing() || builder.isMoving()) return false;
        if (builder.distTo(construction.buildPosition()) > 3.5) return false;
        if (!builder.lastPositionChangedMoreThanAgo(STALLED_IN_PROGRESS_DEMOTE_FRAMES)) return false;

        logDemote(construction, "stoodStillFor=" + builder.lastPositionChangedAgo() + "f");
        twinDemote(construction, "stoodStillFor=" + builder.lastPositionChangedAgo() + "f");
        construction.setStatus(ConstructionOrderStatus.NOT_STARTED);

        return true;
    }

    private static AUnit warpingNearSite(Construction construction) {
        return Select.ourWithUnfinishedOfType(construction.buildingType())
            .inRadius(2.5, construction.buildPosition()).first();
    }

    /**
     * Names a frozen warp (NEXT #150, cid-331 shape): IN_PROGRESS older
     * than any legal build, builder dead, warping entity still on the
     * site. The demote path above is shielded by its own self-match, so
     * without this row the freeze is silent. Names only, never removes
     * or demotes; twinned once per construction.
     */
    private static void twinFrozenWarp(Construction construction, AUnit builder, AUnit warping) {
        if (construction.isFrozenWarpTwinned()) return;
        if (builder == null || !builder.isDead()) return;
        if (construction.startedAgo() < FROZEN_WARP_OBSERVED_FRAMES) return;

        construction.markFrozenWarpTwinned();
        boolean hpStatic;
        try {
            hpStatic = construction.isWarpHpStatic(FROZEN_WARP_STATIC_FRAMES, A.now());
        } catch (Exception | Error ignored) {
            hpStatic = false;
        }
        EventLog.key(LogKey.FROZEN_WARP)
            .param("cid", construction.id())
            .param("type", construction.buildingType() == null ? "-" : construction.buildingType().name())
            .param("tile", construction.buildPosition().toString())
            .param("builder", "#" + builder.id())
            .param("startedAgo", construction.startedAgo())
            .param("warpingHp", warping.hp())
            .param("hpStatic", hpStatic)
            .log(A.now(), A.minSec());
    }

    private static void logDemote(Construction construction, String detail) {
        if (atlantis.config.env.Env.builderLog()) {
            System.err.println("DEMOTE_STALLED_IN_PROGRESS " + construction.buildingType().name()
                + " at=" + construction.buildPosition() + " " + detail);
        }
    }

    /**
     * Names a stall demote (NEXT #70 + #150 lifecycle): a stalled
     * IN_PROGRESS construction handed back to NOT_STARTED so the
     * travel/issue/cancel/requeue machinery recovers it. Names only,
     * never demotes by itself - the two call sites above do that.
     * Fires per demote (a re-armed construction can stall again),
     * unlike the once-per-construction frozen-warp twin.
     */
    private static void twinDemote(Construction construction, String reason) {
        EventLog.key(LogKey.DEMOTE_STALLED)
            .param("cid", construction.id())
            .param("type", construction.buildingType() == null ? "-" : construction.buildingType().name())
            .param("tile", construction.buildPosition() == null ? "-" : construction.buildPosition().toString())
            .param("reason", reason)
            .log(A.now(), A.minSec());
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
    /**
     * Public as a seam: tests pin the finish-vs-churn contract here (a
     * waiting PREPARING site next to a sibling building is owed demand,
     * not a duplicate) rather than through a game.
     */
    public static boolean shouldProximityFinish(Construction construction) {
        if (construction.status().isTerminal()) return false;
        // Dead site, not a waiting one: the same finished building on the
        // exact same tiles means this demand is already standing (measured
        // Wine: own warped Core on its tiles, canBuild000=false, builder
        // held 369f, bank 600). This fires above the exemptions below on
        // purpose - they exist for sites that may still build, and this
        // one cannot be built twice.
        if (finishedBuildingOnSameTiles(construction)) return true;
        if (unworkedAdoption(construction)) return false;

        return buildingFinishedAtSite(construction);
    }

    private static boolean unworkedAdoption(Construction construction) {
        // A picked-but-unproducible site holds no worker (nothing to free -
        // the deadlock this finish cures cannot occur) and its order is not
        // producible yet (nothing is owed downstream). A same-type building
        // nearby is then the normal street-grid shape - a sibling on the
        // next slot - not proof this demand is covered (measured: Gateway
        // PREPARING re-picked 6x in 8s next to its finished sibling). Only
        // builderless sites are exempt: a builder held on one stays visible
        // to the old path. An occupied site itself still fails loudly at
        // adoption/issue time instead of spinning here.
        if (construction.status().isWaiting()
            && construction.builder() == null) return true;
        return construction.status() == ConstructionOrderStatus.NOT_STARTED
            && construction.builder() != null && !construction.builder().isDead()
            && construction.buildWasNeverAttempted();
    }

    /**
     * The stale-duplicate killer (Wine: a Core construction survived its
     * own warped building standing on its exact tiles - builder held
     * 369f, canBuild000=false, bank 600): the same finished building type
     * on the exact same tiles means this demand is already standing in
     * the game, so the construction finishes regardless of the
     * exemptions below, which exist for sites that may still build.
     *
     * <p>Exact tiles, not nearness and not mere overlap: same type means
     * same footprint, so origin equality is tile equality. A sibling
     * grazing the footprint stays exempt (its demand is live), and an
     * unfinished (warping) overlap waits conservatively - a dropped warp
     * must not silently eat demand.</p>
     */
    private static boolean finishedBuildingOnSameTiles(Construction construction) {
        APosition site = construction.buildPosition();
        if (site == null) return false;

        for (AUnit unit : Select.all().list()) {
            if (unit == null || !unit.isAlive()) continue;
            if (!unit.isABuilding() || !unit.isCompleted()) continue;
            if (!construction.buildingType().equals(unit.type())) continue;
            if (unit.tx() == site.tx() && unit.ty() == site.ty()) return true;
        }
        return false;
    }

    /**
     * Names the covering building for the proximity-finish twin: the same
     * two loops as the finish predicates, in the same order, but returning
     * the tile instead of a boolean. Read-only, same sources, so it cannot
     * disagree with the decision it annotates.
     */
    private static String coveringBuildingTile(Construction construction) {
        APosition site = construction.buildPosition();
        if (site == null) return "-";

        for (AUnit unit : Select.all().list()) {
            if (unit == null || !unit.isAlive()) continue;
            if (!unit.isABuilding() || !unit.isCompleted()) continue;
            if (!construction.buildingType().equals(unit.type())) continue;
            if (unit.tx() == site.tx() && unit.ty() == site.ty()) {
                return "[" + unit.tx() + "," + unit.ty() + "]";
            }
        }

        int w = construction.buildingType().getTilesWidth();
        int h = construction.buildingType().getTilesHeights();
        for (AUnit unit : Select.ourOfTypeWithUnfinished(construction.buildingType()).list()) {
            if (unit == null || !unit.isCompleted()) continue;
            if (atlantis.production.constructions.PrepareConstructionSite.footprintsOverlap(
                site.tx(), site.ty(), w, h,
                unit.tx(), unit.ty(),
                unit.type().getTilesWidth(), unit.type().getTilesHeights()
            )) return "[" + unit.tx() + "," + unit.ty() + "]";
        }

        return "-";
    }

    private static boolean buildingFinishedAtSite(Construction construction) {        if (construction.buildPosition() == null) return false;

        // Footprint overlap, not nearness: a finished sibling on the next
        // slot (measured: Pylon [119,18] next to warped [118,16], centers
        // 2.2 apart, footprints disjoint) is live packed-base demand, not
        // coverage - finishing it silently re-requested the same tile 5x
        // in a minute (totalOfType pinned at 1, no cancel twin anywhere).
        // The engine-shift tolerance this radius was for is exactly
        // overlap (a shifted warp still covers part of the ordered
        // footprint), so nothing real is lost.
        APosition site = construction.buildPosition();
        int w = construction.buildingType().getTilesWidth();
        int h = construction.buildingType().getTilesHeights();
        for (AUnit unit : Select.ourOfTypeWithUnfinished(construction.buildingType()).list()) {
            if (unit == null || !unit.isCompleted()) continue;
            if (atlantis.production.constructions.PrepareConstructionSite.footprintsOverlap(
                site.tx(), site.ty(), w, h,
                unit.tx(), unit.ty(),
                unit.type().getTilesWidth(), unit.type().getTilesHeights()
            )) return true;
        }
        return false;
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
