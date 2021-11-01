package atlantis.debug;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.buildings.managers.AGasManager;
import atlantis.combat.eval.ACombatEvaluator;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.MissionAttack;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.ASquadCohesionManager;
import atlantis.combat.squad.Squad;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.constructing.ConstructionOrder;
import atlantis.production.constructing.ConstructionOrderStatus;
import atlantis.production.constructing.position.TerranPositionFinder;
import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.AChoke;
import atlantis.map.AMap;
import atlantis.map.ARegion;
import atlantis.position.APosition;
import atlantis.position.PositionHelper;
import atlantis.production.ProductionOrder;
import atlantis.production.orders.CurrentProductionOrders;
import atlantis.production.orders.ProductionQueue;
import atlantis.production.orders.ProductionQueueMode;
import atlantis.production.requests.AAntiLandBuildingRequests;
import atlantis.scout.AScoutManager;
import atlantis.strategy.EnemyStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.A;
import atlantis.util.CodeProfiler;
import atlantis.util.ColorUtil;
import atlantis.workers.AWorkerManager;
import atlantis.wrappers.ATech;
import atlantis.wrappers.MappingCounter;
import bwapi.Color;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AAdvancedPainter extends APainter {

    protected static int sideMessageTopCounter = 0;
    protected static int sideMessageMiddleCounter = 0;
    protected static int sideMessageBottomCounter = 0;
    protected static int prevTotalFindBuildPlace = 0;
    private static final int timeConsumptionLeftOffset = 572;
    private static final int timeConsumptionTopOffset = 65;
    private static final int timeConsumptionBarMaxWidth = 50;
    private static final int timeConsumptionBarHeight = 14;
    private static final int timeConsumptionYInterval = 16;

    // =========================================================

    /**
     * Executed once per frame, at the end of all other actions.
     */
    public static void paint() {
        if (paintingMode == MODE_NO_PAINTING) {
            return;
        }

        sideMessageTopCounter = 0;
        sideMessageBottomCounter = 0;

        // === PARTIAL PAINTING ====================================
//        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_PAINTING);
        setTextSizeMedium();

        paintSidebarInfo();
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

//        setTextSizeSmall();

        paintCodeProfiler();
//        paintMineralDistance();
        paintRegions();
        paintChokepoints();
        paintImportantPlaces();
        paintStrategicLocations();
//        paintTestSupplyDepotLocationsNearMain();
        paintConstructionProgress();
//        paintEnemyRegionDetails();
        paintSquads();
//        paintColoredCirclesAroundUnits();
        paintBuildingHealth();
        paintWorkersAssignedToBuildings();
        paintUnitsBeingTrainedInBuildings();
        paintBarsUnderUnits();
        paintFoggedUnitsThatIsEnemiesDiscovered();
        paintCombatUnits();
        paintEnemyCombatUnits();
        paintTooltipsOverUnits();

        setTextSizeMedium();
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_PAINTING);
    }

    // =========================================================

    /**
     * Painting for combat units can be a little different. Put here all the related code.
     */
    protected static void paintCombatUnits() {
        for (AUnit unit : Select.ourCombatUnits().listUnits()) {
            if (unit.isLoaded()) {
                continue;
            }

            APosition position = unit.position();

            if (unit.isRunning()) {
                paintRunningUnitWhiteFlag(unit);
            }

            // =========================================================
            // === Paint life bars bars over wounded units
            // =========================================================

            paintLifeBar(unit);

            // =========================================================
            // === Paint if enemy units is dangerously close
            // =========================================================

            paintCooldownAndRunBar(unit);

            // =========================================================
            // === Paint targets for combat units
            // =========================================================

            paintOurCombatUnitTargets(unit);

            // =========================================================
            // === Combat Evaluation Strength
            // =========================================================

            paintCombatEval(unit, false);

////            if (combatEval < 10) {
//            double eval = ACombatEvaluator.evaluateSituation(unit, true, false);
////                if (eval < 999) {
////                    String combatStrength = eval >= 10 ? (ColorUtil.getColorString(Color.Green) + ":)")
////                            : AtlantisCombatEvaluator.getEvalString(unit);
//            String combatStrength = ColorUtil.getColorString(Color.Green)
//                    + ACombatEvaluator.getEvalString(unit, eval);
//            paintTextCentered(new APosition(position.getX(), position.getY() - 15), combatStrength, null);
////                }

            // =========================================================
            // === Paint circle around units with zero ground weapon
            // === cooldown equal to 0 - meaning they can shoot now
            // =========================================================
//                if (unit.getGroundWeaponCooldown() == 0) {
//                    paintCircle(unitPosition, 14, Color.White);
//                }
//            }
            String order = (unit.u().getLastCommand() == null ? "NONE" : unit.getLastCommand().getType().toString())
                    + "(" + unit.getLastOrderFramesAgo() + ")";
            paintTextCentered(new APosition(position.getX(), position.getY() + 8), order, Color.Grey);
        }
    }

    private static void paintOurCombatUnitTargets(AUnit unit) {
//        if (unit.isAttacking()) {
//            paintLine(unit, unit.getTargetPosition(), (unit.isAttackingOrMovingToAttack() ? Color.Teal : Color.Grey));
            paintLine(unit, unit.getTarget(), (unit.isAttackingOrMovingToAttack() ? Color.Green : Color.Yellow));
//        }
//        if (!paintLine(unit, unit.getTarget(), (unit.isAttacking() ? Color.Green : Color.Yellow))) {
//            paintLine(unit, unit.getTargetPosition(), (unit.isAttacking() ? Color.Orange : Color.Yellow));
//        }
    }

    private static void paintEnemyTargets(AUnit enemy) {
        paintLine(enemy, enemy.getTarget(), Color.Red);
//        paintLine(enemy, enemy.getTargetPosition(), Color.Orange);
    }

    /**
     * Paint extra information about visible enemy combat units.
     */
    static void paintEnemyCombatUnits() {
        for (AUnit enemy : Select.enemy().combatUnits().listUnits()) {
            paintCombatEval(enemy, true);
            paintLifeBar(enemy);
            paintEnemyTargets(enemy);
            paintTextCentered(enemy, enemy.idWithHash(), Color.Grey, 0, 1);
        }

        setTextSizeMedium();
        for (AUnit enemy : Select.enemy().effCloaked().listUnits()) {
            paintCircleFilled(enemy, 18, Color.Orange);
            paintTextCentered(enemy, "Cloaked,HP=" + enemy.hp(), Color.Orange);
        }
        for (AUnit enemy : Select.enemy().cloakedButEffVisible().listUnits()) {
            paintCircleFilled(enemy, 18, Color.Green);
            paintTextCentered(enemy, "CloakedVisible,HP=" + enemy.hp(), Color.Green);
        }
    }

    /**
     * Paint focus point for global attack mission etc.
     */
    static void paintSidebarInfo() {
        Mission mission = Squad.getAlphaSquad().mission();

        // Time
        if (AGame.isUms()) {
            paintSideMessage("UMS map mode enabled", Color.Green);
        }
        paintSideMessage("Time: " + AGame.timeSeconds() + "s", Color.Grey);

        // =========================================================
        // Global mission

        paintSideMessage("Enemy strategy: " + (EnemyStrategy.isEnemyStrategyKnown()
                ? EnemyStrategy.get().toString() : "Unknown"),
                EnemyStrategy.isEnemyStrategyKnown() ? Color.Yellow : Color.Red);
        paintSideMessage("Mission: " + mission.getName(), Color.White);
        paintSideMessage("Focus: " + (mission.focusPoint() != null ? mission.focusPoint().toString() : "NONE"), Color.White);
        paintSideMessage("Enemy base: " + AEnemyUnits.enemyBase(), Color.White);

        // =========================================================
        // Focus point

        APosition focusPoint = MissionAttack.getInstance().focusPoint();
        AUnit mainBase = Select.mainBase();
        String desc = "";
        if (focusPoint != null && mainBase != null) {
            desc = "(" + ((int) mainBase.distTo(focusPoint)) + " tiles)";
        }
        paintSideMessage("Focus point: " + focusPoint + desc, Color.Blue, 0);

        // =========================================================
        paintSideMessage("Combat squad size: " + Squad.getAlphaSquad().size(), Color.Yellow, 0);

        // =========================================================
        // Gas workers
//        paintSideMessage("Find build. place: " + AtlantisPositionFinder.totalRequests,
//                prevTotalFindBuildPlace != AtlantisPositionFinder.totalRequests ? Color.Red : Color.Grey);
//        prevTotalFindBuildPlace = AtlantisPositionFinder.totalRequests;
        paintSideMessage("Gas workers: " + AGasManager.defineMinGasWorkersPerBuilding(), Color.Grey);
        paintSideMessage("Reserved minerals: " + ProductionQueue.mineralsReserved(), Color.Grey);
        paintSideMessage("Reserved gas: " + ProductionQueue.gasReserved(), Color.Grey);
    }

    private static void paintCombatEval(AUnit unit, boolean isEnemy) {
        APosition unitPosition = unit.position();
        double eval = (int) ACombatEvaluator.evaluateSituation(unit, true, isEnemy);
        String combatStrength = ColorUtil.getColorString(Color.Red)
                + ACombatEvaluator.getEvalString(unit, eval);
        paintTextCentered(new APosition(unitPosition.getX(), unitPosition.getY() - 15), combatStrength, null);
    }

    /**
     * Paints small progress bars over units that have cooldown.
     */
    static void paintBarsUnderUnits() {
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
//                        / (unit.type().getGroundWeapon().getDamageCooldown() + 1);
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
    static void paintImportantPlaces() {

        // === Handle UMS ==========================================
        if (AGame.isUms()) {
            return;
        }

        // =========================================================

//        APosition missionFocusPoint;
        APosition missionFocusPoint = Missions.globalMission().focusPoint();
//
        paintCircle(missionFocusPoint, 22, Color.Teal);
        paintCircle(missionFocusPoint, 20, Color.Teal);
        paintCircle(missionFocusPoint, 18, Color.Teal);
        paintTextCentered(missionFocusPoint, "FOCUS", Color.Teal);

//        // Main DEFEND focus point
//        APosition position = MissionAttack.getInstance().focusPoint();
//        if (position != null) {
//            position = MissionDefend.getInstance().focusPoint();
//            paintCircle(position, 20, Color.Orange);
//            paintCircle(position, 19, Color.Orange);
//            paintTextCentered(position, "DEFEND", Color.Orange);
//        }
//
//        missionFocusPoint = MissionAttack.getInstance().focusPoint();
//        if (missionFocusPoint != null) {
//            paintCircle(missionFocusPoint, 20, Color.Red);
//            //        paintCircle(position, 19, Color.Black);
//            paintTextCentered(missionFocusPoint, "ATTACK", Color.Red);
//        }
    }

    /**
     * Paints list of units we have in top left corner.
     */
    private static void paintUnitCounters() {
        // Unfinished
        MappingCounter<AUnitType> unitTypesCounter = new MappingCounter<>();
        for (AUnit unit : Select.ourUnfinishedRealUnits().listUnits()) {
//        for (AUnit unit : Select.our().listUnits()) {
            unitTypesCounter.incrementValueFor(unit.type());
        }

        Map<AUnitType, Integer> counters = unitTypesCounter.map();
        counters = A.sortByValue(counters, false);
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
            unitTypesCounter.incrementValueFor(unit.type());
        }

        counters = unitTypesCounter.map();
        counters = A.sortByValue(counters, false);
        for (AUnitType unitType : counters.keySet()) {
            if (!unitType.isBuilding()) {
                paintSideMessage(counters.get(unitType) + "x " + unitType, Color.Grey, 0);
            }
        }
        paintSideMessage("", Color.White, 0);
    }

    /**
     * Paints next units to build in top left corner.
     */
    static void paintProductionQueue() {
        paintSideMessage("", Color.White);
        paintSideMessage("Prod. queue:", Color.White);

        // === Display units currently in production ========================================

        paintCurrentlyInProduction();

        // === Display units that should be produced right now or any time ==================

        ArrayList<ProductionOrder> produceNow = CurrentProductionOrders.thingsToProduce(ProductionQueueMode.ENTIRE_QUEUE);
//        ArrayList<ProductionOrder> produceNow = CurrentProductionOrders.thingsToProduce(ProductionQueueMode.ONLY_WHAT_CAN_AFFORD);
        int counter = 1;
        for (ProductionOrder order : produceNow) {
            paintSideMessage(
                    order.shortName(),
                    order.canHasWhatRequired() ? (order.canAffordNow() ? Color.Green : Color.Yellow) : Color.Red
            );
            if (++counter >= 10) {
                break;
            }
        }

        // === Display next units to produce ================================================

//        ArrayList<ProductionOrder> fullQueue = ProductionQueue.nextInProductionQueue(
//                5 - produceNow.size());
//        for (int index = produceNow.size(); index < fullQueue.size(); index++) {
//            ProductionOrder order = fullQueue.get(index);
//            if (order != null && order.shortName() != null) {
//                if (order.getUnitOrBuilding() != null
//                        && !AGame.hasBuildingsToProduce(order.getUnitOrBuilding(), true)) {
//                    continue;
//                }
//                paintSideMessage(order.shortName(), Color.Red);
//            }
//        }

        // === Paint info if queues are empty ===============================================

        if (produceNow.isEmpty()) {
            paintSideMessage("Nothing to produce - it's a bug", Color.Red);
        }
    }

    private static void paintCurrentlyInProduction() {
        // Units & buildings
        for (AUnit unit : Select.ourUnfinished().list()) {
            AUnitType type = unit.type();
            if (type.equals(AUnitType.Zerg_Egg)) {
                type = unit.getBuildType();
            }
            paintSideMessage(type.shortName(), Color.Green);
        }

        // Techs
        for (TechType techType : ATech.getCurrentlyResearching()) {
            paintSideMessage(techType.toString(), Color.Green);
        }

        // Upgrades
        for (UpgradeType upgradeType : ATech.getCurrentlyUpgrading()) {
            paintSideMessage(upgradeType.toString(), Color.Green);
        }

        // Constructions already planned
        for (ConstructionOrder order : AConstructionRequests.getNotStartedConstructions()) {
            AUnitType type = order.getBuildingType();
            paintSideMessage(type.shortName(), Color.Cyan);
        }
    }

    /**
     * Paints all pending contstructions, including those not yet started, even if only in the AI memory.
     */
    static void paintSidebarConstructionsPending() {
        int yOffset = 220;
        ArrayList<ConstructionOrder> allOrders = AConstructionRequests.getAllConstructionOrders();
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
                paintSideMessage(constructionOrder.getBuildingType().shortName()
                        + ", " + status + ", " + builder, color, yOffset);
            }
        }
    }

    /**
     * Paints places where buildings that do not yet exist are planned to be placed.
     */
    static void paintConstructionPlaces() {
        Color color = Color.Grey;
        for (ConstructionOrder order : AConstructionRequests.getAllConstructionOrders()) {
            if (order.getStatus() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
//            if (order.getStatus() != ConstructionOrderStatus.CONSTRUCTION_FINISHED) {
                APosition positionToBuild = order.getPositionToBuild();
                AUnitType buildingType = order.getBuildingType();
                if (positionToBuild == null || buildingType == null) {
                    continue;
                }

                paintConstructionPlace(positionToBuild, buildingType, null, color);
            }
        }
    }

    private static void paintConstructionPlace(APosition positionToBuild, AUnitType buildingType, String text, Color color) {
        if (text == null) {
            text = buildingType.shortName();
        }

        // Paint box
        paintRectangle(positionToBuild, buildingType.getTileWidth() * 32, buildingType.getTileHeight() * 32, color);

        // Draw X
        paintLine(
                PositionHelper.translateByPixels(positionToBuild, buildingType.getTileWidth() * 32, 0),
                PositionHelper.translateByPixels(positionToBuild, 0, buildingType.getTileHeight() * 32),
                color
        );
        paintLine(positionToBuild,
                buildingType.getTileWidth() * 32,
                buildingType.getTileHeight() * 32,
                color
        );

        // Draw text
        paintTextCentered(
                positionToBuild.translateByPixels(buildingType.getDimensionLeft(), 69), text, color
        );
    }

    /**
     * Paints circles around units which mean what's their mission.
     */
    private static void paintColoredCirclesAroundUnits() {
        for (AUnit unit : Select.ourRealUnits().listUnits()) {
            if (unit.isWorker() && (unit.isGatheringMinerals() || unit.isGatheringGas()) || unit.isLoaded()) {
                continue;
            }

            APosition unitPosition = unit.position();
            APosition targetPosition = unit.getTargetPosition();
            int unitRadius = unit.type().getDimensionLeft();

            // STARTING ATTACK
            if (unit.isStartingAttack()) {
                paintCircle(unit, unitRadius - 7, Color.Orange);
                paintCircle(unit, unitRadius - 6, Color.Orange);
                paintCircle(unit, unitRadius - 5, Color.Orange);
                paintCircle(unit, unitRadius - 4, Color.Orange);
                paintCircle(unit, unitRadius - 3, Color.Orange);
            }
            // ATTACK FRAME
            if (unit.isAttackFrame()) {
                paintRectangleFilled(unit.position().translateByPixels(-5, -10), 10, 20, Color.Red);
//                paintCircle(unit, 2, Color.Red);
//                paintCircle(unit, 4, Color.Red);
//                paintCircle(unit, 5, Color.Red);
//                paintCircle(unit, 8, Color.Red);
//                paintCircle(unit, 9, Color.Red);
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
            if (unit.isAttackingOrMovingToAttack()) {
                paintCircle(unit, unitRadius - 3, Color.Yellow);
                paintCircle(unit, unitRadius - 2, Color.Yellow);
            }
            // MOVE
            if (unit.isMoving()) {
                paintCircle(unit, unitRadius - 4, Color.Blue);
                paintCircle(unit, unitRadius - 3, Color.Blue);
                paintCircle(unit, unitRadius - 2, Color.Blue);
                if (unit.getTargetPosition()!= null) {
                    paintCircleFilled(unit.getTargetPosition(), 4, Color.Blue);
                    paintLine(unit.position(), unit.getTargetPosition(), Color.Blue);
                }
            }
//            // CONSTRUCTING
//            if (unit.isConstructing()) {
//                paintCircle(unit, 6, Color.Teal);
//                paintCircle(unit, 5, Color.Teal);
//            }

            // RUN
            if (unit.isRunning()) {
                paintLine(unit.position(), unit.runningManager().getRunToPosition(), Color.Yellow);
                paintLine(unit.position().translateByPixels(1, 1), unit.runningManager().getRunToPosition(), Color.Yellow);

                if (unit.runningManager().getRunToPosition() != null) {
                    paintCircleFilled(unit.runningManager().getRunToPosition(), 10, Color.Yellow);
                }

                paintRunningUnitWhiteFlag(unit);
            }

            // Paint #ID
            paintTextCentered(unit.position().translateByTiles(0, 1),
                    "#" + unit.getID() + " " + unit.getUnitAction(), Color.Cyan);

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
//                paintCircle(unit, unit.type().getDimensionLeft() + unit.type().getDimensionRight(), color);
//                paintCircle(unit, unit.type().getDimensionLeft() - 2 + unit.type().getDimensionRight(), color);
//            }
            if (unit.isWorker() && unit.isIdle()) {
                paintCircle(unit, 10, Color.Black);
                paintCircle(unit, 8, Color.Black);
                paintCircle(unit, 6, Color.Black);
                paintCircle(unit, 4, Color.Black);
            }
        }
    }

    private static void paintRunningUnitWhiteFlag(AUnit unit) {
        int flagWidth = 15;
        int flagHeight = 8;
        int dy = 12;

        paintLine(unit, unit.getTargetPosition(), Color.Blue); // Where unit is running to

        paintRectangleFilled(unit.position().translateByPixels(0, -flagHeight - dy),
                flagWidth, flagHeight, Color.White); // White flag
        paintRectangle(unit.position().translateByPixels(0, -flagHeight - dy),
                flagWidth, flagHeight, Color.Grey); // Flag border
        paintRectangleFilled(unit.position().translateByPixels(-1, flagHeight - dy),
                2, flagHeight, Color.Grey); // Flag stick
    }

    /**
     * Paints progress bar with percent of completion over all buildings under construction.
     */
    static void paintConstructionProgress() {
        setTextSizeMedium();
        for (AUnit unit : Select.ourBuildingsIncludingUnfinished().listUnits()) {
            if (unit.isCompleted()) {
                continue;
            }

            String stringToDisplay;

            int labelMaxWidth = 60;
            int labelHeight = 14;
            int labelLeft = unit.position().getX() - labelMaxWidth / 2;
            int labelTop = unit.position().getY() + 8;

            double progress = (double) unit.hp() / unit.maxHp();
            int labelProgress = (int) (1 + 99 * progress);

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


            // =========================================================
            // Paint progress text

            Color progressColor;
            if (labelProgress < 26) {
                progressColor = Color.Red;
            }
            else if (labelProgress < 67) {
                progressColor = Color.Yellow;
            }
            else {
                progressColor = Color.Green;
            }
            stringToDisplay = labelProgress + "%";

            paintTextCentered(
                    new APosition(labelLeft + labelMaxWidth * 50 / 100 + 2, labelTop + 2),
                    stringToDisplay, progressColor
            );

            // =========================================================

            // Display name of unit
            String name = unit.getBuildType().shortName();

            // Paint building name
            paintTextCentered(new APosition(unit.position().getX(), unit.position().getY() - 7),
                    name, Color.White);
        }

        setTextSizeSmall();
    }

    /**
     * For buildings not 100% healthy, paints its hit points using progress bar.
     */
    static void paintBuildingHealth() {
        for (AUnit unit : Select.ourBuildings().listUnits()) {
            if (unit.isBunker() || unit.hp() >= unit.maxHp()) { //isWounded()
                continue;
            }
            int labelMaxWidth = 56;
            int labelHeight = 4;
            int labelLeft = unit.position().getX() - labelMaxWidth / 2;
            int labelTop = unit.position().getY() + 13;

            double hpRatio = (double) unit.hp() / unit.maxHp();
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
    static void paintWorkersAssignedToBuildings() {
        setTextSizeLarge();
        for (AUnit building : Select.ourBuildings().listUnits()) {
            if (!building.isBase() && !building.type().isGasBuilding()) {
                continue;
            }

            // Paint text
            int workers = AWorkerManager.getHowManyWorkersWorkingNear(building, false);
            if (workers > 0) {
                String workersAssigned = workers + "";
                paintTextCentered(
                        PositionHelper.translateByPixels(building.position(), -5, -36),
                        workersAssigned, Color.Grey
                );
            }
        }
        setTextSizeSmall();
    }

    /**
     * If buildings are training units, it paints what unit is trained and the progress.
     */
    static void paintUnitsBeingTrainedInBuildings() {
        setTextSizeMedium();
        for (AUnit building : Select.ourBuildingsIncludingUnfinished().listUnits()) {
            if (!building.isBuilding() || !building.isTrainingAnyUnit()) {
                continue;
            }

            int labelMaxWidth = 90;
            int labelHeight = 14;
            int labelLeft = building.position().getX() - labelMaxWidth / 2;
            int labelTop = building.position().getY();

            int operationProgress = 1;
            AUnitType unit = building.getTrainingQueue().get(0);
            String trainedUnitString = "";
            if (unit != null) {
                operationProgress = 100 * (unit.getTotalTrainTime() - building.getRemainingTrainTime()) / unit.getTotalTrainTime();
                trainedUnitString = unit.shortName();
            }

            // Paint box
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop),
                    new APosition(labelLeft + labelMaxWidth * operationProgress / 100, labelTop + labelHeight),
                    Color.Grey,
                    true
            );

            // Paint box borders
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop),
                    new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight),
                    Color.Black,
                    false
            );

            // =========================================================
            // Display label
            paintTextCentered(
                    new APosition(labelLeft + labelMaxWidth / 2, labelTop + 2),
                    trainedUnitString, Color.White
            );
        }
        setTextSizeSmall();
    }

    /**
     * Paints number of units killed and lost in the top right corner.
     */
    static void paintKilledAndLost() {
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

        int balance = AGame.killsLossesResourceBalance();
        Color color = balance >= 0 ? Color.Green : Color.Red;
        paintMessage((balance >= 0 ? "+" : "") + balance, color, x + dx, y + 3 * dy, true);
    }

    /**
     * Tooltips are units messages that appear over them and allow to report actions like "Repairing" or "Run
     * from enemy" etc.
     */
    static void paintTooltipsOverUnits() {
        for (AUnit unit : Select.our().listUnits()) {
            if (unit.isLoaded()) {
                continue;
            }

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
                paintTextCentered(unit.position(), string, Color.White);
            }
        }
    }

    /**
     * Paints information about enemy units that are not visible, but as far as we know are alive.
     */
    static void paintFoggedUnitsThatIsEnemiesDiscovered() {
        for (AFoggedUnit foggedEnemy : AEnemyUnits.getEnemyDiscoveredAndAliveUnits()) {
            if (!foggedEnemy.hasKnownPosition()) {
                continue;
            }

            APosition topLeft;
            topLeft = foggedEnemy.position().translateByPixels(
                    -foggedEnemy.type().getDimensionLeft(),
                    -foggedEnemy.type().getDimensionUp()
            );
//            paintRectangle(
//                    topLeft,
//                    foggedEnemy.type().getDimensionRight() / 32,
//                    foggedEnemy.type().getDimensionDown() / 32,
//                    Color.Grey
//            );
            paintRectangle(
                    foggedEnemy.position(),
                    foggedEnemy.type().getDimensionRight() / 32,
                    foggedEnemy.type().getDimensionDown() / 32,
                    Color.Grey
            );
            paintText(topLeft, foggedEnemy.type().shortName() + " (" + foggedEnemy.lastPositionUpdatedAgo() + ")", Color.White);
        }
    }

    /**
     * Every frame paint next allowed location of Supply Depot. Can be used to debug construction finding, but
     * slows the game down impossibly.
     */
    private static void paintTestSupplyDepotLocationsNearMain() {
        AUnit worker = Select.ourWorkers().first();
        AUnit base = Select.ourBases().first();
        int tileX = base.position().getTileX();
        int tileY = base.position().getTileY();
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
        APosition enemyBase = AEnemyUnits.enemyBase();
        if (enemyBase != null) {
            ARegion enemyBaseRegion = AMap.getRegion(enemyBase);
//            Position polygonCenter = enemyBaseRegion.getPolygon().getCenter();
//            APosition polygonCenter = APosition.create(enemyBaseRegion.getPolygon().getCenter());
            for (APosition point : (ArrayList<APosition>) AScoutManager.scoutingAroundBasePoints.arrayList()) {
                paintCircleFilled(point, 2, Color.Yellow);
            }
        }
    }

    /**
     * Paints bars showing CPU time usage by game aspect (like "Production", "Combat", "Workers", "Scouting").
     */
    static void paintCodeProfiler() {
        int counter = 0;
        double maxValue = A.getMaxElement(
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

    private static void paintCooldownAndRunBar(AUnit unit) {
        boolean shouldAvoidAnyUnit = AAvoidUnits.shouldAvoidAnyUnit(unit);

//        paintUnitProgressBar(unit, 27, 100, Color.Grey);
        paintUnitProgressBar(unit, 22, unit.cooldownPercent(), shouldAvoidAnyUnit ? Color.Red : Color.Teal);
    }

    private static void paintLifeBar(AUnit unit) {
//        if (unit.isWounded()) {
        paintUnitProgressBar(unit, 17, 100, Color.Red);
        paintUnitProgressBar(unit, 17, unit.hpPercent(), unit.isOur() ? Color.Green : Color.Yellow);
//        }
    }

    private static void paintUnitProgressBar(AUnit unit, int dpy, int progressPercent, Color barColor) {
        int barWidth = 20;
        int barHeight = 4;
        APosition topLeft = new APosition(unit.x() - barWidth / 2, unit.y() + dpy);

        // Progress bar
        paintRectangleFilled(topLeft, (int) A.inRange(1, barWidth * progressPercent / 100, 100), barHeight, barColor);

        // Bar borders
        paintRectangle(topLeft, barWidth, barHeight, Color.Black);
    }

    private static void paintBar(APosition topLeft, int width, int height, Color barColor) {

        // Progress bar
        paintRectangleFilled(topLeft, width, height, barColor);

        // Bar borders
        paintRectangle(topLeft, width, height, Color.Black);
    }

    protected static void paintRegions() {
        List<ARegion> regions = AMap.getRegions();
        for (ARegion region : regions) {
            APainter.paintRectangle(
                    region.getCenter().translateByTiles(-3, -3),
                    6,
                    6,
                    Color.Brown
            );
            APainter.paintTextCentered(
                    region.getCenter(),
                    region.toString(),
                    Color.Brown
            );
        }
    }

    protected static void paintChokepoints() {

        // All chokes
        List<AChoke> chokePoints = AMap.getChokes();
        for (AChoke choke : chokePoints) {
            paintChoke(choke, Color.Brown, "");
        }
    }

    protected static void paintStrategicLocations() {
        if (AGame.isUms()) {
            return;
        }

        APainter.setTextSizeMedium();

        // Natural base
        APosition natural = AMap.naturalBase();
        paintBase(natural, "Our natural", Color.Grey);

        // Enemy base
        APosition enemyBase = AMap.getEnemyNatural();
        paintBase(enemyBase, "Enemy natural", Color.Orange);

        // Our main choke
        AChoke mainChoke = AMap.mainBaseChoke();
        paintChoke(mainChoke, Color.Green, "Main choke");

        // Our natural choke
        AChoke naturalChoke = AMap.getChokeForNaturalBase(AMap.naturalBase());
        paintChoke(naturalChoke, Color.Green, "Natural choke");

        // Enemy natural choke
        AChoke enemyNaturalChoke = AMap.enemyNaturalChoke();
        paintChoke(enemyNaturalChoke, Color.Orange, "Enemy natural choke");

        // Next defensive building position
        AUnitType building = AAntiLandBuildingRequests.building();
//        APosition defBuild = AAntiLandBuildingRequests.positionForNextBuilding();
        paintConstructionPlace(AAntiLandBuildingRequests.positionForNextBuilding(), building, building.shortName(), Color.Brown);
    }

    private static void paintMineralDistance() {
        AUnit mainBase = Select.mainBase();
        if (mainBase == null) {
            return;
        }

        for (AUnit mineral : Select.minerals().inRadius(8, mainBase).list()) {
            String dist = A.digit(mineral.distTo(mainBase));
            int assigned = AWorkerManager.countWorkersAssignedTo(mineral);
            setTextSizeLarge();
            paintTextCentered(mineral, dist + " (" + assigned + ")", Color.White);
        }

        if (A.now() <= 100) {
            for (AUnit worker : Select.ourWorkers().list()) {
                if (worker.getTarget() != null) {
                    paintLine(worker, worker.getTarget(), Color.Grey);
                }
            }
        }
    }

    private static void paintSquads() {
        Squad alphaSquad = Squad.getAlphaSquad();
        if (alphaSquad == null) {
            return;
        }

        APosition median = alphaSquad.center();
        if (median != null) {
            int maxDist = (int) (ASquadCohesionManager.preferredDistToSquadCenter(alphaSquad.size()) * 32);

            APainter.paintCircle(median, maxDist + 1, Color.Cyan);
            APainter.paintCircle(median, maxDist, Color.Cyan);

//            APainter.setTextSizeMedium();
//            APainter.paintTextCentered(median, "Median (" + maxDist + ")", Color.Cyan, 0, 0.5);
        }
    }

}
