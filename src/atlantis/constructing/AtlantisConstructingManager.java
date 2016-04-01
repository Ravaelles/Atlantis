package atlantis.constructing;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.constructing.position.AtlantisPositionFinder;
import atlantis.information.AtlantisUnitInformationManager;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import bwapi.Position;
import bwapi.UnitType;

public class AtlantisConstructingManager {

    /**
     * List of all unfinished (started or pending) constructions.
     */
    private static ConcurrentLinkedQueue<ConstructionOrder> constructionOrders = new ConcurrentLinkedQueue<>();

    // =========================================================
    
    /**
     * Issues request of constructing new building. It will automatically find position and builder unit for
     * it.
     */
    public static void requestConstructionOf(AUnitType building) {
        requestConstructionOf(building, null, null);
    }

    /**
     * Issues request of constructing new building. It will automatically find position and builder unit for
     * it.
     */
    public static void requestConstructionOf(AUnitType building, Position near) {
        requestConstructionOf(building, null, near);
    }

    /**
     * Issues request of constructing new building. It will automatically find position and builder unit for
     * it.
     */
    public static void requestConstructionOf(AUnitType building, ProductionOrder order, Position near) {

        // Validate
        if (!building.isBuilding()) {
            throw new RuntimeException("Requested construction of not building!!! Type: " + building);
        }

        if (AtlantisSpecialConstructionManager.handledAsSpecialBuilding(building, order)) {
            return;
        }

        // =========================================================
        // Create ConstructionOrder object, assign random worker for the time being
        ConstructionOrder newConstructionOrder = new ConstructionOrder(building);
        newConstructionOrder.setProductionOrder(order);
        newConstructionOrder.assignRandomBuilderForNow();

        if (newConstructionOrder.getBuilder() == null) {
            if (AtlantisGame.getSupplyUsed() >= 7) {
                System.err.println("Builder is null, got damn it!");
            }
            return;
        }

        // =========================================================
        // Find place for new building
        Position positionToBuild = AtlantisPositionFinder.getPositionForNew(
                newConstructionOrder.getBuilder(), building, newConstructionOrder, near, 25
        );
//        System.out.println("@@ " + building + " at " + positionToBuild);

        // =========================================================
        // Successfully found position for new building
        AUnit optimalBuilder = null;
        if (positionToBuild != null) {

            // Update construction order with found position for building
            newConstructionOrder.setPositionToBuild(positionToBuild);

            // Assign optimal builder for this building
            newConstructionOrder.assignOptimalBuilder();
            // System.out.println("@@ BUILDER = " + optimalBuilder);

            // Add to list of pending orders
            constructionOrders.add(newConstructionOrder);

            // Rebuild production queue as new building is about to be built
            AtlantisGame.getBuildOrders().rebuildQueue();
        } // Couldn't find place for building! That's f'g bad.
        else {
            System.err.println("requestConstruction `" + building + "` FAILED! POSITION: " + positionToBuild
                    + " / BUILDER = " + optimalBuilder);
        }
    }

    // =========================================================
    
    /**
     * Manages all pending construction orders. Ensures builders are assigned to constructions, removes
     * finished objects etc.
     */
    public static void update() {
        for (ConstructionOrder constructionOrder : constructionOrders) {
            checkForConstructionStatusChange(constructionOrder, constructionOrder.getConstruction());

            // When playing as Terran, it's possible that SCV gets killed and we should send another unit to
            // finish the construction.
//            if (AtlantisGame.playsAsTerran()) {
            checkForBuilderStatusChange(constructionOrder, constructionOrder.getBuilder());
//            }
        }

        // =========================================================
        // Check if we should buy a base, because we have shitload of minerals
        if (AtlantisGame.hasMinerals(490) && Select.ourBases().count() <= 7
                && AtlantisConstructingManager.countNotStartedConstructionsOfType(AtlantisConfig.BASE) == 0) {
            requestConstructionOf(AtlantisConfig.BASE);
        }
    }

    // =========================================================
    
    /**
     * If builder has died when constructing, replace him with new one.
     */
    private static void checkForBuilderStatusChange(ConstructionOrder constructionOrder, AUnit builder) {
        if (builder == null || !builder.exists()) {
            constructionOrder.assignOptimalBuilder();
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

        // If ZERG change builder into building (it just happens, yeah, weird stuff)
        if (building == null || !building.exists()) {
            AUnit builder = constructionOrder.getBuilder();
            if (builder != null && !builder.getType().equals(AtlantisConfig.WORKER)) {
//                System.out.println("getBuildType = " + constructionOrder.getBuilder().getBuildType());
//                System.out.println("getBuildUnit = " + constructionOrder.getBuilder().getBuildUnit());
//                System.out.println("getTarget = " + constructionOrder.getBuilder().getTarget());
//                System.out.println("getOrderTarget = " + constructionOrder.getBuilder().getOrderTarget());
//                System.out.println("Constr = " + constructionOrder.getConstruction());
//                System.out.println("Exists = " + constructionOrder.getBuilder().exists());
//                System.out.println("Completed = " + constructionOrder.getBuilder().isCompleted());
//                AUnit buildUnit = AUnit.createFrom(builder.getType().);

                // Happens for Extractor
                if (constructionOrder.getBuilder().getBuildType().equals(AUnitType.None)) {
                    building = builder;
                    constructionOrder.setConstruction(builder);
                }
//                if (buildUnit != null) {
//                    building = buildUnit;
//                    constructionOrder.setConstruction(buildUnit);
//                }
            }
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

            // COMPLETED: building is finished, remove it from the list
            if (building.isCompleted()) {
                constructionOrder.setStatus(ConstructionOrderStatus.CONSTRUCTION_FINISHED);
                removeOrder(constructionOrder);

                // @FIX to fix bug with Refineries not being shown as created, because they're kinda changed.
                if (building.getType().isGasBuilding()) {
                    AtlantisUnitInformationManager.rememberUnit(building);
                }
            } // NOT YET COMPLETED
            else {
                constructionOrder.setStatus(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS);
            }
        } // Building doesn't exist yet, means builder is travelling to the construction place
        else {
            Position positionToBuild = AtlantisPositionFinder.getPositionForNew(
                    constructionOrder.getBuilder(), constructionOrder.getBuildingType(), constructionOrder
            );
            constructionOrder.setPositionToBuild(positionToBuild);
        }
        
        // =========================================================
        // Check if both building and builder are destroyed
        if (constructionOrder.getBuilder() == null && constructionOrder.getConstruction() == null) {
            constructionOrder.cancel();
        }
    }

    /**
     *
     */
    protected static void removeOrder(ConstructionOrder constructionOrder) {
        constructionOrders.remove(constructionOrder);
    }

    // =========================================================no
    // Public class access methods
    
    /**
     * Returns true if given worker has been assigned to construct new building or if the constructions is
     * already in progress.
     */
    public static boolean isBuilder(AUnit worker) {
        if (worker.isConstructing() || getConstructionOrderFor(worker) != null) {
            return true;
        }

        for (ConstructionOrder constructionOrder : constructionOrders) {
            if (worker.equals(constructionOrder.getBuilder())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns ConstructionOrder object for given builder.
     */
    public static ConstructionOrder getConstructionOrderFor(AUnit builder) {
        for (ConstructionOrder constructionOrder : constructionOrders) {
            if (builder.equals(constructionOrder.getBuilder())) {
                return constructionOrder;
            }
        }

        return null;
    }

    /**
     * If we requested to build building A and even assigned worker who's travelling to the building site,
     * it's still doesn't count as unitCreated. We need to manually count number of constructions and only
     * then, we can e.g. "count unstarted barracks constructions".
     */
    public static int countNotStartedConstructionsOfType(AUnitType type) {
        int total = 0;
        for (ConstructionOrder constructionOrder : constructionOrders) {
            if (constructionOrder.getStatus() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED
                    && constructionOrder.getBuildingType().equals(type)) {
                total++;
            }
        }

        // =========================================================
        // Special case for Overlord
        if (type.equals(AUnitType.Zerg_Overlord)) {
            total += Select.ourUnfinished().ofType(type).count();
        }

        return total;
    }

    public static int countNotFinishedConstructionsOfType(AUnitType type) {
        return Select.ourUnfinished().ofType(type).count()
                + countNotStartedConstructionsOfType(type);
    }

    /**
     * Returns how many buildings (or Overlords) of given type are currently being produced (started, but not
     * finished).
     */
    public static int countPendingConstructionsOfType(AUnitType type) {
        int total = 0;
        for (ConstructionOrder constructionOrder : constructionOrders) {
            if (constructionOrder.getStatus() == ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS
                    && constructionOrder.getBuildingType().equals(type)) {
                total++;
            }
        }

        // =========================================================
        // Special case for Overlord
        if (type.equals(AUnitType.Zerg_Overlord)) {
            total += Select.ourUnfinished().ofType(AUnitType.Zerg_Overlord).count();
        }

        return total;
    }

    /**
     * If we requested to build building A and even assigned worker who's travelling to the building site,
     * it's still doesn't count as unitCreated. We need to manually count number of constructions and only
     * then, we can e.g. "get unstarted barracks constructions".
     *
     * @param AUnitType type if null, then all not started constructions will be returned
     * @return
     */
    public static ArrayList<ConstructionOrder> getNotStartedConstructionsOfType(AUnitType type) {
        ArrayList<ConstructionOrder> notStarted = new ArrayList<>();
        for (ConstructionOrder constructionOrder : constructionOrders) {
            if (constructionOrder.getStatus() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED
                    && (type == null || constructionOrder.getBuildingType().equals(type))) {
                notStarted.add(constructionOrder);
            }
        }
        return notStarted;
    }

    /**
     * Returns every construction order that is active in this moment. It will include even those buildings
     * that haven't been started yet.
     *
     * @return
     */
    public static ArrayList<ConstructionOrder> getAllConstructionOrders() {
        return new ArrayList<>(constructionOrders);
    }

    /**
     * @return first int is number minerals, second int is number of gas required.
     */
    public static int[] countResourcesNeededForNotStartedConstructions() {
        int mineralsNeeded = 0;
        int gasNeeded = 0;
        for (ConstructionOrder constructionOrder : AtlantisConstructingManager.getNotStartedConstructionsOfType(null)) {
            mineralsNeeded += constructionOrder.getBuildingType().getMineralPrice();
            gasNeeded += constructionOrder.getBuildingType().getGasPrice();
        }
        int[] result = {mineralsNeeded, gasNeeded};
        return result;
    }

}
