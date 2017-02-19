package atlantis.debug;

import atlantis.Atlantis;
import atlantis.AGame;
import atlantis.buildings.managers.AGasManager;
import atlantis.combat.AtlantisCombatEvaluator;
import atlantis.combat.squad.AtlantisSquadManager;
import atlantis.combat.squad.missions.MissionAttack;
import atlantis.combat.squad.missions.MissionDefend;
import atlantis.constructing.AtlantisConstructionManager;
import atlantis.constructing.ConstructionOrder;
import atlantis.constructing.ConstructionOrderStatus;
import atlantis.constructing.position.AtlantisPositionFinder;
import atlantis.constructing.position.TerranPositionFinder;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.information.AtlantisMap;
import atlantis.information.UnitData;
import atlantis.production.ProductionOrder;
import atlantis.production.orders.AtlantisBuildOrdersManager;
import atlantis.scout.AScoutManager;
import atlantis.strategy.AEnemyStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import atlantis.util.AtlantisUtilities;
import atlantis.util.CodeProfiler;
import atlantis.util.ColorUtil;
import atlantis.util.PositionUtil;
import atlantis.workers.AWorkerManager;
import atlantis.wrappers.APosition;
import atlantis.wrappers.MappingCounter;
import bwapi.Color;
import bwapi.Game;
import bwapi.Position;
import bwapi.Text.Size.Enum;
import bwapi.Unit;
import bwta.Region;
import java.util.ArrayList;
import java.util.Map;

/**
 * Here you can include code that will draw extra informations over units etc.
 */
public class APainter {

    public static final int MODE_NO_PAINTING = 1;
    public static final int MODE_PARTIAL_PAINTING = 2;
    public static final int MODE_FULL_PAINTING = 3;

    public static int paintingMode = MODE_NO_PAINTING;
//    public static int paintingMode = MODE_PARTIAL_PAINTING;
//    public static int paintingMode = MODE_FULL_PAINTING;

    // =========================================================
    private static Game bwapi;
    private static int sideMessageTopCounter = 0;
    private static int sideMessageMiddleCounter = 0;
    private static int sideMessageBottomCounter = 0;
    private static int prevTotalFindBuildPlace = 0;

    // =========================================================
    /**
     * Executed once per frame, at the end of all other actions.
     */
    public static void paint() {
//        if (1 < 2) {
//            return;
//        }

        sideMessageTopCounter = 0;
        sideMessageBottomCounter = 0;
        bwapi = Atlantis.getBwapi();

        // === Dynamic PAINTING MODE ===============================
//        paintingMode = (AGame.getSupplyUsed() >= 29 ? MODE_FULL_PAINTING : MODE_NO_PAINTING);
        // =========================================================
        if (paintingMode == MODE_NO_PAINTING) {
            return;
        }

        // === PARTIAL PAINTING ====================================
//        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_PAINTING);
        bwapi.setTextSize(Enum.Default);

        paintInfo();
        paintKilledAndLost();
        paintProductionQueue();
        paintSidebarConstructionsPending();
        paintConstructionPlaces();
        //        paintUnitCounters();

        if (paintingMode == MODE_PARTIAL_PAINTING) {
            CodeProfiler.endMeasuring(CodeProfiler.ASPECT_PAINTING);
            return;
        }

        // =========================================================
        bwapi.setTextSize(Enum.Small);

        paintCodeProfiler();
//        paintTestSupplyDepotLocationsNearMain();
        paintConstructionProgress();
        paintEnemyRegionDetails();
        paintImportantPlaces();
        paintColoredCirclesAroundUnits();
        paintBuildingHealth();
        paintWorkersAssignedToBuildings();
        paintUnitsBeingTrainedInBuildings();
        paintBarsUnderUnits();
        paintEnemyDiscovered();
        paintCombatUnits();
        paintEnemyCombatUnits();
        paintTooltipsOverUnits();

        // =========================================================
//        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_PAINTING);
    }

    // =========================================================
    // Hi-level
    /**
     * Paint focus point for global attack mission etc.
     */
    private static void paintInfo() {

        // Time
        paintSideMessage("Time: " + AGame.getTimeSeconds() + "s", Color.Grey);

        // =========================================================
        // Global mission
        paintSideMessage("Enemy strategy: " + (AEnemyStrategy.isEnemyStrategyKnown()
                ? AEnemyStrategy.getEnemyStrategy() : "Unknown"),
                AEnemyStrategy.isEnemyStrategyKnown() ? Color.Yellow : Color.Red);
        paintSideMessage("Mission: " + AtlantisSquadManager.getAlphaSquad().getMission().getName(), Color.White);
        paintSideMessage("Enemy base: " + AtlantisEnemyUnits.getEnemyBase(), Color.White);

        // =========================================================
        // Focus point
        APosition focusPoint = MissionAttack.getInstance().getFocusPoint();
        AUnit mainBase = Select.mainBase();
        String desc = "";
        if (focusPoint != null && mainBase != null) {
            desc = "(" + ((int) mainBase.distanceTo(focusPoint)) + " tiles)";
        }
        paintSideMessage("Focus point: " + focusPoint + desc, Color.Blue, 0);

        // =========================================================
        paintSideMessage("Combat squad size: " + AtlantisSquadManager.getAlphaSquad().size(), Color.Yellow, 0);

        // =========================================================
        // Gas workers
//        paintSideMessage("Find build. place: " + AtlantisPositionFinder.totalRequests,
//                prevTotalFindBuildPlace != AtlantisPositionFinder.totalRequests ? Color.Red : Color.Grey);
//        prevTotalFindBuildPlace = AtlantisPositionFinder.totalRequests;
        paintSideMessage("Gas workers: " + AGasManager.defineMinGasWorkersPerBuilding(), Color.Grey);
        paintSideMessage("Reserved minerals: " + AtlantisBuildOrdersManager.getMineralsNeeded(), Color.Grey);
        paintSideMessage("Reserved gas: " + AtlantisBuildOrdersManager.getGasNeeded(), Color.Grey);
    }

    /**
     * Painting for combat units can be a little different. Put here all the related code.
     */
    private static void paintCombatUnits() {
        for (AUnit unit : Select.ourCombatUnits().listUnits()) {
            APosition unitPosition = unit.getPosition();
            double combatEval = AtlantisCombatEvaluator.evaluateSituation(unit);

            // =========================================================
            // === Paint life bars bars over wounded units
            // =========================================================
            if (unit.isWounded()) {
                int boxWidth = 20;
                int boxHeight = 4;
                int boxLeft = unitPosition.getX() - boxWidth / 2;
                int boxTop = unitPosition.getY() + 23;

                Position topLeft = new APosition(boxLeft, boxTop);

                // =========================================================
                // Paint box
                int healthBarProgress = boxWidth * unit.getHitPoints() / (unit.getMaxHitPoints() + 1);
                bwapi.drawBoxMap(topLeft, new APosition(boxLeft + boxWidth, boxTop + boxHeight), Color.Red, true);
                bwapi.drawBoxMap(topLeft, new APosition(boxLeft + healthBarProgress, boxTop + boxHeight), Color.Green, true);

                // =========================================================
                // Paint box borders
                bwapi.drawBoxMap(topLeft, new APosition(boxLeft + boxWidth, boxTop + boxHeight), Color.Black, false);
            }

            // =========================================================
            // === Paint targets for combat units
            // =========================================================
            APosition targetPosition = unit.getTargetPosition();
//            if (targetPosition == null) {
//                targetPosition = unit.getTarget().getPosition();
//            }
//            if (targetPosition != null && unit.distanceTo(targetPosition) <= 15) {
//                paintLine(unitPosition, targetPosition, (unit.isAttacking() ? Color.Green : Color.Red));
//            }

            // =========================================================
            // === Combat Evaluation Strength
            // =========================================================
//            if (combatEval < 10) {
            double eval = AtlantisCombatEvaluator.evaluateSituation(unit, true, false);
//                if (eval < 999) {
//                    String combatStrength = eval >= 10 ? (ColorUtil.getColorString(Color.Green) + ":)")
//                            : AtlantisCombatEvaluator.getEvalString(unit);
            String combatStrength = ColorUtil.getColorString(Color.Green)
                    + AtlantisCombatEvaluator.getEvalString(unit, eval);
            paintTextCentered(new APosition(unitPosition.getX(), unitPosition.getY() - 15), combatStrength, null);
//                }

            // =========================================================
            // === Paint circle around units with zero ground weapon 
            // === cooldown equal to 0 - meaning they can shoot now
            // =========================================================
//                if (unit.getGroundWeaponCooldown() == 0) {
//                    paintCircle(unitPosition, 14, Color.White);
//                }
//            }
        }
    }

    /**
     * Paint extra information about visible enemy combat units.
     */
    private static void paintEnemyCombatUnits() {
        for (AUnit unit : Select.enemy().combatUnits().listUnits()) {
            paintCircle(unit, unit.getType().getDimensionLeft() * 2, Color.Red);
            paintCircle(unit, unit.getType().getDimensionLeft() * 2 - 1, Color.Red);

            APosition unitPosition = unit.getPosition();
            double eval = (int) AtlantisCombatEvaluator.evaluateSituation(unit, true, true);
//            if (eval < 999) {
//                String combatStrength = eval >= 10 ? (ColorUtil.getColorString(Color.Green) + ":)")
//                        : AtlantisCombatEvaluator.getEvalString(unit);
            String combatStrength = ColorUtil.getColorString(Color.Red)
                    + AtlantisCombatEvaluator.getEvalString(unit, eval);
            paintTextCentered(new APosition(unitPosition.getX(), unitPosition.getY() - 15), combatStrength, null);
//            }
        }
    }

    /**
     * Paints small progress bars over units that have cooldown.
     */
    private static void paintBarsUnderUnits() {
//        for (AUnit unit : Select.ourCombatUnits().listUnits()) {
//
//            // =========================================================
//            // === Paint life bars bars over wounded units
//            // =========================================================
//            if (UnitUtil.getHPPercent(unit) < 100) {
//                int boxWidth = 20;
//                int boxHeight = 4;
//                int boxLeft = unit.getPosition().getX() - boxWidth / 2;
//                int boxTop = unit.getPosition().getY() + 23;
//
//                Position topLeft = new APosition(boxLeft, boxTop);
//
//                // =========================================================
//                // Paint box
//                int healthBarProgress = boxWidth * unit.getHitPoints() / (unit.getMaxHitPoints() + 1);
//                bwapi.drawBoxMap(topLeft, new APosition(boxLeft + boxWidth, boxTop + boxHeight), Color.Red, true);
//                bwapi.drawBoxMap(topLeft, new APosition(boxLeft + healthBarProgress, boxTop + boxHeight), Color.Green, true);
//
//                // =========================================================
//                // Paint box borders
//                bwapi.drawBoxMap(topLeft, new APosition(boxLeft + boxWidth, boxTop + boxHeight), Color.Black, false);
//            }

        // =========================================================
        // === Paint cooldown progress bars over units
        // =========================================================
//            if (unit.getGroundWeaponCooldown() > 0) {
//                int cooldownWidth = 20;
//                int cooldownHeight = 4;
//                int cooldownLeft = unit.getPX() - cooldownWidth / 2;
//                int cooldownTop = unit.getPY() + 23;
//                String cooldown = Color.getColorString(Color.Yellow) + "(" + unit.getGroundWeaponCooldown() + ")";
//
//                Position topLeft = new APosition(cooldownLeft, cooldownTop);
//
//                // =========================================================
//                // Paint box
//                int cooldownProgress = cooldownWidth * unit.getGroundWeaponCooldown()
//                        / (unit.getType().getGroundWeapon().getDamageCooldown() + 1);
//                bwapi.drawBox(topLeft, new APosition(cooldownLeft + cooldownProgress, cooldownTop + cooldownHeight),
//                        Color.Brown, true, false);
//
//                // =========================================================
//                // Paint box borders
//                bwapi.drawBox(topLeft, new APosition(cooldownLeft + cooldownWidth, cooldownTop + cooldownHeight),
//                        Color.Black, false, false);
//
//                // =========================================================
//                // Paint label
////                paintTextCentered(new APosition(cooldownLeft + cooldownWidth - 4, cooldownTop), cooldown, false);
//            }
        // =========================================================
        // === Paint battle squad
        // =========================================================
//            if (unit.getSquad() != null) {
//                paintTextCentered(new APosition(unit.getPX(), unit.getPY() + 3), Color.getColorString(Color.Grey)
//                        + "#" + unit.getSquad().getID(), false);
//            }
        // =========================================================
        // === Paint num of other units around this unit
        // =========================================================
//            int ourAround = Select.ourCombatUnits().inRadius(1.7, unit).count();
//            paintTextCentered(new APosition(unit.getPX(), unit.getPY() - 15), Color.getColorString(Color.Orange)
//                    + "(" + ourAround + ")", false);
//            // =========================================================
//            // === Combat Evaluation Strength
//            // =========================================================
//            if (AtlantisCombatEvaluator.evaluateSituation(unit) < 10) {
//                double eval = AtlantisCombatEvaluator.evaluateSituation(unit);
//                if (eval < 999) {
//                    String combatStrength = eval >= 10 ? (ColorUtil.getColorString(Color.Green) + "++")
//                            : AtlantisCombatEvaluator.getEvalString(unit);
//                    paintTextCentered(new APosition(unit.getPosition().getX(), unit.getPosition().getY() - 15), combatStrength, null);
//                }
//            }
//        }
//
//        for (AUnit unit : Select.enemy().combatUnits().listUnits()) {
//            double eval = AtlantisCombatEvaluator.evaluateSituation(unit);
//            if (eval < 999) {
//                String combatStrength = eval >= 10 ? (ColorUtil.getColorString(Color.Green) + "++")
//                        : AtlantisCombatEvaluator.getEvalString(unit);
//                paintTextCentered(new APosition(unit.getPosition().getX(), unit.getPosition().getY() - 15), combatStrength, null);
//            }
//        }
    }

    /**
     * Paints important choke point near the base.
     */
    private static void paintImportantPlaces() {

        // === Handle UMT ==========================================
        if (AGame.isUmtMode()) {
            return;
        }

        // =========================================================
        APosition position;

        // Main DEFEND focus point
        position = MissionAttack.getInstance().getFocusPoint();
        if (position != null) {
            position = MissionDefend.getInstance().getFocusPoint();
            paintCircle(position, 20, Color.Orange);
            paintCircle(position, 19, Color.Orange);
            paintTextCentered(position, "DEFEND", Color.Orange);
        }

        // Mission ATTACK focus point
        position = MissionAttack.getInstance().getFocusPoint();
        if (position != null) {
            paintCircle(position, 20, Color.Red);
            //        paintCircle(position, 19, Color.Black);
            paintTextCentered(position, "ATTACK", Color.Red);
        }
    }

    /**
     * Paints list of units we have in top left corner.
     */
    private static void paintUnitCounters() {
        // Unfinished
        MappingCounter<AUnitType> unitTypesCounter = new MappingCounter<>();
        for (AUnit unit : Select.ourUnfinishedRealUnits().listUnits()) {
//        for (AUnit unit : Select.our().listUnits()) {
            unitTypesCounter.incrementValueFor(unit.getType());
        }

        Map<AUnitType, Integer> counters = unitTypesCounter.map();
        counters = AtlantisUtilities.sortByValue(counters, false);
        boolean paintedMessage = false;
        for (AUnitType unitType : counters.keySet()) {
            paintSideMessage("+" + counters.get(unitType) + " " + unitType.toString(), Color.Blue, 0);
            paintedMessage = true;
        }

        if (paintedMessage) {
            paintSideMessage("", Color.White, 0);
        }

        // =========================================================
        // Finished
        unitTypesCounter = new MappingCounter<>();
        for (AUnit unit : Select.our().listUnits()) {
            unitTypesCounter.incrementValueFor(unit.getType());
        }

        counters = unitTypesCounter.map();
        counters = AtlantisUtilities.sortByValue(counters, false);
        for (AUnitType unitType : counters.keySet()) {
            if (!unitType.isBuilding()) {
                paintSideMessage(counters.get(unitType) + "x " + unitType.toString(), Color.Grey, 0);
            }
        }
        paintSideMessage("", Color.White, 0);
    }

    /**
     * Paints next units to build in top left corner.
     */
    private static void paintProductionQueue() {
        paintSideMessage("", Color.White);
        paintSideMessage("Prod. queue:", Color.White);

        // Display units currently in production
        for (AUnit unit : Select.ourNotFinished().listUnits()) {
            AUnitType type = unit.getType();
            if (type.equals(AUnitType.Zerg_Egg)) {
                type = unit.getBuildType();
            }
            paintSideMessage(type.getShortName(), Color.Green);
        }

        // Display units that should be produced right now or any time
        ArrayList<ProductionOrder> produceNow = AGame.getBuildOrders().getThingsToProduceRightNow(
                AtlantisBuildOrdersManager.MODE_ALL_ORDERS
        );
        for (ProductionOrder order : produceNow) {
            paintSideMessage(order.getShortName(), Color.Yellow);
        }

        // Display next units to produce
        ArrayList<ProductionOrder> fullQueue = AGame.getBuildOrders().getProductionQueueNext(
                5 - produceNow.size());
        for (int index = produceNow.size(); index < fullQueue.size(); index++) {
            ProductionOrder order = fullQueue.get(index);
            if (order != null && order.getShortName() != null) {
                if (order.getUnitOrBuilding() != null
                        && !AGame.hasBuildingsToProduce(order.getUnitOrBuilding(), true)) {
                    continue;
                }
                paintSideMessage(order.getShortName(), Color.Red);
            }
        }

        // Paint info if queues are empty
        if (produceNow.isEmpty() && fullQueue.isEmpty()) {
            paintSideMessage("Nothing to produce - it seems to be a bug", Color.Red);
        }
    }

    /**
     * Paints all pending contstructions, including those not yet started, even if only in the AI memory.
     */
    private static void paintSidebarConstructionsPending() {
        int yOffset = 220;
        ArrayList<ConstructionOrder> allOrders = AtlantisConstructionManager.getAllConstructionOrders();
        if (!allOrders.isEmpty()) {
            paintSideMessage("Constructing (" + allOrders.size() + ")", Color.White, yOffset);
            for (ConstructionOrder constructionOrder : allOrders) {
                Color color = null;
                switch (constructionOrder.getStatus()) {
                    case CONSTRUCTION_NOT_STARTED:
                        color = Color.Red;
                        break;
                    case CONSTRUCTION_IN_PROGRESS:
                        color = Color.Blue;
                        break;
                    case CONSTRUCTION_FINISHED:
                        color = Color.Teal;
                        break;
                    default:
                        color = Color.Purple;
                        break;
                }

                String status = constructionOrder.getStatus().toString().replace("CONSTRUCTION_", "");
                String builder = (constructionOrder.getBuilder() + "").replace("AUnit(", "");
                paintSideMessage(constructionOrder.getBuildingType().getShortName()
                        + ", " + status + ", " + builder, color, yOffset);
            }
        }
    }

    /**
     * Paints places where buildings that do not yet exist are planned to be placed.
     */
    private static void paintConstructionPlaces() {
        for (ConstructionOrder order : AtlantisConstructionManager.getAllConstructionOrders()) {
            if (order.getStatus() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
                APosition positionToBuild = order.getPositionToBuild();
                AUnitType buildingType = order.getBuildingType();
                if (positionToBuild == null || buildingType == null) {
                    continue;
                }

                // Paint box
                paintRectangle(positionToBuild,
                        buildingType.getTileWidth() * 32, buildingType.getTileHeight() * 32, Color.Brown);

                // Draw X
                paintLine(
                        PositionUtil.translateByPixels(positionToBuild, buildingType.getTileWidth() * 32, 0),
                        PositionUtil.translateByPixels(positionToBuild, 0, buildingType.getTileHeight() * 32),
                        Color.Brown
                );
                paintLine(positionToBuild,
                        buildingType.getTileWidth() * 32,
                        buildingType.getTileHeight() * 32,
                        Color.Brown
                );

                // Draw text
                paintTextCentered(
                        positionToBuild.translateByPixels(buildingType.getDimensionLeft(), 69),
                        buildingType.getShortName(), Color.Brown
                );
            }
        }
    }

    /**
     * Paints circles around units which mean what's their mission.
     */
    private static void paintColoredCirclesAroundUnits() {
        for (AUnit unit : Select.ourRealUnits().listUnits()) {
            if (unit.isWorker() && (unit.isGatheringMinerals() || unit.isGatheringGas())) {
                continue;
            }

            APosition unitPosition = unit.getPosition();
            APosition targetPosition = unit.getTargetPosition();
            int unitRadius = unit.getType().getDimensionLeft();

            // STARTING ATTACK
            if (unit.isStartingAttack()) {
                paintCircle(unit, unitRadius - 5, Color.Brown);
                paintCircle(unit, unitRadius - 4, Color.Brown);
                paintCircle(unit, unitRadius - 3, Color.Brown);
            }
            // ATTACK FRAME
            if (unit.isAttackFrame()) {
                paintCircle(unit, 1, Color.Red);
                paintCircle(unit, 2, Color.Red);
                paintCircle(unit, 4, Color.Red);
                paintCircle(unit, 5, Color.Red);
                paintCircle(unit, 8, Color.Red);
                paintCircle(unit, 9, Color.Red);
            }
            // STUCK
            if (unit.isStuck()) {
                unit.setTooltip("STUCK");
                paintCircle(unit, 2, Color.Teal);
                paintCircle(unit, 4, Color.Teal);
                paintCircle(unit, 6, Color.Teal);
                paintCircle(unit, 8, Color.Teal);
                paintCircle(unit, 10, Color.Teal);
            }
            // ATTACKING
            if (unit.isAttacking()) {
                paintCircle(unit, unitRadius - 3, Color.Orange);
                paintCircle(unit, unitRadius - 2, Color.Orange);
            }
            // MOVE
            if (unit.isMoving()) {
                paintCircle(unit, unitRadius - 3, Color.Blue);
                paintCircle(unit, unitRadius - 2, Color.Blue);
                if (unit.getTarget() != null) {
                    APainter.paintLine(unit.getPosition(), unit.getTarget().getPosition(), Color.Blue);
                }
            }
//            // CONSTRUCTING
//            if (unit.isConstructing()) {
//                paintCircle(unit, 6, Color.Teal);
//                paintCircle(unit, 5, Color.Teal);
//            }

            // RUN
            if (unit.isRunning()) {
//                paintLine(unit.getPosition(), unit.getRunManager().getRunToPosition(), Color.Blue);

                // =========================================================
                // === Paint white flags over running units
                // =========================================================
                int flagWidth = 15;
                int flagHeight = 8;
                int dy = 12;

                paintLine(unitPosition, targetPosition, Color.Blue); // Where unit is running to

                paintRectangleFilled(unitPosition.translateByPixels(0, -flagHeight - dy),
                        flagWidth, flagHeight, Color.White); // White flag
                paintRectangle(unitPosition.translateByPixels(0, -flagHeight - dy),
                        flagWidth, flagHeight, Color.Grey); // Flag border
                paintRectangleFilled(unitPosition.translateByPixels(-1, --flagHeight - dy),
                        2, flagHeight, Color.Grey); // Flag stick
            }
//            
//            // Paint #ID
//            paintTextCentered(unit, "#" + unit.getID(), Color.Cyan);
            // BUILDER
//            if (AtlantisConstructingManager.isBuilder(unit)) {
//                paintCircle(unit, 15, Color.Teal);
//                paintCircle(unit, 13, Color.Teal);
//                paintCircle(unit, 11, Color.Teal);
//            }
            // Current COMMAND
//            if (!unit.isMoving()) {
//                paintTextCentered(unit, unit.getLastCommand().getUnitCommandType().toString(), Color.Purple);
//            }
            // =========================================================
            Color color = Color.Grey;
            if (unit.getUnitAction() != null) {
//                if (unit.getUnitAction().equals(UnitActions.MOVE)) {
//                    color = Color.Teal;
//                } else if (unit.getUnitAction().isAttacking()) {
//                    color = Color.Orange;
//                } else if (unit.getUnitAction().equals(UnitActions.RUN)) {
//                    color = Color.Brown;
//                } else if (unit.getUnitAction().equals(UnitActions.RETREAT)) {
//                    color = Color.Brown;
//                } else if (unit.getUnitAction().equals(UnitActions.HEAL)) {
//                    color = Color.Purple;
//                } else if (unit.getUnitAction().equals(UnitActions.BUILD)) {
//                    color = Color.Purple;
//                } else if (unit.getUnitAction().equals(UnitActions.REPAIR)) {
//                    color = Color.Purple;
//                }
//            else if (unit.getUnitAction().equals(UnitActions.)) {
//                color = Color.;
//            }
//            else if (unit.getUnitAction().equals(UnitActions.)) {
//                color = Color.;
//            }
            }

//            if (!unit.isWorker() && !unit.isGatheringMinerals() && !unit.isGatheringGas()) {
//                paintCircle(unit, unit.getType().getDimensionLeft() + unit.getType().getDimensionRight(), color);
//                paintCircle(unit, unit.getType().getDimensionLeft() - 2 + unit.getType().getDimensionRight(), color);
//            }
            if (unit.isWorker() && unit.isIdle()) {
                paintCircle(unit, 10, Color.Black);
                paintCircle(unit, 8, Color.Black);
                paintCircle(unit, 6, Color.Black);
                paintCircle(unit, 4, Color.Black);
            }
        }
    }

    /**
     * Paints progress bar with percent of completion over all buildings under construction.
     */
    private static void paintConstructionProgress() {
        for (AUnit unit : Select.ourBuildingsIncludingUnfinished().listUnits()) {
            if (unit.isCompleted()) {
                continue;
            }

            String stringToDisplay;

            int labelMaxWidth = 56;
            int labelHeight = 6;
            int labelLeft = unit.getPosition().getX() - labelMaxWidth / 2;
            int labelTop = unit.getPosition().getY() + 13;

            double progress = (double) unit.getHitPoints() / unit.getMaxHitPoints();
            int labelProgress = (int) (1 + 99 * progress);
//            String color = AtlantisUtilities.assignStringForValue(
//                    progress,
//                    1.0,
//                    0.0,
//                    new String[]{ColorUtil.getColorString(Color.Red), ColorUtil.getColorString(Color.Yellow),
//                        ColorUtil.getColorString(Color.Green)});
            stringToDisplay = labelProgress + "%";

            // Paint box
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop),
                    new APosition(labelLeft + labelMaxWidth * labelProgress / 100, labelTop + labelHeight),
                    Color.Blue,
                    true
            );
            //bwapi.drawBox(new APosition(labelLeft, labelTop), new APosition(labelLeft + labelMaxWidth * labelProgress / 100, labelTop + labelHeight), Color.Blue, true, false);

            // Paint box borders
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop),
                    new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight),
                    Color.Black,
                    false
            );
            //bwapi.drawBox(new APosition(labelLeft, labelTop), new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight), Color.Black, false, false);

            // Paint label
            paintTextCentered(new APosition(labelLeft + labelMaxWidth * 50 / 100, labelTop - 3), stringToDisplay, false);

            // Display name of unit
            String name = unit.getBuildType().getShortName();
            paintTextCentered(new APosition(unit.getPosition().getX(), unit.getPosition().getY() - 1), 
                    name, Color.Green);
        }
    }

    /**
     * For buildings not 100% healthy, paints its hit points using progress bar.
     */
    private static void paintBuildingHealth() {
        for (AUnit unit : Select.ourBuildings().listUnits()) {
            if (unit.getHitPoints() >= unit.getMaxHitPoints()) { //isWounded()
                continue;
            }
            int labelMaxWidth = 56;
            int labelHeight = 4;
            int labelLeft = unit.getPosition().getX() - labelMaxWidth / 2;
            int labelTop = unit.getPosition().getY() + 13;

            double hpRatio = (double) unit.getHitPoints() / unit.getMaxHitPoints();
            int hpProgress = (int) (1 + 99 * hpRatio);

            Color color = Color.Green;
            if (hpRatio < 0.66) {
                color = Color.Yellow;
                if (hpRatio < 0.33) {
                    color = Color.Red;
                }
            }

            // Paint box
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop),
                    new APosition(labelLeft + labelMaxWidth * hpProgress / 100, labelTop + labelHeight),
                    color,
                    true
            );
            //bwapi.drawBox(new APosition(labelLeft, labelTop), new APosition(labelLeft + labelMaxWidth * hpProgress / 100, labelTop + labelHeight), color, true, false);

            // Paint box borders
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop),
                    new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight),
                    Color.Black,
                    false
            );
            //bwapi.drawBox(new APosition(labelLeft, labelTop), new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight), Color.Black, false, false);
        }
    }

    /**
     * Paints the number of workers that are gathering to this building.
     */
    private static void paintWorkersAssignedToBuildings() {
        bwapi.setTextSize(Enum.Default);
        for (AUnit building : Select.ourBuildings().listUnits()) {

            // Paint text
            int workers = AWorkerManager.getHowManyWorkersGatheringAt(building);
            if (workers > 0) {
                String workersAssigned = workers + " WRK";
                paintTextCentered(PositionUtil.translateByPixels(building.getPosition(), 0, -15), workersAssigned, Color.Blue);
            }
        }
        bwapi.setTextSize(Enum.Small);
    }

    /**
     * If buildings are training units, it paints what unit is trained and the progress.
     */
    private static void paintUnitsBeingTrainedInBuildings() {
        for (AUnit unit : Select.ourBuildingsIncludingUnfinished().listUnits()) {
            if (!unit.getType().isBuilding() || !unit.isTrainingAnyUnit()) {
                continue;
            }

            int labelMaxWidth = 100;
            int labelHeight = 10;
            int labelLeft = unit.getPosition().getX() - labelMaxWidth / 2;
            int labelTop = unit.getPosition().getY() + 5;

            int operationProgress = 1;
            AUnit trained = unit.getBuildUnit();
            String trainedUnitString = "";
            if (trained != null) {
                operationProgress = trained.getHPPercent(); // trained.getHP() * 100 / trained.getMaxHP();
                trainedUnitString = trained.getShortName();
            }

            // Paint box
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop),
                    new APosition(labelLeft + labelMaxWidth * operationProgress / 100, labelTop + labelHeight),
                    Color.White,
                    true
            );
            //bwapi.drawBox(new APosition(labelLeft, labelTop), new APosition(labelLeft + labelMaxWidth * operationProgress / 100, labelTop + labelHeight), Color.White, true, false);

            // Paint box borders
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop),
                    new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight),
                    Color.Black,
                    false
            );
            //bwapi.drawBox(new APosition(labelLeft, labelTop), new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight), Color.Black, false, false);

            // =========================================================
            // Display label
            paintTextCentered(
                    new APosition(unit.getPosition().getX() - 4 * trainedUnitString.length(), unit.getPosition().getY() + 16),
                    ColorUtil.getColorString(Color.White) + trainedUnitString, false
            );
        }
    }

    /**
     * Paints number of units killed and lost in the top right corner.
     */
    private static void paintKilledAndLost() {
        int x = 574;
        int y = 18;
        int dx = 30;
        int dy = 9;

        paintMessage("Killed: ", Color.White, x, y, true);
        paintMessage("Lost: ", Color.White, x, y + dy, true);
        paintMessage("-----------", Color.Grey, x, y + 2 * dy, true);
        paintMessage("Price: ", Color.White, x, y + 3 * dy, true);

        paintMessage(Atlantis.KILLED + "", Color.Green, x + dx, y, true);
        paintMessage(Atlantis.LOST + "", Color.Red, x + dx, y + dy, true);

        int balance = Atlantis.KILLED_RESOURCES - Atlantis.LOST_RESOURCES;
        Color color = balance >= 0 ? Color.Green : Color.Red;
        paintMessage((balance >= 0 ? "+" : "") + balance, color, x + dx, y + 3 * dy, true);
    }

    /**
     * Tooltips are units messages that appear over them and allow to report actions like "Repairing" or "Run
     * from enemy" etc.
     */
    private static void paintTooltipsOverUnits() {
        for (AUnit unit : Select.our().listUnits()) {
            if (unit.hasTooltip() && !unit.isGatheringMinerals() && !unit.isGatheringGas()) {
                String string = "";

                if (unit.hasTooltip()) {
                    string += unit.getTooltip();
                } else {
                    string += "---";
                }

//            string += "/";
//            
//            if (unit.getUnitAction() != null) {
//                string += unit.getUnitAction();
//            }
//            else {
//                string += "no_mission";
//            }
                paintTextCentered(unit.getPosition(), string, Color.White);
            }
        }
    }

    /**
     * Paints information about enemy units that are not visible, but as far as we know are alive.
     */
    private static void paintEnemyDiscovered() {
        for (UnitData enemyUnitData : AtlantisEnemyUnits.getDiscoveredAndAliveUnits()) {
            APosition topLeft;
//            if (enemyUnitData.getType().isBuilding()) {
            topLeft = enemyUnitData.getPosition().translateByPixels(
                    -enemyUnitData.getType().getDimensionLeft(),
                    -enemyUnitData.getType().getDimensionUp()
            );
            paintRectangle(topLeft, enemyUnitData.getType().getDimensionRight(),
                    enemyUnitData.getType().getDimensionDown(), Color.Grey);
            paintText(topLeft, enemyUnitData.getType().getShortName(), Color.White);
//            }
//            else {
//                paintCircle(enemyUnitData.getPosition(), 10, Color.Red);
//            }
//            paintTextCentered(enemyUnitData.getPosition().translate(0, 10), 
//                    enemyUnitData.getPosition().toString(), Color.White);
        }
    }

    /**
     * Every frame paint next allowed location of Supply Depot. Can be used to debug construction finding, but
     * slows the game down impossibly.
     */
    private static void paintTestSupplyDepotLocationsNearMain() {
        AUnit worker = Select.ourWorkers().first();
        AUnit base = Select.ourBases().first();
        int tileX = base.getPosition().getTileX();
        int tileY = base.getPosition().getTileY();
        for (int x = tileX - 10; x <= tileX + 10; x++) {
            for (int y = tileY - 10; y <= tileY + 10; y++) {
                APosition position = APosition.create(x, y);
                boolean canBuild = TerranPositionFinder.doesPositionFulfillAllConditions(
                        worker, AUnitType.Terran_Supply_Depot, position
                );

                paintCircleFilled(position, 4, canBuild ? Color.Green : Color.Red);

                if (x == tileX && y == tileY) {
                    paintCircleFilled(position, 10, canBuild ? Color.Green : Color.Red);
                }
            }
        }
    }

    /**
     * Can be helpful to illustrate or debug behavior or worker unit which is scouting around enemy base.
     */
    private static void paintEnemyRegionDetails() {
        APosition enemyBase = AtlantisEnemyUnits.getEnemyBase();
        if (enemyBase != null) {
            Region enemyBaseRegion = AtlantisMap.getRegion(enemyBase);
//            Position polygonCenter = enemyBaseRegion.getPolygon().getCenter();
//            APosition polygonCenter = APosition.create(enemyBaseRegion.getPolygon().getCenter());
            for (Position point : (ArrayList<APosition>) AScoutManager.scoutingAroundBasePoints.arrayList()) {
                paintCircleFilled(point, 2, Color.Yellow);
            }
        }
    }

    private static final int timeConsumptionLeftOffset = 572;
    private static final int timeConsumptionTopOffset = 65;
    private static final int timeConsumptionBarMaxWidth = 50;
    private static final int timeConsumptionBarHeight = 14;
    private static final int timeConsumptionYInterval = 16;
    
    /**
     * Paints bars showing CPU time usage by game aspect (like "Production", "Combat", "Workers", "Scouting").
     */
    private static void paintCodeProfiler() {
        int counter = 0;
        double maxValue = AtlantisUtilities.getMaxElement(
                CodeProfiler.getAspectsTimeConsumption().values()
        );

        for (String aspectTitle : CodeProfiler.getAspectsTimeConsumption().keySet()) {
            int x = timeConsumptionLeftOffset;
            int y = timeConsumptionTopOffset + timeConsumptionYInterval * counter++;

            int value = CodeProfiler.getAspectsTimeConsumption().get(aspectTitle).intValue();

            // Draw aspect time consumption bar
            int barWidth = (int) (timeConsumptionBarMaxWidth * value / maxValue);
            if (barWidth < 3) {
                barWidth = 3;
            }
            if (barWidth > timeConsumptionBarMaxWidth) {
                barWidth = timeConsumptionBarMaxWidth;
            }

            bwapi.drawBoxScreen(x, y, x + barWidth, y + timeConsumptionBarHeight, Color.Grey, true);
            bwapi.drawBoxScreen(x, y, x + timeConsumptionBarMaxWidth, y + timeConsumptionBarHeight, Color.Black);

            // Draw aspect label
            paintMessage(aspectTitle, Color.White, x + 4, y + 1, true);
        }
        
        // Paint total time
        int x = timeConsumptionLeftOffset;
        int y = timeConsumptionTopOffset + timeConsumptionYInterval * counter++ + 3;
        int frameLength = (int) CodeProfiler.getTotalFrameLength();
        paintMessage("Length: " + frameLength, Color.White, x + 4, y + 1, true);
    }

    // =========================================================
    // Lo-level
    public static void paintSideMessage(String text, Color color) {
        paintSideMessage(text, color, 0);
    }

    public static void paintSideMessage(String text, Color color, int yOffset) {
        if (color == null) {
            color = Color.White;
        }

        int screenX = 10;
        int screenY = 5 + 9 * (yOffset == 0 ? sideMessageTopCounter : sideMessageBottomCounter);
        paintMessage(text, color, screenX, yOffset + screenY, true);

        if (yOffset == 0) {
            sideMessageTopCounter++;
        } else {
            sideMessageBottomCounter++;
        }
    }

    public static void paintMessage(String text, Color color, int x, int y, boolean screenCoord) {
        if (screenCoord) {
            bwapi.drawTextScreen(new APosition(x, y), ColorUtil.getColorString(color) + text);
        } else {
            bwapi.drawTextMap(new APosition(x, y), ColorUtil.getColorString(color) + text);
        }
    }

    public static void paintRectangle(APosition position, int width, int height, Color color) {
        if (position == null) {
            return;
        }
        bwapi.drawBoxMap(position, PositionUtil.translateByPixels(position, width, height), color, false);
    }

    public static void paintRectangleFilled(APosition position, int width, int height, Color color) {
        if (position == null) {
            return;
        }
        bwapi.drawBoxMap(position, PositionUtil.translateByPixels(position, width, height), color, true);
    }

    public static void paintCircle(AUnit unit, int radius, Color color) {
        paintCircle(unit.getPosition(), radius, color);
    }

    public static void paintCircle(Position position, int radius, Color color) {
        if (position == null) {
            return;
        }
        bwapi.drawCircleMap(position, radius, color, false);
    }

    public static void paintCircleFilled(Position position, int radius, Color color) {
        if (position == null) {
            return;
        }
        bwapi.drawCircleMap(position, radius, color, true);
    }

    public static void paintLine(APosition start, int dx, int dy, Color color) {
        paintLine(start, PositionUtil.translateByPixels(start, dx, dy), color);
    }

    public static void paintLine(Position start, Position end, Color color) {
        if (start == null || end == null) {
            return;
        }
        bwapi.drawLineMap(start, end, color);
    }

    public static void paintLine(AUnit unit, Position end, Color color) {
        if (unit == null || end == null) {
            return;
        }
        bwapi.drawLineMap(unit.getPosition(), end, color);
    }

    // Causes Java runtime errors
//    public static void paintLine(Position start, Position end, Color color, int width) {
//        if (start == null || end == null) {
//            return;
//        }
//        for (int dx = 0; dx < width; dx++) {
//            for (int dy = 0; dy < width; dx++) {
//                bwapi.drawLineMap(new Position(start.getX() + dx, start.getY() + dy).makeValid(), 
//                        new Position(end.getX() + dx, end.getY() + dy).makeValid(), 
//                        color);
//            }
//        }
//    }
    public static void paintTextCentered(AUnit unit, String text, Color color) {
        paintTextCentered(unit.getPosition(), text, color, false);
    }

    public static void paintTextCentered(APosition position, String text, Color color) {
        paintTextCentered(position, text, color, false);
    }

    public static void paintTextCentered(AUnit unit, String text, boolean screenCords) {
        paintTextCentered(unit.getPosition(), text, null, screenCords);
    }

    public static void paintTextCentered(APosition position, String text, boolean screenCords) {
        paintTextCentered(position, text, null, screenCords);
    }

    public static void paintTextCentered(APosition position, String text, Color color, boolean screenCoords) {
        if (position == null || text == null) {
            return;
        }

        if (screenCoords) {
            bwapi.drawTextScreen(
                    PositionUtil.translateByPixels(position, (int) (-2.7 * text.length()), -2),
                    ColorUtil.getColorString(color) + text
            );
        } else {
            bwapi.drawTextMap(
                    PositionUtil.translateByPixels(position, (int) (-2.7 * text.length()), -2),
                    ColorUtil.getColorString(color) + text
            );
        }
    }

    public static void paintText(APosition position, String text, Color color) {
        if (position == null || text == null) {
            return;
        }

        bwapi.drawTextMap(position, ColorUtil.getColorString(color) + text);
    }

}
