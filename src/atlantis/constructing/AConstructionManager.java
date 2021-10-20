package atlantis.constructing;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.constructing.position.APositionFinder;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

import java.util.ArrayList;
import java.util.Iterator;

public class AConstructionManager {

    // =========================================================
    
    /**
     * Manages all pending construction orders. Ensures builders are assigned to constructions, removes
     * finished objects etc.
     */
    public static void update() {
        for (Iterator<ConstructionOrder> iterator = AConstructionRequests.constructionOrders.iterator(); iterator.hasNext(); ) {
            ConstructionOrder constructionOrder =  iterator.next();
            checkForConstructionStatusChange(constructionOrder, constructionOrder.getConstruction());
            checkForBuilderStatusChange(constructionOrder, constructionOrder.getBuilder());
            handleConstructionUnderAttack(constructionOrder);
            handleConstructionThatLooksBugged(constructionOrder);
        }
    }

    // =========================================================

    /**
     * If builder has died when constructing, replace him with new one.
     */
    private static void checkForBuilderStatusChange(ConstructionOrder constructionOrder, AUnit builder) {

        // When playing as Terran, it's possible that SCV gets killed and we should send another unit to
        // finish the construction.
        if (AGame.isPlayingAsTerran()) {
            if (builder == null || !builder.exists() || !builder.isAlive()) {
                constructionOrder.assignOptimalBuilder();
            }
        }
    }

    /**
     * If building is completed, mark construction as finished and remove it.
     */
    private static void checkForConstructionStatusChange(ConstructionOrder constructionOrder, AUnit building) {
//        System.out.println("==============");
//        System.out.println(constructionOrder.getBuildingType());
//        System.out.println(constructionOrder.getStatus());
//        System.out.println(constructionOrder.getBuilder());

        // =========================================================
        AUnit builder = constructionOrder.getBuilder();

        // ...change builder into building (it just happens, yeah, weird stuff)
        if (building == null || !building.exists()) {
            if (builder != null) {

                // If builder has changed its type and became Zerg Extractor
                if (!builder.getType().equals(AtlantisConfig.WORKER)) {

                    // Happens for Extractor
                    if (constructionOrder.getBuilder().getBuildType().equals(AUnitType.None)) {
                        building = builder;
                        constructionOrder.setConstruction(builder);
                    }
                } // Builder did not change it's type so it's not Zerg Extractor case
                else {
//                    System.out.println("getBuildType = " + constructionOrder.getBuilder().getBuildType());
//                    System.out.println("getBuildUnit = " + constructionOrder.getBuilder().getBuildUnit());
//                    System.out.println("getTarget = " + constructionOrder.getBuilder().getTarget());
//                    System.out.println("getOrderTarget = " + constructionOrder.getBuilder().getOrderTarget());
//                    System.out.println("Constr = " + constructionOrder.getConstruction());
//                    System.out.println("Exists = " + constructionOrder.getBuilder().exists());
//                    System.out.println("Completed = " + constructionOrder.getBuilder().isCompleted());
                    AUnit buildUnit = builder.getBuildUnit();
                    if (buildUnit != null) {
                        building = buildUnit;
                        constructionOrder.setConstruction(buildUnit);
                    }
                }
            }
        }

        // If playing as ZERG...
        if (AGame.isPlayingAsZerg()) {
            handleZergConstructionsWhichBecameBuildings();
        }

        // =========================================================
//        if (building != null) {
//            System.out.println("==============");
//            System.out.println(constructionOrder.getPositionToBuild());
//            System.out.println(building.getType());
//            System.out.println(building);
//            System.out.println(building.isExists());
//            System.out.println(constructionOrder.getStatus());
//            System.out.println();
//            System.out.println();
//        }
        // If building exists
        if (building != null) {

            // Finished: building is completed, remove the construction order object
            if (building.isCompleted()) {
                constructionOrder.setStatus(ConstructionOrderStatus.CONSTRUCTION_FINISHED);
                AConstructionRequests.removeOrder(constructionOrder);
            } // In progress
            else if (constructionOrder.getStatus().equals(ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED)) {
                constructionOrder.setStatus(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS);
            }
        } // Building doesn't exist yet, means builder is travelling to the construction place
        else if (builder != null && !builder.isMoving()) {
            if (constructionOrder.getPositionToBuild() == null) {
                APosition positionToBuild = APositionFinder.getPositionForNew(
                        constructionOrder.getBuilder(), constructionOrder.getBuildingType(), constructionOrder
                );
                constructionOrder.setPositionToBuild(positionToBuild);
            }
        }

        // =========================================================
        // Check if both building and builder are destroyed
        if (constructionOrder.getBuilder() == null && constructionOrder.getConstruction() == null) {
            constructionOrder.cancel();
        }
    }

    // =========================================================no
    // Public class access methods
    
    /**
     * Returns true if given worker has been assigned to construct new building or if the constructions is
     * already in progress.
     */
    public static boolean isBuilder(AUnit worker) {
        if (worker.isConstructing() || 
                (!AGame.isPlayingAsProtoss() && AConstructionRequests.getConstructionOrderFor(worker) != null)) {
            return true;
        }

        for (ConstructionOrder constructionOrder : AConstructionRequests.constructionOrders) {
            if (worker.equals(constructionOrder.getBuilder())) {
                
                // Pending Protoss buildings allow builder to go away
                // Terran and Zerg need to use the worker until construction is finished
                return !AGame.isPlayingAsProtoss() || !ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS
                        .equals(constructionOrder.getStatus());
            }
        }

        return false;
    }

    // === Zerg ========================================
    
    /**
     * The moment zerg drone starts building a building we're not detecting it without this method. This
     * method looks for constructions for which builder.type and builder.builds.type is the same, meaning that
     * the drone actually became a building (sweet metamorphosis, yay!).
     */
    private static void handleZergConstructionsWhichBecameBuildings() {
        if (AGame.isPlayingAsZerg()) {
            ArrayList<ConstructionOrder> allOrders = AConstructionRequests.getAllConstructionOrders();
            if (!allOrders.isEmpty()) {
                for (ConstructionOrder constructionOrder : allOrders) {
                    AUnit builder = constructionOrder.getBuilder();
                    if (constructionOrder.getStatus().equals(ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED)) {
                        if (builder != null) {
                            if (builder.getType().equals(constructionOrder.getBuildingType())) {
                                constructionOrder.setStatus(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void handleConstructionUnderAttack(ConstructionOrder order) {
        AUnit building = order.getConstruction();
        
        // If unfinished building is under attack
        if (building != null && !building.isCompleted() && building.isUnderAttack()) {
            
            // If it has less than 71HP or less than 60% and is close to being finished
            if (building.getHP() <= 70 
                    || (building.getRemainingBuildTime() <= 2 && building.HPPercent() < 60)
                    || (building.getRemainingBuildTime() <= 3 && building.type().isMilitaryBuilding() 
                        && building.HPPercent() < 60)) {
                order.cancel();
            }
        }
    }

    private static void handleConstructionThatLooksBugged(ConstructionOrder order) {
        if (order.getStatus() != ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
            return;
        }

        int timeout = 30 * (
                (order.getBuildingType().isBase() ? 25 : 9)
                + (int) (1.4 * order.getPositionToBuild().distanceTo(order.getBuilder())
        ));

        if (AGame.getTimeFrames() - order.getFrameOrdered() > timeout) {
            System.out.println("Cancel construction of " + order.getBuildingType());
            order.cancel();
        }
    }

}
