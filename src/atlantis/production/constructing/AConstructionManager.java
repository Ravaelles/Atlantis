package atlantis.production.constructing;

import atlantis.config.AtlantisConfig;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

import java.util.ArrayList;
import java.util.Iterator;

public class AConstructionManager {

    // =========================================================
    
    /**
     * Manages all pending construction orders. Ensures builders are assigned to constructions, removes
     * finished objects etc.
     */
    public static void update() {
        for (Iterator<ConstructionOrder> iterator = ConstructionRequests.constructionOrders.iterator(); iterator.hasNext(); ) {
            ConstructionOrder constructionOrder =  iterator.next();
            checkForConstructionStatusChange(constructionOrder, constructionOrder.construction());
            checkForBuilderStatusChange(constructionOrder);
            handleConstructionUnderAttack(constructionOrder);
            handleConstructionThatLooksBugged(constructionOrder);
        }
    }

    // =========================================================

    /**
     * If builder has died when constructing, replace him with new one.
     */
    private static void checkForBuilderStatusChange(ConstructionOrder constructionOrder) {

        // When playing as Terran, it's possible that SCV gets killed and we should send another unit to
        // finish the construction.
        if (We.terran()) {
            AUnit builder = constructionOrder.builder();

            if (
                    (builder == null || !builder.exists() || !builder.isAlive())
            ) {
                constructionOrder.assignOptimalBuilder();

                builder = constructionOrder.builder();
                if (
                        builder != null && constructionOrder.construction() != null
                                && constructionOrder.status().equals(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS)
                ) {
                    builder.doRightClickAndYesIKnowIShouldAvoidUsingIt(constructionOrder.construction());
                    builder.setTooltipTactical("Resume");
                }
            }
        }
    }

    /**
     * If building is completed, mark construction as finished and remove it.
     */
    private static void checkForConstructionStatusChange(ConstructionOrder order, AUnit building) {
//        System.out.println("==============");
//        System.out.println(order.buildingType());
//        System.out.println(order.status());
//        System.out.println(order.builder());
//        if (building != null) {
//            System.out.println(building + " // " + building.hp() + " // " + building.isAlive() + " // " + building.exists());
//        }

        if (
                !We.zerg()
                && order.status() == ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS
                && order.startedAgo() >= 30
                && (building == null || !building.isAlive())
        ) {
            order.cancel();
            System.out.println("Building destroyed - cancel construction");
            return;
        }

        // =========================================================
        AUnit builder = order.builder();

        // ...change builder into building (it just happens, yeah, weird stuff)
        if (building == null || !building.exists()) {
            if (builder != null) {

                // If builder has changed its type and became Zerg Extractor
                if (!builder.is(AtlantisConfig.WORKER)) {

                    // Happens for Extractor
                    if (builder.buildType() == null || builder.buildType().equals(AUnitType.None)) {
                        building = builder;
                        order.setConstruction(builder);
                    }
                }

                // Builder did not change it's type so it's not Zerg Extractor case
                else {
//                    System.out.println("getBuildType = " + constructionOrder.getBuilder().getBuildType());
//                    System.out.println("getBuildUnit = " + constructionOrder.getBuilder().getBuildUnit());
//                    System.out.println("getTarget = " + constructionOrder.getBuilder().getTarget());
//                    System.out.println("getOrderTarget = " + constructionOrder.getBuilder().getOrderTarget());
//                    System.out.println("Constr = " + constructionOrder.getConstruction());
//                    System.out.println("Exists = " + constructionOrder.getBuilder().exists());
//                    System.out.println("Completed = " + constructionOrder.getBuilder().isCompleted());
                    AUnit buildUnit = builder.buildUnit();
                    if (buildUnit != null) {
                        building = buildUnit;
                        order.setConstruction(buildUnit);
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

            // Finished: building is completed, remove the construction order object
            if (building.isCompleted()) {
                order.setStatus(ConstructionOrderStatus.CONSTRUCTION_FINISHED);
                ConstructionRequests.removeOrder(order);
            } // In progress
            else if (order.status().equals(ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED)) {
                order.setStatus(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS);
            }
        }

        // Building doesn't exist yet, means builder is travelling to the construction place
        else if (builder != null && !builder.isMoving()) {
            if (order.buildPosition() == null) {
                APosition positionToBuild = APositionFinder.findPositionForNew(
                        order.builder(), order.buildingType(), order
                );
                order.setPositionToBuild(positionToBuild);
            }
        }

        // =========================================================
        // Check if both building and builder are destroyed
        if (order.builder() == null && order.construction() == null) {
            order.cancel();
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
                (!AGame.isPlayingAsProtoss() && ConstructionRequests.constructionOrderFor(worker) != null)) {
            return true;
        }

        for (ConstructionOrder constructionOrder : ConstructionRequests.constructionOrders) {
            if (worker.equals(constructionOrder.builder())) {
                
                // Pending Protoss buildings allow builder to go away
                // Terran and Zerg need to use the worker until construction is finished
                return !AGame.isPlayingAsProtoss() || !ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS
                        .equals(constructionOrder.status());
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
            ArrayList<ConstructionOrder> allOrders = ConstructionRequests.all();
            if (!allOrders.isEmpty()) {
                for (ConstructionOrder constructionOrder : allOrders) {
                    AUnit builder = constructionOrder.builder();
                    if (constructionOrder.status().equals(ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED)) {
                        if (builder != null) {
                            if (builder.is(constructionOrder.buildingType())) {
                                constructionOrder.setStatus(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void handleConstructionUnderAttack(ConstructionOrder order) {
        AUnit building = order.construction();
        
        // If unfinished building is under attack
        if (building != null && !building.isCompleted() && building.lastUnderAttackLessThanAgo(20)) {
            
            // If it has less than 71HP or less than 60% and is close to being finished
            if (building.hp() <= 32 || building.getRemainingBuildTime() <= 30) {
//                System.out.println("Construction under attack - cancel! " + building.lastUnderAttackLessThanAgo(20));
                order.cancel();
            }
        }
    }

    private static void handleConstructionThatLooksBugged(ConstructionOrder order) {
        if (order.status() != ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
            return;
        }

        if (order.builder() == null) {
            if (Count.workers() > 1) {
                System.out.println("Weird case, " + order.buildingType() + " has no builder. Cancel.");
            }
            order.cancel();
            return;
        }

        int timeout = 30 * (
                (order.buildingType().isBase() || order.buildingType().isCombatBuilding() ? 40 : 15)
                + (int) (1.7 * order.buildPosition().distTo(order.builder())
        ));

        if (AGame.now() - order.timeOrdered() > timeout) {
            System.out.println("Cancel construction of " + order.buildingType());
            order.cancel();
        }
    }

    public static ArrayList<AUnit> builders() {
        ArrayList<AUnit> units = new ArrayList<>();

        for (ConstructionOrder order : ConstructionRequests.constructionOrders) {
            if (order.builder() != null && order.builder().isAlive()) {
                units.add(order.builder());
            }
        }

        return units;
    }
}
