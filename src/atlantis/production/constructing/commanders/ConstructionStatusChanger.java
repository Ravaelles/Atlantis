package atlantis.production.constructing.commanders;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionOrderStatus;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

import java.util.ArrayList;
import java.util.Iterator;

public class ConstructionStatusChanger extends Commander {
    @Override
    protected void handle() {
        for (Iterator<Construction> iterator = ConstructionRequests.constructions.iterator(); iterator.hasNext(); ) {
            Construction construction = iterator.next();

            checkForConstructionStatusChange(construction);
        }
    }

    private void checkForConstructionStatusChange(Construction construction) {
        AUnit building = construction.buildingUnit();

        if (
            !We.zerg()
                && construction.status() == ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS
                && construction.startedAgo() >= 30
                && (building == null || !building.isAlive())
        ) {
            construction.cancel();
            A.println("Building destroyed - cancel construction");
            return;
        }

        // =========================================================
        AUnit builder = construction.builder();

        // ...change builder into building (it just happens, yeah, weird stuff)
        if (building == null || !building.exists()) {
            if (builder != null) {

                // If builder has changed its type and became Zerg Extractor
                if (!builder.is(AtlantisRaceConfig.WORKER)) {

                    // Happens for Extractor
                    if (builder.buildType() == null || builder.buildType().equals(AUnitType.None)) {
                        building = builder;
                        construction.setBuild(builder);
                    }
                }

                // Builder did not change it's type so it's not Zerg Extractor case
                else {
                    AUnit buildUnit = builder.buildUnit();
                    if (buildUnit != null) {
                        building = buildUnit;
                        construction.setBuild(buildUnit);
                    }
                }
            }
        }

        // If playing as ZERG...
        if (AGame.isPlayingAsZerg()) {
            handleZergConstructionsWhichBecameBuildings();
        }

        // If building exists
        if (building != null) {

            // Finished: building is completed, remove the construction object
            if (building.isCompleted()) {
                construction.setStatus(ConstructionOrderStatus.CONSTRUCTION_FINISHED);
                ConstructionRequests.removeOrder(construction);
            }
            // In progress
            else if (construction.status().equals(ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED)) {
                construction.setStatus(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS);
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
            System.err.println("Builder was NULL for " + building + " / now = " + builder);
        }
//        builder = assignBuilderToConstructionIfNeeded(construction);

        // =========================================================
        // Check if both building and builder are destroyed
        if ((builder == null || builder.isDead()) && (building == null || building.isDead())) {
            construction.cancel();
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
     * The moment zerg drone starts building a building we're not detecting it without this method. This
     * method looks for constructions for which builder.type and builder.builds.type is the same, meaning that
     * the drone actually became a building (sweet metamorphosis, yay!).
     */
    private void handleZergConstructionsWhichBecameBuildings() {
        if (AGame.isPlayingAsZerg()) {
            ArrayList<Construction> allOrders = ConstructionRequests.all();
            if (!allOrders.isEmpty()) {
                for (Construction construction : allOrders) {
                    AUnit builder = construction.builder();
                    if (construction.status().equals(ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED)) {
                        if (builder != null) {
                            if (builder.is(construction.buildingType())) {
                                construction.setStatus(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS);
                            }
                        }
                    }
                }
            }
        }
    }
}
