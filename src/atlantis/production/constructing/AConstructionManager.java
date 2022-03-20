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
        for (Iterator<Construction> iterator = ConstructionRequests.constructions.iterator(); iterator.hasNext(); ) {
            Construction construction =  iterator.next();
            checkForConstructionStatusChange(construction, construction.construction());
            checkForBuilderStatusChange(construction);
            handleConstructionUnderAttack(construction);
            handleConstructionThatLooksBugged(construction);
        }
    }

    // =========================================================

    /**
     * If builder has died when constructing, replace him with new one.
     */
    private static void checkForBuilderStatusChange(Construction construction) {

        // When playing as Terran, it's possible that SCV gets killed and we should send another unit to
        // finish the construction.
        if (We.terran()) {
            AUnit builder = construction.builder();

            if (
                    (builder == null || !builder.exists() || !builder.isAlive())
            ) {
                construction.assignOptimalBuilder();

                builder = construction.builder();
                if (
                        builder != null && construction.construction() != null
                                && construction.status().equals(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS)
                ) {
                    builder.doRightClickAndYesIKnowIShouldAvoidUsingIt(construction.construction());
                    builder.setTooltipTactical("Resume");
                }
            }
        }
    }

    /**
     * If building is completed, mark construction as finished and remove it.
     */
    private static void checkForConstructionStatusChange(Construction order, AUnit building) {
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
//                    System.out.println("getBuildType = " + construction.getBuilder().getBuildType());
//                    System.out.println("getBuildUnit = " + construction.getBuilder().getBuildUnit());
//                    System.out.println("getTarget = " + construction.getBuilder().getTarget());
//                    System.out.println("getOrderTarget = " + construction.getBuilder().getOrderTarget());
//                    System.out.println("Constr = " + construction.getConstruction());
//                    System.out.println("Exists = " + construction.getBuilder().exists());
//                    System.out.println("Completed = " + construction.getBuilder().isCompleted());
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
                (!AGame.isPlayingAsProtoss() && ConstructionRequests.constructionFor(worker) != null)) {
            return true;
        }

        for (Construction construction : ConstructionRequests.constructions) {
            if (worker.equals(construction.builder())) {
                
                // Pending Protoss buildings allow builder to go away
                // Terran and Zerg need to use the worker until construction is finished
                return !AGame.isPlayingAsProtoss() || !ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS
                        .equals(construction.status());
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

    private static void handleConstructionUnderAttack(Construction order) {
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

    private static void handleConstructionThatLooksBugged(Construction order) {
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

        for (Construction order : ConstructionRequests.constructions) {
            if (order.builder() != null && order.builder().isAlive()) {
                units.add(order.builder());
            }
        }

        return units;
    }
}
