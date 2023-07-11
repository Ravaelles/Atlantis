package atlantis.debug.painter;

import atlantis.Atlantis;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.terran.TerranBunker;
import atlantis.combat.micro.terran.TerranMissileTurretsForMain;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.retreating.ShouldRetreat;
import atlantis.combat.squad.AllSquads;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.GameLog;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.tech.ATech;
import atlantis.map.*;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.scout.ScoutCommander;
import atlantis.map.scout.ScoutManager;
import atlantis.production.ProductionOrder;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionOrderStatus;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.TerranPositionFinder;
import atlantis.production.orders.production.CurrentProductionQueue;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.production.orders.production.ProductionQueueMode;
import atlantis.production.requests.zerg.ZergSunkenColony;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.buildings.GasBuildingsCommander;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.workers.WorkerRepository;
import atlantis.util.CodeProfiler;
import atlantis.util.ColorUtil;
import atlantis.util.MappingCounter;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;
import atlantis.util.log.LogMessage;
import bwapi.Color;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static bwapi.Color.Green;
import static bwapi.Color.Grey;

public class AAdvancedPainter extends APainter {

    protected static int sideMessageTopCounter = 0;
    protected static int sideMessageMiddleCounter = 0;
    protected static int sideMessageBottomCounter = 0;
    protected static int prevTotalFindBuildPlace = 0;
    private static final int rightSideMessageLeftOffset = 572;
    private static final int rightSideMessageTopOffset = 450;
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

        if (A.now() < 1) {
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
        paintGameLog();
        paintConstructionPlaces();
        //        paintUnitCounters();

        if (paintingMode == MODE_PARTIAL_PAINTING) {
            CodeProfiler.endMeasuring(CodeProfiler.ASPECT_PAINTING);
            return;
        }

        // =========================================================

//        setTextSizeSmall();

//        paintUnitTypes();
        paintRegions();
//        paintMineralDistance();
        paintChokepoints();
        paintImportantPlaces();
        paintBases();
        paintStrategicLocations();
//        paintTestSupplyDepotLocationsNearMain();
        paintConstructionProgress();
//        paintEnemyRegionDetails();
//        paintColoredCirclesAroundUnits();
        paintBuildingHealth();
        paintWorkersAssignedToBuildings();
        paintBuildingsTrainingUnitsAndResearching();
        paintBarsUnderUnits();
        paintFoggedUnits();
        paintCombatUnits();
        paintEnemyCombatUnits();
        paintTooltipsOverUnits();
        paintCodeProfiler();
        paintSquads();

        setTextSizeMedium();
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_PAINTING);
    }

    // =========================================================

    private static void paintUnitTypes() {
        for (AUnit unit : Select.all().list()) {
            paintTextCentered(unit, unit.type().toString(), Color.White, 0, -0.2);

            if (unit.buildType() != null) {
                paintTextCentered(unit, "Build: " + unit.buildType().toString(), Green, 0,0.1);
            }
        }
    }

    /**
     * Painting for combat units can be a little different. Put here all the related code.
     */
    protected static void paintCombatUnits() {
        for (AUnit unit : Select.ourCombatUnits().list()) {
//            paintUnitInRangeInfo(unit);

            // =========================================================
            // === Paint targets for combat units
            // =========================================================

            paintTargets(unit);

            if (unit.isLoaded()) {
                continue;
            }

            // =========================================================
            // === Paint running and white flag
            // =========================================================

            if (unit.isRunning()) {
                paintWhiteFlagWhenRunning(unit);
            }

            // =========================================================
            // === Paint life bars bars over wounded units
            // =========================================================

            paintLifeBar(unit);

            // =========================================================
            // === Paint if enemy units is dangerously close
            // =========================================================

//            paintCooldown(unit);

            if (unit.isStimmed()) {
                paintCircle(unit, 13, Color.Purple);
                paintCircle(unit, 11, Color.Purple);
            }

            // =========================================================
            // === Combat Evaluation Strength
            // =========================================================

            paintCombatEval(unit);

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
            // === Other stuff =========================================

            paintSquad(unit);
            paintLastAction(unit);
            paintLog(unit);
        }
    }

    private static void paintUnitInRangeInfo(AUnit unit) {
        APosition position = unit.position();

        Color inRangeColor = unit.nearestEnemyDist() <= 4
            ? (unit.cooldownRemaining() == 0 ? Color.Green : Color.Yellow)
            : Color.Red;

        paintRectangleFilled(
            position.translateByTiles(0.5, 0),
            32, 5, inRangeColor
        );
        paintTextCentered(
            position.translateByTiles(0.5, 0),
            A.dist(unit.nearestEnemyDist()) + "  " + unit.cooldownRemaining(),
            inRangeColor, 1, -0.4
        );
    }

    private static void paintLog(AUnit unit) {
        int baseOffset = 13;
        int counter = 0;
        for (int i = unit.log().messages().size() - 1; i >= 0; i--) {
            LogMessage message = unit.log().messages().get(i);
//            unit.paintInfo(message.createdAtFrames() + "-" + message.message(), Color.Grey, offset);
            paintTextCentered(
                    unit,
                    message.createdAtFrames() + "-" + message.message(),
//                    Color.Grey,
                    Color.Yellow,
                    0,
                    (baseOffset + (8 * (counter++))) / 32.0
            );
        }
    }

    private static void paintLastAction(AUnit unit) {
        if (!unit.isRealUnit() || unit.isBuilding()) {
            return;
        }

//        String action = unit.action().toString() + "(" + unit.lastActionFramesAgo() + ")";
        String action = unit.action() != null ? unit.action().toString() : "NO_ACTION";
        if ("ATTACK_UNIT".equals(action)) {
            action += ":" + (unit.target() != null ? unit.target().type() : ("NO_TARGET(" + unit.lastActionFramesAgo() + ")"));
        }
        action += "(" + unit.lastActionFramesAgo() + ")";

        paintTextCentered(new APosition(unit.x(), unit.y() - 6), action, Color.Grey);
    }

    private static void paintSquad(AUnit unit) {
        if (!unit.isAlive() || unit.squad() == null) {
            return;
        }
//        String extra = " " + unit.idWithHash();
        String extra = " " + A.dist(unit.distToLeader());
        String squadLetter = unit.squad().letter() + extra;
        paintTextCentered(unit.translateByPixels(10, -16), squadLetter, Color.Purple);
    }

    private static void paintTargets(AUnit unit) {
        if (unit.hasTargetPosition() && !unit.targetPositionAtLeastAway(12)) {
            paintLine(unit, unit.targetPosition(), Color.Grey);
//            paintLine(unit, unit.targetPosition(), (unit.isAttackingOrMovingToAttack() ? Color.Teal : Color.Grey));
//            paintLine(unit, unit.target(), (unit.isAttackingOrMovingToAttack() ? Color.Green : Color.Yellow));
        }
//        if (!paintLine(unit, unit.getTarget(), (unit.isAttacking() ? Color.Green : Color.Yellow))) {
//            paintLine(unit, unit.getTargetPosition(), (unit.isAttacking() ? Color.Orange : Color.Yellow));
//        }
    }

    private static void paintEnemyTargets(AUnit enemy) {
        paintLine(enemy, enemy.target(), Color.Red);
    }

    /**
     * Paint extra information about visible enemy combat units.
     */
    static void paintEnemyCombatUnits() {
        for (AUnit enemy : Select.enemy().combatUnits().list()) {
            if (!enemy.isAlive()) {
                continue;
            }

            paintCombatEval(enemy);
            paintLifeBar(enemy);
//            paintEnemyTargets(enemy);
            paintTextCentered(enemy, enemy.idWithHash(), Color.Grey, 0, 1);
        }

        setTextSizeMedium();
        for (AUnit enemy : Select.enemy().effUndetected().list()) {
            paintCircle(enemy, 16, Color.Orange);
            paintCircle(enemy, 15, Color.Orange);
            paintTextCentered(enemy, "Cloaked," + enemy.name() + ",HP=" + enemy.hp(), Color.Red);
        }
        for (AUnit enemy : Select.enemy().effVisible().list()) {
            if (enemy.isCloaked() || enemy.isBurrowed()) {
                paintCircle(enemy, 16, Green);
                paintCircle(enemy, 15, Green);
                paintCircle(enemy, 15, Green);
                paintTextCentered(enemy, "CloakedVisible,HP=" + enemy.hp(), Color.White);
            }
        }
    }

    /**
     * Paint focus point for global attack mission etc.
     */
    static void paintSidebarInfo() {
        Color color;

        // Time
        if (AGame.isUms()) {
            paintSideMessage("UMS MAP MODE DETECTED", Green);
            paintSideMessage("---------------------", Grey);
        }
        paintSideMessage("Time: " + AGame.timeSeconds() + "s (" + A.now() + ")", Color.Grey);

        // =========================================================
        // Global mission

        int armyStrength = ArmyStrength.ourArmyRelativeStrength();
        paintSideMessage("Army strength: " + armyStrength + "%", armyStrength >= 100 ? Green : Color.Red);
        paintSideMessage("Enemy strategy: " + (EnemyStrategy.isEnemyStrategyKnown()
                ? EnemyStrategy.get().toString() : "Unknown"),
                EnemyStrategy.isEnemyStrategyKnown() ? Color.Yellow : Color.Red);

        Mission mission = Missions.globalMission();
        if (mission.isMissionDefend()) {
            color = Color.White;
        } else if (mission.isMissionContain()) {
            color = Color.Teal;
        } else {
            color = Color.Orange;
        }
        paintSideMessage("Global mission: " + mission.name() + " (" + Missions.counter() + ")", color);

        mission = Alpha.get().mission();
        if (mission.isMissionDefend()) {
            color = Color.White;
        } else if (mission.isMissionContain()) {
            color = Color.Teal;
        } else {
            color = Color.Orange;
        }
        paintSideMessage("Alpha mission: " + Alpha.get().mission().name(), color);

//        AFocusPoint focus = mission.focusPoint();
        paintSideMessage("Enemy base: " + EnemyUnits.enemyBase(), Color.Grey);
        paintSideMessage("Fogged buildings: " + EnemyUnits.foggedUnits().buildings().count(), Color.Grey);
        paintSideMessage("Fogged units: " + EnemyUnits.foggedUnits().realUnits().count(), Color.Grey);

        // =========================================================
        // Focus point

//        AFocusPoint focusPoint = MissionAttack.getInstance().focusPoint();
        AFocusPoint focusPoint = Alpha.get().mission().focusPoint();
        AUnit mainBase = Select.main();
        String desc = "";
        String focusPointString = focusPoint != null
            ? (focusPoint.getName() != null ? focusPoint.getName() : focusPoint.toString())
            : "NONE";
        if (focusPoint != null && mainBase != null) {
            desc = "(" + ((int) mainBase.distTo(focusPoint)) + " tiles)";
        }
        paintSideMessage("Focus point: " + focusPointString + desc, Color.Blue, 0);

        // =========================================================

        paintSideMessage("Combat squad size: " + Alpha.get().size(), Color.Yellow, 0);

        int scouts = ScoutCommander.allScouts().size();
        color = scouts == 0 ? Color.Grey : (scouts == 1 ? Color.Yellow : Color.Red);
        paintSideMessage("Scouts: " + scouts, color, 0);

        if (We.terran()) {
            paintSideMessage("Repairers: " + RepairAssignments.countTotalRepairers(), Color.White, 0);
            paintSideMessage("Protectors: " + RepairAssignments.countTotalProtectors(), Color.White, 0);
        }

        // =========================================================
        // Gas workers
//        paintSideMessage("Find build. place: " + AtlantisPositionFinder.totalRequests,
//                prevTotalFindBuildPlace != AtlantisPositionFinder.totalRequests ? Color.Red : Color.Grey);
//        prevTotalFindBuildPlace = AtlantisPositionFinder.totalRequests;
        paintSideMessage("Workers: " + Count.workers(), Color.White);
        paintSideMessage("Gas workers: " + GasBuildingsCommander.minGasWorkersPerBuilding(), Color.Grey);
        paintSideMessage("Reserved minerals: " + ProductionQueue.mineralsReserved(), Color.Grey);
        paintSideMessage("Reserved gas: " + ProductionQueue.gasReserved(), Color.Grey);
    }

    private static void paintCombatEval(AUnit unit) {
        APosition unitPosition = unit.position();
        double combatEval = unit.combatEvalRelative();
//        double combatEval = unit.combatEvalAbsolute();
        String combatStrength = ColorUtil.getColorString(Color.Red) +
                (combatEval >= 9876 ? "+" : A.digit(combatEval > 2 ? (int) combatEval : combatEval));
//                (combatEval >= 0 ? "+" : A.digit(combatEval > 2 ? (int) combatEval : combatEval));
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
        for (AUnit unit : Select.ourUnfinishedRealUnits().list()) {
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
        for (AUnit unit : Select.our().list()) {
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

//        paintCurrentlyInProduction();
//        paintNotStartedConstructions();

        // === Display units that should be produced right now or any time ==================

        ArrayList<ProductionOrder> produceNow = CurrentProductionQueue.thingsToProduce(ProductionQueueMode.ENTIRE_QUEUE);
//        ArrayList<ProductionOrder> produceNow = CurrentProductionQueue.thingsToProduce(ProductionQueueMode.ONLY_WHAT_CAN_AFFORD);
        int counter = 1;
        for (ProductionOrder order : produceNow) {
            paintSideMessage(
                    String.format("%02d", order.minSupply()) + " - " + order.name(),
                    order.hasWhatRequired() ? (order.currentlyInProduction() ? Green : Color.Yellow) : Color.Red
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
//            if (order != null && order.name() != null) {
//                if (order.getUnitOrBuilding() != null
//                        && !AGame.hasBuildingsToProduce(order.getUnitOrBuilding(), true)) {
//                    continue;
//                }
//                paintSideMessage(order.name(), Color.Red);
//            }
//        }

        // === Paint info if queues are empty ===============================================

        if (produceNow.isEmpty()) {
            paintSideMessage("Nothing to produce - it's a bug", Color.Red);
        }
    }

    private static void paintNotStartedConstructions() {

        // Constructions already planned
        for (Construction order : ConstructionRequests.notStarted()) {
            AUnitType type = order.buildingType();
            paintSideMessage(type.name(), Color.Cyan);
        }
    }

    private static void paintCurrentlyInProduction() {
        // Units & buildings
        for (AUnit unit : Select.ourUnfinished().list()) {
            AUnitType type = unit.type();
            if (type.equals(AUnitType.Zerg_Egg)) {
                type = unit.buildType();
            }
            paintSideMessage(type.name(), Green);
        }

        // Techs
        for (TechType techType : ATech.getCurrentlyResearching()) {
            paintSideMessage(techType.toString(), Green);
        }

        // Upgrades
        for (UpgradeType upgradeType : ATech.getCurrentlyUpgrading()) {
            paintSideMessage(upgradeType.toString(), Green);
        }
    }

    /**
     * Paints all pending contstructions, including those not yet started, even if only in the AI memory.
     */
    static void paintSidebarConstructionsPending() {
        int yOffset = 250;
        ArrayList<Construction> allOrders = ConstructionRequests.all();
        if (!allOrders.isEmpty()) {
            paintSideMessage("Constructing (" + allOrders.size() + ")", Color.White, yOffset);
            for (Construction construction : allOrders) {
                Color color = null;
                switch (construction.status()) {
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

                String status = construction.status().toString().replace("CONSTRUCTION_", "");
                String builderDist = A.dist(construction.builder(), construction.buildPosition());
                if (construction.builder() != null) {
                    String builder = (construction.builder().idWithHash() + " " + builderDist);
                    paintSideMessage(
                            construction.buildingType().name()
                            + ", " + construction.buildPosition()
                            + ", " + status + ", " + builder,
                            color,
                            yOffset
                    );
                }
            }
        }
    }

    /**
     * Paints places where buildings that do not yet exist are planned to be placed.
     */
    static void paintConstructionPlaces() {
        Color color = Color.Grey;
        for (Construction order : ConstructionRequests.all()) {
            if (order.status() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
//            if (order.getStatus() != ConstructionOrderStatus.CONSTRUCTION_FINISHED) {
                APosition positionToBuild = order.buildPosition();
                AUnitType buildingType = order.buildingType();
                if (positionToBuild == null || buildingType == null) {
                    continue;
                }

                paintConstructionPlace(positionToBuild, buildingType, null, color);
            }
        }
    }

    private static void paintConstructionPlace(APosition positionToBuild, AUnitType buildingType, String text, Color color) {
        if (positionToBuild == null) {
            if (Select.main() != null) {
                ErrorLog.printErrorOnce(buildingType + " has no position to build");
//                throw new Exception("That's unacceptable, lad!");
                return;
            } else {
                return;
            }
        }

        if (text == null) {
            text = buildingType.name();
        }

        // Paint box
        paintRectangle(positionToBuild, buildingType.getTileWidth() * 32, buildingType.getTileHeight() * 32, color);

        // Draw X
        paintLine(
                positionToBuild.translateByPixels(buildingType.getTileWidth() * 32, 0),
                positionToBuild.translateByPixels(0, buildingType.getTileHeight() * 32),
                color
        );
        paintLine(positionToBuild,
                buildingType.getTileWidth() * 32,
                buildingType.getTileHeight() * 32,
                color
        );

        // Draw text
        paintTextCentered(
                positionToBuild.translateByPixels(buildingType.dimensionLeft(), 69), text, color
        );
    }

    /**
     * Paints circles around units which mean what's their mission.
     */
    private static void paintColoredCirclesAroundUnits() {
        for (AUnit unit : Select.ourRealUnits().list()) {
            if (unit.isWorker() && (unit.isGatheringMinerals() || unit.isGatheringGas()) || unit.isLoaded()) {
                continue;
            }

            APosition unitPosition = unit.position();
            APosition targetPosition = unit.targetPosition();
            int unitRadius = unit.type().dimensionLeft();

            // STARTING ATTACK
//            if (unit.isStartingAttack()) {
//                paintCircle(unit, unitRadius - 7, Color.Orange);
//                paintCircle(unit, unitRadius - 6, Color.Orange);
//                paintCircle(unit, unitRadius - 5, Color.Orange);
//                paintCircle(unit, unitRadius - 4, Color.Orange);
//                paintCircle(unit, unitRadius - 3, Color.Orange);
//            }
            // ATTACK FRAME
//            if (unit.isAttackFrame()) {
//                paintRectangleFilled(unit.translateByPixels(-5, -10), 10, 20, Color.Red);
//            }
            // STUCK
            if (unit.isStuck()) {
                unit.setTooltipTactical("STUCK");
                paintCircle(unit, 2, Color.Teal);
                paintCircle(unit, 4, Color.Teal);
                paintCircle(unit, 6, Color.Teal);
                paintCircle(unit, 8, Color.Teal);
                paintCircle(unit, 10, Color.Teal);
            }
            // ATTACKING
//            if (unit.isAttackingOrMovingToAttack()) {
//                paintCircle(unit, unitRadius - 3, Color.Yellow);
//                paintCircle(unit, unitRadius - 2, Color.Yellow);
//            }
            // MOVE
//            if (unit.isMoving()) {
//                paintCircle(unit, unitRadius - 4, Color.Blue);
//                paintCircle(unit, unitRadius - 3, Color.Blue);
//                paintCircle(unit, unitRadius - 2, Color.Blue);
//                if (unit.targetPosition() != null) {
//                    paintCircleFilled(unit.targetPosition(), 4, Color.Blue);
//                    paintLine(unit.position(), unit.targetPosition(), Color.Blue);
//                }
//            }
//            // CONSTRUCTING
//            if (unit.isConstructing()) {
//                paintCircle(unit, 6, Color.Teal);
//                paintCircle(unit, 5, Color.Teal);
//            }

            // RUN
            if (unit.isRunning()) {
                paintLine(unit.position(), unit.runningManager().runToPosition(), Color.Yellow);
                paintLine(unit.translateByPixels(1, 1), unit.runningManager().runToPosition(), Color.Yellow);

                if (unit.runningManager().runToPosition() != null) {
                    paintCircleFilled(unit.runningManager().runToPosition(), 10, Color.Yellow);
                }

                paintWhiteFlagWhenRunning(unit);
            }

            // Paint hash + unit ID and unit action e.g. "ENGAGE", "ATTACK_UNIT" etc
            String action = unit.action() != null ? unit.action().toString() : null;
            System.out.println(action + " // " + ("ATTACK_UNIT".equals(action)));
            if ("ATTACK_UNIT".equals(action)) {
                action += ":" + unit.target().type();
            }
            paintTextCentered(unit.translateByTiles(0, 1),"#" + unit.id() + " " + action, Color.Cyan);

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
//            Color color = Color.Grey;
//            if (unit.action() != null) {
//                if (unit.getAction().equals(UnitActions.MOVE)) {
//                    color = Color.Teal;
//                } else if (unit.getAction().isAttacking()) {
//                    color = Color.Orange;
//                } else if (unit.getAction().equals(UnitActions.RETREAT)) {
//                    color = Color.Brown;
//                } else if (unit.getAction().equals(UnitActions.HEAL)) {
//                    color = Color.Purple;
//                } else if (unit.getAction().equals(UnitActions.BUILD)) {
//                    color = Color.Purple;
//                } else if (unit.getAction().equals(UnitActions.REPAIR)) {
//                    color = Color.Purple;
//                }
//            else if (unit.getAction().equals(UnitActions.)) {
//                color = Color.;
//            }
//            else if (unit.getAction().equals(UnitActions.)) {
//                color = Color.;
//            }
//            }

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

    private static void paintWhiteFlagWhenRunning(AUnit unit) {
        int flagWidth = 15;
        int flagHeight = 8;
        int dy = 12;
        Color flagColor = ShouldRetreat.shouldRetreat(unit) ? Color.Red : Color.White;

//        paintLine(unit, unit.targetPosition(), Color.Blue); // Where unit is running to

        paintRectangleFilled(unit.translateByPixels(0, -flagHeight - dy),
                flagWidth, flagHeight, flagColor); // White flag
        paintRectangle(unit.translateByPixels(0, -flagHeight - dy),
                flagWidth, flagHeight, Color.Grey); // Flag border
        paintRectangleFilled(unit.translateByPixels(-1, flagHeight - dy),
                2, flagHeight, Color.Grey); // Flag stick
    }

    /**
     * Paints progress bar with percent of completion over all buildings under construction.
     */
    static void paintConstructionProgress() {
        setTextSizeMedium();
//        for (AUnit unit : Select.ourBuildingsWithUnfinished().listUnits()) {
        for (Construction order : ConstructionRequests.all()) {
            AUnit building = order.construction();
            if (building == null || building.isCompleted()) {
                continue;
            }

            String stringToDisplay;

            int labelMaxWidth = 60;
            int labelHeight = 14;
            int labelLeft = building.position().getX() - labelMaxWidth / 2;
            int labelTop = building.position().getY() + 8;

            double progress = (double) building.hp() / building.maxHp();
            int labelProgress = (int) (1 + 99 * progress);

            // Paint box
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop).p(),
                    new APosition(labelLeft + labelMaxWidth * labelProgress / 100, labelTop + labelHeight).p(),
                    Color.Blue,
                    true
            );
            //bwapi.drawBox(new APosition(labelLeft, labelTop), new APosition(labelLeft + labelMaxWidth * labelProgress / 100, labelTop + labelHeight), Color.Blue, true, false);

            // Paint box borders
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop).p(),
                    new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight).p(),
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
                progressColor = Green;
            }
            stringToDisplay = labelProgress + "%";

            paintTextCentered(
                    new APosition(labelLeft + labelMaxWidth * 50 / 100 + 2, labelTop + 2),
                    stringToDisplay, progressColor
            );

            // =========================================================

            // Display name of unit
            // @TODO BUG_NULL happens with destroyed Turret in vsGosu
            String name = (building.buildType() != null ? building.buildType().name() : "-BUG_NULL");

            // Paint building name
            paintTextCentered(new APosition(building.position().getX(), building.position().getY() - 7),
                    name, Color.White);

            // Builder status
            AUnit builder = order.builder();
            boolean builderProblem = builder == null || !builder.isAlive();
            paintTextCentered(new APosition(building.position().getX(), building.position().getY() - 15),
                    builderProblem ? "NO BUILDER" : "", builderProblem ? Color.Red : Green);
        }

        setTextSizeSmall();
    }

    /**
     * For buildings not 100% healthy, paints its hit points using progress bar.
     */
    static void paintBuildingHealth() {
        for (AUnit unit : Select.ourBuildings().list()) {
            if (unit.isBunker() || unit.hp() >= unit.maxHp()) { //isWounded()
                continue;
            }
            int labelMaxWidth = 56;
            int labelHeight = 4;
            int labelLeft = unit.position().getX() - labelMaxWidth / 2;
            int labelTop = unit.position().getY() + 13;

            double hpRatio = (double) unit.hp() / unit.maxHp();
            int hpProgress = (int) (1 + 99 * hpRatio);

            Color color = Green;
            if (hpRatio < 0.66) {
                color = Color.Yellow;
                if (hpRatio < 0.33) {
                    color = Color.Red;
                }
            }

            // Paint box
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop).p(),
                    new APosition(labelLeft + labelMaxWidth * hpProgress / 100, labelTop + labelHeight).p(),
                    color,
                    true
            );

            // Paint box borders
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop).p(),
                    new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight).p(),
                    Color.Black,
                    false
            );
        }
    }

    /**
     * Paints the number of workers that are gathering to this building.
     */
    static void paintWorkersAssignedToBuildings() {
        setTextSizeLarge();
        for (AUnit building : Select.ourBuildings().list()) {
            if (!building.isBase() && !building.type().isGasBuilding()) {
                continue;
            }

            // Paint text
            int workers = WorkerRepository.getHowManyWorkersWorkingNear(building, false);
            if (workers > 0) {
                String workersAssigned = workers + "";
                paintTextCentered(building.translateByPixels(-5, -36), workersAssigned, Color.Grey);
            }
        }
        setTextSizeSmall();
    }

    /**
     * If buildings are training units, it paints what unit is trained and the progress.
     */
    static void paintBuildingsTrainingUnitsAndResearching() {
        setTextSizeMedium();
        for (AUnit building : Select.ourBuildings().list()) {
            if (!building.isBusy()) {
                continue;
            }

            // UNITS PRODUCED
            if (building.isTrainingAnyUnit()) {
                AUnitType unitType = building.trainingQueue().get(0);
                paintBuildingActionProgress(
                        building,
                        unitType.name(),
                        building.remainingTrainTime(),
                        unitType.totalTrainTime()
                );
            }

            // RESEARCHING
            else if (building.isResearching()) {
                TechType techType = building.whatIsResearching();
                paintBuildingActionProgress(
                        building,
                        techType.name(),
                        building.remainingResearchTime(),
                        techType.researchTime()
                );
            }

            // UPGRADING
            else if (building.isResearching()) {
                UpgradeType upgradeType = building.whatIsUpgrading();
                paintBuildingActionProgress(
                        building,
                        upgradeType.name(),
                        building.remainingUpgradeTime(),
                        upgradeType.upgradeTime()
                );
            }
        }
        setTextSizeSmall();
    }

    public static void paintBuildingActionProgress(AUnit building, String text, int remaining, int max) {
        int labelMaxWidth = 90;
        int labelHeight = 14;
        int labelLeft = building.position().getX() - labelMaxWidth / 2;
        int labelTop = building.position().getY();

        double operationProgress = (max - remaining) / (max + 1.0);

        // Paint box
        paintRectangleFilled(
                new APosition(labelLeft, labelTop), (int) (labelMaxWidth * operationProgress), labelHeight, Color.Grey
        );

        // Paint box borders
        paintRectangle(
                new APosition(labelLeft, labelTop), labelMaxWidth, labelHeight, Color.Black
        );

        // Display label
        paintTextCentered(
                new APosition(labelLeft + labelMaxWidth / 2, labelTop + 2), text, Color.White
        );
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

        paintMessage(Atlantis.KILLED + "", Green, x + dx, y, true);
        paintMessage(Atlantis.LOST + "", Color.Red, x + dx, y + dy, true);

        int balance = AGame.killsLossesResourceBalance();
        Color color = balance >= 0 ? Green : Color.Red;
        paintMessage((balance >= 0 ? "+" : "") + balance, color, x + dx, y + 3 * dy, true);
    }

    /**
     * Tooltips are units messages that appear over them and allow to report actions like "Repairing" or "Run
     * from enemy" etc.
     */
    static void paintTooltipsOverUnits() {
        for (AUnit unit : Select.our().list()) {
            if (unit.isBuilding() || !unit.isCompleted() || unit.isLoaded()) {
                continue;
            }

            if (unit.hasTooltip() && !unit.isGatheringMinerals() && !unit.isGatheringGas()) {
                String string = "";

                if (unit.hasTooltip()) {
                    string += unit.tooltip();
                } else {
                    string += "---";
                }

//            string += "/";
//
//            if (unit.getAction() != null) {
//                string += unit.getAction();
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
    public static void paintFoggedUnits() {
//        Selection buildings = EnemyUnits.foggedUnits().buildings();
//        if (buildings.notEmpty()) {
//            buildings.print("Enemy fogged buildings");
//        }

        for (AbstractFoggedUnit foggedEnemy : EnemyUnits.rawUnitsDiscovered()) {
            if (!foggedEnemy.hasPosition()) {
                continue;
            }

//            if (!foggedEnemy.position().isPositionVisible()) {
//                APainter.paintCircleFilled(foggedEnemy, 4, Color.Grey);
//            }

            APosition topLeft;
            AUnitType type = foggedEnemy.type();
            topLeft = foggedEnemy.translateByPixels(
                    -type.dimensionLeft(),
                    -type.dimensionUp()
            );
            paintRectangle(
                    foggedEnemy.position(),
                    type.dimensionRight() / 32,
                    type.dimensionDown() / 32,
                    Color.Grey
            );
            paintText(topLeft, type.name() + " (" + foggedEnemy.lastPositionUpdatedAgo() + ")", Color.White);
        }
    }

    /**
     * Every frame paint next allowed location of Supply Depot. Can be used to debug construction finding, but
     * slows the game down impossibly.
     */
    private static void paintTestSupplyDepotLocationsNearMain() {
        AUnit worker = Select.ourWorkers().first();
        AUnit base = Select.ourBases().first();
        int tileX = base.position().tx();
        int tileY = base.position().ty();
        for (int x = tileX - 10; x <= tileX + 10; x++) {
            for (int y = tileY - 10; y <= tileY + 10; y++) {
                APosition position = APosition.create(x, y);
                boolean canBuild = TerranPositionFinder.doesPositionFulfillAllConditions(
                        worker, AUnitType.Terran_Supply_Depot, position
                );

                paintCircleFilled(position, 4, canBuild ? Green : Color.Red);

                if (x == tileX && y == tileY) {
                    paintCircleFilled(position, 10, canBuild ? Green : Color.Red);
                }
            }
        }
    }

    /**
     * Can be helpful to illustrate or debug behavior or worker unit which is scouting around enemy base.
     */
    private static void paintEnemyRegionDetails() {
        AUnit enemyBase = EnemyUnits.enemyBase();
        if (enemyBase != null) {
//            ARegion enemyBaseRegion = Regions.getRegion(enemyBase);
//            Position polygonCenter = enemyBaseRegion.getPolygon().getCenter();
//            APosition polygonCenter = APosition.create(enemyBaseRegion.getPolygon().getCenter());
            for (ARegionBoundary point : ScoutManager.scoutingAroundBasePoints.arrayList()) {
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

        paintSquadsInfo(x, y);
    }

    private static void paintSquadsInfo(int x, int y) {
        y += 26;

        if (Alpha.get().isNotEmpty()) {
            paintMessage("Squads: ", Color.White, x, y, true);
            for (Squad squad : AllSquads.all()) {
                if (squad.size() == 0) {
                    continue;
                }
                paintMessage(
                    squad.name() + ": " + squad.size() + "/" + squad.mission().name().substring(0, 3),
                    squad.isEmpty() ? Color.Red : Color.White,
                    x, y += 12, true
                );
            }
        }
    }

    private static void paintSquads() {
        for (Squad squad : AllSquads.all()) {
            AUnit centerUnit = squad.leader();
            if (centerUnit != null) {
                int maxDist = (int) squad.radius();

                paintCircle(centerUnit, maxDist + 1, Grey);
                paintCircle(centerUnit, maxDist, Grey);

                setTextSizeMedium();
                paintTextCentered(centerUnit, squad.cohesionPercent() + "%", Color.Teal, 0, -(maxDist / 32.0) + 0.12);
            }
        }
    }

    private static void paintGameLog() {
        int x = rightSideMessageLeftOffset - 130;
        int y = rightSideMessageTopOffset;

        int counter = 0;
        setTextSizeSmall();
        for (LogMessage log : GameLog.get().messages()) {
            paintMessage(log.message(), log.color(), x, y - 12 * counter++, true);
        }
    }

    private static void paintCooldown(AUnit unit) {
        boolean shouldAvoidAnyUnit = (new AvoidEnemies(unit)).shouldAvoidAnyUnit();

//        paintUnitProgressBar(unit, 27, 100, Color.Grey);
        paintUnitProgressBar(unit, 22, unit.cooldownPercent(), shouldAvoidAnyUnit ? Color.Red : Color.Teal);
    }

    private static void paintLifeBar(AUnit unit) {
        Color color = unit.isOur() ? Green : Color.Yellow;

//        if (unit.isWounded()) {
        paintUnitProgressBar(unit, 10, 100, Color.Red);
        paintUnitProgressBar(unit, 10, unit.hpPercent(), color);
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
        AUnit main = Select.main();
        if (main == null) {
            return;
        }

        ARegion mainRegion = main.position().region();
        if (mainRegion == null) {
            return;
        }

        paintRegionBoundaries(mainRegion);

        AUnit enemyBase = EnemyUnits.enemyBase();
        if (enemyBase != null) {
            paintRegionBoundaries(enemyBase.position().region());
        }

//        List<ARegion> regions = Regions.regions();
//        for (ARegion region : regions) {
//            paintRectangle(
//                    region.center().translateByTiles(-3, -3),
//                    6,
//                    6,
//                    Color.Brown
//            );
//            paintTextCentered(
//                    region.center(),
//                    region.toString(),
//                    Color.Brown
//            );
//        }
    }

    protected static void paintRegionBoundaries(ARegion region) {
        if (region == null) {
            return;
        }

        paintCircle(region.center(), 6, Color.Brown);
        paintCircle(region.center(), 5, Color.Brown);

        ArrayList<ARegionBoundary> boundaries = region.boundaries();
        for (ARegionBoundary boundary : boundaries) {
            APosition position = boundary.position();
            Color color = Color.Grey;
//            Color color = Color.Green;
            paintCircle(position, 4, color);
            paintCircle(position, 3, color);
        }
    }

    protected static void paintChokepoints() {

        // All chokes
        List<AChoke> chokePoints = Chokes.chokes();
        AUnit main = Select.main();
        for (AChoke choke : chokePoints) {
            String text = main == null ? "" : (((int) main.groundDist(choke)) + "");
            paintChoke(choke, Color.Brown, text, 1.3);
        }
    }

    protected static void paintStrategicLocations() {
        if (AGame.isUms()) {
            return;
        }

        setTextSizeMedium();

        // Natural base
        APosition natural = Bases.natural();
        paintBase(natural, "Our natural", Color.Grey, 0);

        // Enemy base
        APosition enemyBase = Bases.enemyNatural();
        paintBase(enemyBase, "Enemy natural", Color.Orange, 0);

        // Our natural choke
        AChoke naturalChoke = Chokes.natural(Bases.natural());
        paintChoke(naturalChoke, Green, "Natural choke");

        // Our main choke
        AChoke mainChoke = Chokes.mainChoke();
        paintChoke(mainChoke, Green, "Main choke");

        // Enemy natural choke
        AChoke enemyNaturalChoke = Chokes.enemyNaturalChoke();
        paintChoke(enemyNaturalChoke, Color.Orange, "Enemy natural choke");

        // First Pylon
//        paintBuildingPosition(
//            PylonPosition.positionForFirstPylon(), AUnitType.Protoss_Pylon, "First pylon", Color.Green
//        );

        // Bunker
//        TerranBunkerPositionFinder.findPosition(Select.ourWorkers().first(), null);

        // Sunken
        if (We.zerg()) {
            paintBuildingPosition((new ZergSunkenColony()).nextBuildingPosition(), "Next Sunken");
        }
        if (We.terran()) {
            paintBuildingPosition((new TerranBunker()).nextBuildingPosition(), "Next Bunker");
        }

        // Next defensive building position
//        if (Count.bases() > 0) {
//            AUnitType building = AAntiLandBuildingRequests.building();
//            paintConstructionPlace(AAntiLandBuildingRequests.positionForNextBuilding(), building, building.name(), Color.Brown);
//        }

//        paintTurretsInMain();
    }

    private static void paintTurretsInMain() {
        ArrayList<APosition> turrets = (new TerranMissileTurretsForMain()).positionsForTurretsNearMainBorder();
//        System.out.println("turrets = " + turrets.size());
        for (APosition turret : turrets) {
//            System.out.println("turret = " + turret);
            paintRectangle(turret, 34, 34, Color.Purple);
            paintRectangle(turret.translateByPixels(1, 1), 32, 32, Color.Purple);
            paintTextCentered(turret.translateByPixels(17, 12), "Turret", Color.Purple);

//            CameraCommander.centerCameraOn(turret);
        }
    }

    private static void paintMineralDistance() {
        AUnit mainBase = Select.main();
        if (mainBase == null) {
            return;
        }

        for (AUnit mineral : Select.minerals().inRadius(8, mainBase).list()) {
            String dist = A.digit(mineral.distTo(mainBase));
            int assigned = WorkerRepository.countWorkersAssignedTo(mineral);
            setTextSizeLarge();
            paintTextCentered(mineral, dist + " (" + assigned + ")", Color.White);
        }

        if (A.now() <= 100) {
            for (AUnit worker : Select.ourWorkers().list()) {
                if (worker.target() != null) {
                    paintLine(worker, worker.target(), Color.Grey);
                }
            }
        }
    }

    public static void paintBuildingPosition(HasPosition position, String text) {
        if (position == null) {
            return;
        }

        double dtx = 1;
        paintRectangle(position, 2, 2, Color.Orange);
        paintTextCentered(position.translateByTiles(dtx, 1), text, Color.Orange);
    }

    public static void paintPosition(HasPosition position, String text) {
        if (position == null) {
            return;
        }

        double dtx = 1;
        paintRectangle(position.translateByTiles(-dtx, -dtx), 2 * 32, 2 * 32, Color.Orange);
        paintTextCentered(position.translateByTiles(dtx, 1), text, Color.Orange);
    }

    public static void paintChoke(AChoke choke, Color color, String extraText) {
        paintChoke(choke, color, extraText, 0);
    }

    public static void paintChoke(AChoke choke, Color color, String extraText, double extraDy) {
        if (choke == null || isDisabled()) {
            return;
        }

        if ("".equals(extraText)) {
            extraText = choke.width() + " wide choke";
        }

//        for (WalkPosition walkPosition : choke.rawChoke().getGeometry()) {
////            paintRectangle(APosition.create(walkPosition), 32, 32, Color.Brown);
//            paintCircle(APosition.create(walkPosition), 32, Color.Brown);
//        }

        // Paint line perpendicular to the choke, useful for blocking it
        paintLine(choke.firstPoint(), choke.lastPoint(), Color.Brown);

        paintCircle(choke.center(), choke.width() * 32, color);
        paintTextCentered(
            choke.center().translateByTiles(0, choke.width() + extraDy),
            extraText,
            color
        );
    }

    public static void paintBases() {
        AUnit main = Select.main();
        for (ABaseLocation base : Bases.baseLocations()) {
            if (base.isStartLocation()) {
                continue;
            }
            String away = main == null ? "" : (A.distGround(main, base.position()) + " away");
            AAdvancedPainter.paintBase(base.position(), "Base " + away, Color.Yellow, -0.3);
        }
    }

    public static void paintBase(APosition position, String text, Color color, double extraDy) {
        if (position == null || isDisabled()) {
            return;
        }

        paintRectangle(
//                position.translateByPixels(-2 * 32, (int) -1.5 * 32),
            position,
            4 * 32, 3 * 32, color
        );
        APainter.paintTextCentered(position.translateByTiles(1, -0.4 + extraDy), text, color);
    }

    protected static void paintBuildingPosition(APosition position, AUnitType type, String text, Color color) {
        if (position == null || isDisabled()) {
            return;
        }

        paintRectangle(
            position.translateByPixels(-2 * 32, (int) -1.5 * 32),
            type.getTileWidth() * 32, type.getTileHeight() * 32, color
        );
        APainter.paintTextCentered(position.translateByTiles(1, -1), text, color);
    }
}
