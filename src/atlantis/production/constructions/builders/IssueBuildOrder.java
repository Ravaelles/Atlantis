package atlantis.production.constructions.builders;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.Strategy;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.builders.BuilderDiagnostics;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.HasUnit;
import atlantis.units.actions.Actions;
import atlantis.util.AConsole;
import atlantis.util.log.ErrorLog;
import bwapi.TilePosition;

public class IssueBuildOrder extends HasUnit {
    public IssueBuildOrder(AUnit unit) {
        super(unit);
    }

    protected boolean considerIssuingNow(Construction construction) {
        AUnitType buildingType = construction.buildingType();

        AUnit builder = construction.builder();
        if (builder != null && builder.buildUnit() != null) {
            System.err.println("builder = " + builder);
            System.err.println("builder.buildUnit() = " + builder.buildUnit());
            System.err.println("builder.construction() = " + builder.construction());
            if (builder.construction() != null) {
                System.err.println("builder.productionOrder() = " + builder.construction().productionOrder());
            }
        }

//        if (We.protoss()) {
//            AUnit newBuilding = Select.ourUnfinished()
//                .ofType(buildingType)
//                .inRadius(2, unit).nearestTo(unit);
//            if (newBuilding != null) {
//                construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
//                construction.setBuilder(null);
//                unit.stop("Finished!");
//                return false;
//            }
//        }

        if (A.canAfford(buildingType.mineralPrice(), buildingType.gasPrice())) {
//            System.err.println("buildPosition PRE = " + construction.buildPosition());
//            APosition buildPosition = refreshBuildPosition(construction);
//            APosition buildPosition = refreshConstructionPositionIfNeeded(construction, buildingType);
            APosition buildPosition = construction.buildPosition();

            if (buildPosition == null) {
                ErrorLog.printMaxOncePerMinute("Cancel constr of " + buildingType + " because position null");
                return false;
            }

//            System.err.println("buildPosition POST = " + buildPosition);
//            System.err.println("buildPosition.translateByTiles(1, 1) = " + buildPosition.translateByTiles(1, 1));

            moveOtherUnitsOutOfConstructionPlace(buildPosition.translateByTiles(-3, -3));

            // If place is ok, unit isn't constructing, and we can afford it, issue the build command.
            buildPosition = (new GasBuildingFix(unit)).applyGasBuildingFixIfNeeded(buildPosition, buildingType);

            if (buildPosition == null) {
                if (construction.buildingUnit() == null) {
                    construction.cancel(buildingType + " has null buildPosition");
                    ErrorLog.printMaxOncePerMinute("Cancel construction of " + buildingType + " because position null");
                }

                construction.setBuilder(null);

                return false;
            }

//            if (Select.ourWithUnfinishedOfType(AtlantisRaceConfig.GAS_BUILDING).inRadius(3, buildPosition).notEmpty()) {
//                construction.cancel();
//                return false;
//            }

            if (!unit.isConstructing() || AGame.now() % 37 == 0) {
//                AConsole.println("_CONSTRUCT_ " + buildingType + " at " + buildTilePosition + ", construction: " + construction);

                if (Strategy.get().isExpansion() && A.supplyUsed() <= 17) {
                    RefreshConstructionPosition.refreshIfNeeded(construction);
                }

                if (unit.lastActionMoreThanAgo(20)) {
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
                    BuilderDiagnostics.logBuildAttempt(unit, construction, buildTilePosition, buildResult);
                    if (buildResult) {
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
                    BuilderDiagnostics.logBuildAttempt(unit, construction, buildTilePosition, buildResult);
                    if (buildResult) {
                        return true;
                    }
                }
//                System.err.println("unit.A = " + unit.action().name());
//                System.err.println("unit.B = " + unit.getLastCommandRaw().getType().name());
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
