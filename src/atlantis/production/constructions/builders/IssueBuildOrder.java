package atlantis.production.constructions.builders;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.Strategy;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.builders.BuilderDiagnostics;
import atlantis.units.special.SleepUntilFrameGate;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.HasUnit;
import atlantis.units.actions.Actions;
import atlantis.util.log.ErrorLog;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;
import bwapi.TilePosition;

public class IssueBuildOrder extends HasUnit {
    public IssueBuildOrder(AUnit unit) {
        super(unit);
    }

    protected boolean considerIssuingNow(Construction construction) {
        AUnitType buildingType = construction.buildingType();

        // PROBE (NEXT.md #59): was this method reached at all? "The build was never attempted"
        // has two very different causes - this path never ran (the mover claimed the worker),
        // or it ran and the gates inside refused. The Gateway soak could not be told apart
        // without it. Under BUILDER_LOG=1 only.
        if (atlantis.config.env.Env.builderLog() && A.everyNthGameFrame(30)) {
            EventLog.key(LogKey.ISSUE_BUILD_REACHED)
                .param("type", buildingType.name())
                .param("unit", unit.id())
                .param("action", unit.action().name())
                .param("lastActionAgo", unit.lastActionAgo(unit.action()))
                .param("lastCommandIssuedAgo", unit.lastCommandIssuedAgo())
                .param("constructing", unit.isConstructing())
                .param("moving", unit.isMoving())
                .log(A.now(), A.minSec());
        }

        AUnit builder = construction.builder();
        if (builder != null && builder.buildUnit() != null) {
            System.err.println("builder = " + builder);
            System.err.println("builder.buildUnit() = " + builder.buildUnit());
            System.err.println("builder.construction() = " + builder.construction());
            if (builder.construction() != null) {
                System.err.println("builder.productionOrder() = " + builder.construction().productionOrder());
            }
        }

        if (A.canAfford(buildingType.mineralPrice(), buildingType.gasPrice())) {
            APosition buildPosition = construction.buildPosition();

            if (buildPosition == null) {
                ErrorLog.printMaxOncePerMinute("Cancel constr of " + buildingType + " because position null");
                return false;
            }

            moveOtherUnitsOutOfConstructionPlace(buildPosition.translateByTiles(-3, -3));

            // If place is ok, unit isn't constructing, and we can afford it, issue the build command.
            buildPosition = (new GasBuildingFix(unit)).applyGasBuildingFixIfNeeded(buildPosition, buildingType);

            // The refinery tile is always the geyser's own tile (NEXT #62, measured
            // 20261013_101406: GAS_ACCEPTED win=0 exact + the server's
            // can_place_building/build_refinery demand a geyser under the sent
            // position's center - any shift fails server-side even when the
            // client accepts it). The 15-frame rotation experiment that proved
            // this is removed; this is the answer it found.

            if (buildPosition == null) {
                if (construction.buildingUnit() == null) {
                    construction.cancel(buildingType + " has null buildPosition");
                    ErrorLog.printMaxOncePerMinute("Cancel construction of " + buildingType + " because position null");
                }

                construction.setBuilder(null);

                return false;
            }

            if (!unit.isConstructing() || AGame.now() % 37 == 0) {

                if (Strategy.get().isExpansion() && A.supplyUsed() <= 17) {
                    RefreshConstructionPosition.refreshIfNeeded(construction);
                }

                // `lastCommandIssuedAgo`, NOT `lastActionMoreThanAgo(20)`.
                //
                // The action age is reset by the very path this worker came through:
                // `AUnitOrders.move` calls `setLastActionReceivedNow().setAction(...)` on every
                // invocation, so a builder whose last command was MOVE_BUILD has
                // `lastActionAgo(MOVE_BUILD)` driven back towards 0 constantly, and this gate
                // was false on nearly every frame - measured as a soak at `distToSite=1.3` with
                // the age sawtoothing 2, 5, 8, 11, 14, 17, 20 and exactly one build attempt in
                // the whole run, then "Gateway took too long (43s)".
                //
                // The command age is different in the way that matters: `lastCommandIssuedNow`
                // is called only when a command is ACTUALLY issued (`AUnitOrders.move` returns
                // early on its own debounce without calling it), so it measures "how long since
                // this worker last did anything new" - which is what the settle-before-build
                // wait was always meant to be. It is also the same class of fix the file already
                // carries twice: a debounce must not read a value the code path itself refreshes.
                if (unit.lastCommandIssuedAgo() >= 3 || unit.isConstructing()) {
                    TilePosition buildTilePosition = new TilePosition(buildPosition.tx(), buildPosition.ty());

                    if (TravelToConstruct.cantBuildHere(construction, buildingType)) {
                        RefreshConstructionPosition.refreshPosition(construction);
                        ErrorLog.printMaxOncePerMinute(
                            "Couldn't build " + buildingType + " at " + buildPosition
                                + ". Refreshed to " + construction.buildPosition()
                        );
                        buildTilePosition = new TilePosition(buildPosition.tx(), buildPosition.ty());
                    }

                    boolean buildResult = unit.build(buildingType, buildTilePosition, construction);
                    construction.setLastBuildAttemptNow();
                    BuilderDiagnostics.logBuildAttempt(unit, construction, buildTilePosition, buildResult);
                    if (atlantis.debug.DebugFlags.CHERRYVIS_BUILDER_ACTIONS) {
                        atlantis.cherryvis.CV.unitLog(
                            (buildResult ? "Build issued " : "Build refused ") + buildingType.name()
                                + " at " + buildTilePosition,
                            unit
                        );
                    }
                    if (buildResult) {
                        // Order-latency gate (owner recommendation 2026-10-12): the engine
                        // registers the Build after getRemainingLatencyFrames(); until then
                        // `lastCommand` still reads the older order and IssueBuildOrder
                        // re-attempted the build every frame against its own refusal.
                        SleepUntilFrameGate.holdAfterOrder(unit);
                        return true;
                    }

                    // REFUSED, and the reason is measured: the engine will not accept a Build
                    // while the worker's own order is still `Move`. A refused Gateway attempt
                    // reads `lastCommand=Move getOrder=Move isMoving=true distToBuild=0.6`,
                    // while every Pylon that succeeds reads `lastCommand=Build
                    // getOrder=PlaceBuilding` - because a Pylon is issued after the worker has
                    // arrived and stopped.
                    //
                    // So the sequence has to be explicit: stop first, build next frame. It
                    // must NOT be done by moving away (the worker is already ~1 tile from the
                    // site, which is why `moveToConstruct` stops issuing moves) and it must not
                    // be skipped, or the construction sits NOT_STARTED until
                    // `CancelTooLongConstructions` kills it - the measured 35-43s cancels.
                    // PROBE (NEXT.md #59): WHICH engine gate refused a refused build.
                    // `canCommand` and `canBuildHere` are already known true from
                    // logBuildAttempt, so what is left to separate is the type check and the
                    // `isBuilding || isInterruptible` term. Printed only under BUILDER_LOG=1,
                    // and only on a REFUSAL, so it costs nothing on the working path.
                    if (atlantis.config.env.Env.builderLog()) {
                        System.err.println("BUILD_REFUSED_WHY " + buildingType.name()
                            + " canBuild=" + unit.u().canBuild(buildingType.ut(), buildTilePosition)
                            + " isInterruptible=" + unit.u().isInterruptible()
                            + " isBuilding=" + unit.u().getType().isBuilding()
                            + " isMoving=" + unit.u().isMoving()
                            + " getOrder=" + unit.u().getOrder()
                            + " lastCommand=" + unit.lastCommandName());
                    }

                    // PROBE (NEXT.md #62): gas refusal terms, throttled and ungated like
                    // GAS_FIX so a plain run captures it. The tile math says the request
                    // equals the geyser tile and the terrain fallback passes the builder
                    // next to a geyser, so a refusal is elsewhere: the no-builder answer
                    // encodes the refinery exact-tile rule, the with-builder answer adds
                    // reachability, and canBuild is the full command gate.
                    if (buildingType.isGasBuilding()) {
                        ErrorLog.printMaxOncePerMinute("GAS_REFUSED tile=" + buildTilePosition
                            + " noBuilder=" + atlantis.Atlantis.game().canBuildHere(buildTilePosition, buildingType.ut())
                            + " withBuilder=" + atlantis.Atlantis.game().canBuildHere(buildTilePosition, buildingType.ut(), unit.u())
                            + " canBuild=" + unit.u().canBuild(buildingType.ut(), buildTilePosition)
                            + " lastCommand=" + unit.lastCommandName());
                    }

                    // MEASURED (610 refusals in one run): the engine refuses a Build while the
                    // worker's engine-side order is still the Move that brought it here -
                    // `distToBuild=1.3 isMoving=false action=BUILD lastCommand=Move` frame
                    // after frame, and the Pylon only ever succeeded after its walk order had
                    // expired. The Gateway twin of the same defect keeps isMoving=true
                    // (the worker slides around the site and never settles), so the old
                    // `&& !isMoving()` gate below never fired and the construction died at
                    // 35s as "took too long / buildable:true" (NEXT #59). Stopping a worker
                    // that is already within build reach is always safe: this branch only
                    // runs inside the 1.4-tile threshold, travel issues no moves here, and
                    // the >= 3 command-age gate above retries the build once settled.
                    if (unit.lastCommandName().equals("Move")) {
                        unit.stop("Clear stale Move before build");
                        return true;
                    }

                    if (
                        A.canAfford(buildingType.mineralPrice() - 8, buildingType.gasPrice())
                            && TravelToConstruct.cantBuildHere(construction, buildingType)
                    ) {
                        AbstractPositionFinder.clearCache();

                        RefreshConstructionPosition.refreshPosition(construction);
                        ErrorLog.printMaxOncePerMinute(
                            "Couldn't build " + buildingType + " so force-refreshed to " + construction.buildPosition()
                        );
                    }

                    buildResult = unit.build(buildingType, buildTilePosition, construction);
                    construction.setLastBuildAttemptNow();
                    BuilderDiagnostics.logBuildAttempt(unit, construction, buildTilePosition, buildResult);
                    if (buildResult) {
                        return true;
                    }
                }
                return true;
            }
        }

        unit.setTooltip("CantAfford" + buildingType.name() + "Yet");
        return true;
    }

    private void moveOtherUnitsOutOfConstructionPlace(APosition buildPosition) {
        for (AUnit unit : unit.friendsNear().groundUnits().inRadius(2.3, buildPosition).exclude(unit).list()) {
            unit.moveAwayFrom(buildPosition, 1, Actions.SPECIAL, "Construction!");
        }
    }
}
