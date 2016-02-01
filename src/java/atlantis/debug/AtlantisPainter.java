package atlantis.debug;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.buildings.managers.AtlantisGasManager;
import atlantis.combat.AtlantisCombatEvaluator;
import atlantis.combat.group.AtlantisGroupManager;
import atlantis.combat.group.missions.MissionAttack;
import atlantis.combat.group.missions.MissionDefend;
import atlantis.combat.group.missions.MissionPrepare;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.constructing.ConstructionOrder;
import atlantis.constructing.ConstructionOrderStatus;
import atlantis.production.ProductionOrder;
import atlantis.util.RUtilities;
import atlantis.workers.AtlantisWorkerManager;
import atlantis.wrappers.MappingCounter;
import atlantis.wrappers.SelectUnits;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.util.BWColor;

/**
 * Here you can include code that will draw extra informations over units etc.
 */
public class AtlantisPainter {

    private static JNIBWAPI bwapi;
    private static int sideMessageTopCounter = 0;
    private static int sideMessageMiddleCounter = 0;
    private static int sideMessageBottomCounter = 0;
    
    /**
     * List of enemy units that have been targetted in the last frame.
     */
//    private static TreeSet<Unit> _temporaryTargets = new TreeSet<>();

    // =========================================================
    /**
     * Executed once per frame, at the end of all other actions.
     */
    public static void paint() {
        sideMessageTopCounter = 0;
        sideMessageBottomCounter = 0;
        bwapi = Atlantis.getBwapi();

        // =========================================================
//        bwapi.drawTargets(true); // Draws line from unit to the target position
//        bwapi.getMap().drawTerrainData(bwapi);
        // =========================================================
        // Paint from least important to most important (last is on the top)
        paintImportantPlaces();
        paintColorCirclesAroundUnits();
        paintConstructionProgress();
        paintConstructionPlaces();
        paintBuildingHealth();
        paintWorkersAssignedToBuildings();
        paintUnitsBeingTrainedInBuildings();
        paintSpecialsOverUnits();
        paintVariousStats();
        paintUnitCounters();
        paintProductionQueue();
        paintConstructionsPending();
        paintKilledAndLost();
        paintTemporaryTargets();

        // =========================================================
        // Paint TOOLTIPS over units
        for (Unit unit : SelectUnits.our().list()) {
            if (unit.hasTooltip()) {
                paintTextCentered(unit, unit.getTooltip(), false);
            }
        }
    }

    // =========================================================
    // Hi-level
    
    /**
     * Paint focus point for global attack mission etc.
     */
    private static void paintVariousStats() {
        
        // =========================================================
        // Time
        paintSideMessage("Time: " + AtlantisGame.getTimeSeconds() + "s", BWColor.Grey);
        
        // =========================================================
        // Gas workers
        paintSideMessage("Gas workers: " + AtlantisGasManager.defineMinGasWorkersPerBuilding(), BWColor.Grey);
        
        // =========================================================
        // Global mission
        paintSideMessage("Mission: " + AtlantisGroupManager.getAlphaGroup().getMission().getName(), BWColor.White);
        
        // =========================================================
        // Focus point
        
        Position focusPoint = MissionAttack.getFocusPoint();
        String desc = "";
        Unit mainBase = SelectUnits.mainBase();
        if (focusPoint != null && mainBase != null) {
            desc = "(dist:" + ((int) focusPoint.distanceTo(mainBase)) + ")";
        }
        paintSideMessage("Focus point: " + focusPoint + desc, BWColor.Blue, 0);
        
        // =========================================================
        
        paintSideMessage("Combat group size: " + AtlantisGroupManager.getAlphaGroup().size(), BWColor.Blue, 0);
    }
    
    /**
     * Paints small progress bars over units that have cooldown.
     */
    private static void paintSpecialsOverUnits() {
        for (Unit unit : SelectUnits.ourCombatUnits().list()) {

            // =========================================================
            // === Paint life bars bars over wounded units
            // =========================================================
            if (unit.getHPPercent() < 100) {
                int boxWidth = 20;
                int boxHeight = 4;
                int boxLeft = unit.getPX() - boxWidth / 2;
                int boxTop = unit.getPY() + 23;

                Position topLeft = new Position(boxLeft, boxTop);

                // =========================================================
                // Paint box
                int healthBarProgress = boxWidth * unit.getHP() / (unit.getMaxHP() + 1);
                bwapi.drawBox(topLeft, new Position(boxLeft + boxWidth, boxTop + boxHeight),
                        BWColor.Red, true, false);
                bwapi.drawBox(topLeft, new Position(boxLeft + healthBarProgress, boxTop + boxHeight),
                        BWColor.Green, true, false);

                // =========================================================
                // Paint box borders
                bwapi.drawBox(topLeft, new Position(boxLeft + boxWidth, boxTop + boxHeight),
                        BWColor.Black, false, false);
            }

            // =========================================================
            // === Paint cooldown progress bars over units
            // =========================================================
//            if (unit.getGroundWeaponCooldown() > 0) {
//                int cooldownWidth = 20;
//                int cooldownHeight = 4;
//                int cooldownLeft = unit.getPX() - cooldownWidth / 2;
//                int cooldownTop = unit.getPY() + 23;
//                String cooldown = BWColor.getColorString(BWColor.Yellow) + "(" + unit.getGroundWeaponCooldown() + ")";
//
//                Position topLeft = new Position(cooldownLeft, cooldownTop);
//
//                // =========================================================
//                // Paint box
//                int cooldownProgress = cooldownWidth * unit.getGroundWeaponCooldown()
//                        / (unit.getType().getGroundWeapon().getDamageCooldown() + 1);
//                bwapi.drawBox(topLeft, new Position(cooldownLeft + cooldownProgress, cooldownTop + cooldownHeight),
//                        BWColor.Brown, true, false);
//
//                // =========================================================
//                // Paint box borders
//                bwapi.drawBox(topLeft, new Position(cooldownLeft + cooldownWidth, cooldownTop + cooldownHeight),
//                        BWColor.Black, false, false);
//
//                // =========================================================
//                // Paint label
////                paintTextCentered(new Position(cooldownLeft + cooldownWidth - 4, cooldownTop), cooldown, false);
//            }

            // =========================================================
            // === Paint battle group
            // =========================================================
//            if (unit.getGroup() != null) {
//                paintTextCentered(new Position(unit.getPX(), unit.getPY() + 3), BWColor.getColorString(BWColor.Grey)
//                        + "#" + unit.getGroup().getID(), false);
//            }
            
            // =========================================================
            // === Paint num of other units around this unit
            // =========================================================
//            int ourAround = SelectUnits.ourCombatUnits().inRadius(1.7, unit).count();
//            paintTextCentered(new Position(unit.getPX(), unit.getPY() - 15), BWColor.getColorString(BWColor.Orange)
//                    + "(" + ourAround + ")", false);
            // =========================================================
            // === Combat Evaluation Strength
            // =========================================================
            if (AtlantisCombatEvaluator.evaluateSituation(unit) < 3) {
                String combatStrength = AtlantisCombatEvaluator.getEvalString(unit);
                paintTextCentered(new Position(unit.getPX(), unit.getPY() - 15), combatStrength, null);
            }
        }
    }

    /**
     * Paints important choke point near the base.
     */
    private static void paintImportantPlaces() {
        Position position;

        // Main DEFEND focus point
        position = MissionDefend.getFocusPoint();
        paintCircle(position, 20, BWColor.Black);
        paintCircle(position, 19, BWColor.Black);
        paintTextCentered(position, "DEFEND", BWColor.Grey);

        // Mission PREPARE focus point
        position = MissionPrepare.getFocusPoint();
        paintCircle(position, 20, BWColor.Black);
        paintCircle(position, 19, BWColor.Black);
        paintTextCentered(position, "PREPARE", BWColor.Grey);

        // Mission ATTACK focus point
        position = MissionAttack.getFocusPoint();
        paintCircle(position, 20, BWColor.Red);
//        paintCircle(position, 19, BWColor.Black);
        paintTextCentered(position, "ATTACK", BWColor.Red);
    }
    
    /**
     * Paints list of units we have in top left corner.
     */
    private static void paintUnitCounters() {
        // Unfinished
        MappingCounter<UnitType> unitTypesCounter = new MappingCounter<>();
        for (Unit unit : SelectUnits.ourUnfinishedRealUnits().list()) {
            unitTypesCounter.incrementValueFor(unit.getType());
        }

        Map<UnitType, Integer> counters = unitTypesCounter.map();
        counters = RUtilities.sortByValue(counters, false);
        boolean paintedMessage = false;
        for (UnitType unitType : counters.keySet()) {
            paintSideMessage("+" + counters.get(unitType) + " " + unitType.getName(), BWColor.Blue, 0);
            paintedMessage = true;
        }

        if (paintedMessage) {
            paintSideMessage("", BWColor.White, 0);
        }

        // =========================================================
        // Finished
        unitTypesCounter = new MappingCounter<>();
        for (Unit unit : SelectUnits.our().list()) {
            unitTypesCounter.incrementValueFor(unit.getType());
        }

        counters = unitTypesCounter.map();
        counters = RUtilities.sortByValue(counters, false);
        for (UnitType unitType : counters.keySet()) {
            if (!unitType.isBuilding()) {
                paintSideMessage(counters.get(unitType) + "x " + unitType.getName(), BWColor.Grey, 0);
            }
        }
        paintSideMessage("", BWColor.White, 0);
    }

    /**
     * Paints next units to build in top left corner.
     */
    private static void paintProductionQueue() {
        paintSideMessage("Prod. queue:", BWColor.White);

        // Display units currently in production
        for (Unit unit : SelectUnits.ourUnfinished().list()) {
            UnitType type = unit.getType();
            if (type.equals(UnitType.UnitTypes.Zerg_Egg)) {
                type = unit.getBuildType();
            }
            paintSideMessage(type.getShortName(), BWColor.Green);
        }

        // Display units that should be produced right now or any time
        ArrayList<ProductionOrder> produceNow = AtlantisGame.getProductionStrategy().getThingsToProduceRightNow(false);
        for (ProductionOrder order : produceNow) {
            paintSideMessage(order.getShortName(), BWColor.Yellow);
        }

        // Display next units to produce
        ArrayList<ProductionOrder> fullQueue = AtlantisGame.getProductionStrategy().getProductionQueueNext(
                5 - produceNow.size());
        for (int index = produceNow.size(); index < fullQueue.size(); index++) {
            ProductionOrder order = fullQueue.get(index);
            if (order != null && order.getShortName() != null) {
                if (order.getUnitType() != null 
                        && !AtlantisGame.hasBuildingsToProduce(order.getUnitType())) {
                    continue;
                }
                paintSideMessage(order.getShortName(), BWColor.Red);
            }
        }
    }

    /**
     * Paints all pending contstructions, including those not yet started, even if only in the AI memory.
     */
    private static void paintConstructionsPending() {
        int yOffset = 205;
        ArrayList<ConstructionOrder> allOrders = AtlantisConstructingManager.getAllConstructionOrders();
        if (!allOrders.isEmpty()) {
            paintSideMessage("Constructing (" + allOrders.size() + ")", BWColor.White, yOffset);
            for (ConstructionOrder constructionOrder : allOrders) {
                BWColor color = null;
                switch (constructionOrder.getStatus()) {
                    case CONSTRUCTION_NOT_STARTED:
                        color = BWColor.Red;
                        break;
                    case CONSTRUCTION_IN_PROGRESS:
                        color = BWColor.Blue;
                        break;
                    case CONSTRUCTION_FINISHED:
                        color = BWColor.Teal;
                        break;
                    default:
                        color = BWColor.Teal;
                        break;
                }
                paintSideMessage(constructionOrder.getBuildingType().getShortName(), color, yOffset);
            }
        }
    }

    /**
     * Paints places where buildings that do not yet exist are planned to be placed.
     */
    private static void paintConstructionPlaces() {
        for (ConstructionOrder order : AtlantisConstructingManager.getAllConstructionOrders()) {
            if (order.getStatus() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
                Position positionToBuild = order.getPositionToBuild();
                UnitType buildingType = order.getBuildingType();
                if (positionToBuild == null || buildingType == null) {
                    continue;
                }

                // Paint box
                bwapi.drawBox(
                        positionToBuild,
                        positionToBuild.translated(buildingType.getTileWidth() * 32, buildingType.getTileHeight() * 32),
                        BWColor.Teal, false, false);

                // Draw X
                bwapi.drawLine(
                        positionToBuild,
                        positionToBuild.translated(buildingType.getTileWidth() * 32, buildingType.getTileHeight() * 32),
                        BWColor.Teal, false);
                bwapi.drawLine(
                        positionToBuild.translated(buildingType.getTileWidth() * 32, 0),
                        positionToBuild.translated(0, buildingType.getTileHeight() * 32),
                        BWColor.Teal, false);
                
                // Draw text
                paintTextCentered(positionToBuild, buildingType.getShortName(), BWColor.Grey);
            }
        }
    }

    /**
     * Paints circles around units which mean what's their mission.
     */
    private static void paintColorCirclesAroundUnits() {
        for (Unit unit : SelectUnits.ourCombatUnits().list()) {

            // STARTING ATTACK
//            if (unit.isStartingAttack()) {
//                bwapi.drawCircle(unit, 10, BWColor.Yellow, false, false);
//                bwapi.drawCircle(unit, 9, BWColor.Yellow, false, false);
//            }
            // ATTACK FRAME
//            if (unit.isAttackFrame()) {
//                bwapi.drawCircle(unit, 13, BWColor.Orange, false, false);
//                bwapi.drawCircle(unit, 14, BWColor.Orange, false, false);
//            }
            // ATTACKING
//            if (unit.isAttacking()) {
//                bwapi.drawCircle(unit, 12, BWColor.Red, false, false);
//                bwapi.drawCircle(unit, 11, BWColor.Red, false, false);
//            }
            // MOVE
//            if (unit.isMoving()) {
//                bwapi.drawCircle(unit, 8, BWColor.Teal, false, false);
//                bwapi.drawCircle(unit, 7, BWColor.Teal, false, false);
//            }

            // RUN
            if (unit.isRunning()) {
                paintLine(unit, unit.getRunning().getNextPositionToRunTo(), BWColor.Blue);
            }
        }
    }

    /**
     * Paints progress bar with percent of completion over all buildings under construction.
     */
    private static void paintConstructionProgress() {
        for (Unit unit : SelectUnits.ourBuildingsIncludingUnfinished().list()) {
            if (unit.isCompleted()) {
                continue;
            }

            String stringToDisplay;

            int labelMaxWidth = 56;
            int labelHeight = 6;
            int labelLeft = unit.getPX() - labelMaxWidth / 2;
            int labelTop = unit.getPY() + 13;

            double progress = (double) unit.getHP() / unit.getType().getMaxHitPoints();
            int labelProgress = (int) (1 + 99 * progress);
            String color = RUtilities.assignStringForValue(
                    progress,
                    1.0,
                    0.0,
                    new String[]{BWColor.getColorString(BWColor.Red), BWColor.getColorString(BWColor.Yellow),
                        BWColor.getColorString(BWColor.Green)});
            stringToDisplay = color + labelProgress + "%";

            // Paint box
            bwapi.drawBox(new Position(labelLeft, labelTop), new Position(labelLeft + labelMaxWidth * labelProgress
                    / 100, labelTop + labelHeight), BWColor.Blue, true, false);

            // Paint box borders
            bwapi.drawBox(new Position(labelLeft, labelTop), new Position(labelLeft + labelMaxWidth, labelTop
                    + labelHeight), BWColor.Black, false, false);

            // Paint label
            paintTextCentered(new Position(labelLeft, labelTop - 3), stringToDisplay, false);

            // Display name of unit
            String name = unit.getBuildType().getShortName();
            paintTextCentered(new Position(unit.getPX(), unit.getPY() - 4), BWColor.getColorString(BWColor.Green)
                    + name, false);
        }
    }

    /**
     * For buildings not 100% healthy, paints its hit points using progress bar.
     */
    private static void paintBuildingHealth() {
        for (Unit unit : SelectUnits.ourBuildings().list()) {
            if (!unit.isWounded()) {
                continue;
            }
            int labelMaxWidth = 56;
            int labelHeight = 4;
            int labelLeft = unit.getPX() - labelMaxWidth / 2;
            int labelTop = unit.getPY() + 13;

            double hpRatio = (double) unit.getHP() / unit.getType().getMaxHitPoints();
            int hpProgress = (int) (1 + 99 * hpRatio);

            BWColor color = BWColor.Green;
            if (hpRatio < 0.66) {
                color = BWColor.Yellow;
                if (hpRatio < 0.33) {
                    color = BWColor.Red;
                }
            }

            // Paint box
            bwapi.drawBox(new Position(labelLeft, labelTop), new Position(labelLeft + labelMaxWidth * hpProgress / 100,
                    labelTop + labelHeight), color, true, false);

            // Paint box borders
            bwapi.drawBox(new Position(labelLeft, labelTop), new Position(labelLeft + labelMaxWidth, labelTop
                    + labelHeight), BWColor.Black, false, false);
        }
    }

    /**
     * Paints the number of workers that are gathering to this building.
     */
    private static void paintWorkersAssignedToBuildings() {
        for (Unit building : SelectUnits.ourBuildings().list()) {

            // Paint text
            int workers = AtlantisWorkerManager.getHowManyWorkersAt(building);
            if (workers > 0) {
                String workersAssigned = "Workers: " + workers;
                paintTextCentered(building.translated(0, -15), workersAssigned, BWColor.Blue);
            }
        }
    }

    /**
     * If buildings are training units, it paints what unit is trained and the progress.
     */
    private static void paintUnitsBeingTrainedInBuildings() {
        for (Unit unit : SelectUnits.ourBuildingsIncludingUnfinished().list()) {
            if (!unit.isBuilding() || !unit.isTraining()) {
                continue;
            }

            int labelMaxWidth = 100;
            int labelHeight = 10;
            int labelLeft = unit.getPX() - labelMaxWidth / 2;
            int labelTop = unit.getPY() + 5;

            int operationProgress = 1;
            Unit trained = unit.getBuildUnit();
            String trainedUnitString = "";
            if (trained != null) {
                operationProgress = trained.getHP() * 100 / trained.getMaxHP();
                trainedUnitString = trained.getShortName();
            }

            // Paint box
            bwapi.drawBox(new Position(labelLeft, labelTop), new Position(labelLeft + labelMaxWidth * operationProgress
                    / 100, labelTop + labelHeight), BWColor.White, true, false);

            // Paint box borders
            bwapi.drawBox(new Position(labelLeft, labelTop), new Position(labelLeft + labelMaxWidth, labelTop
                    + labelHeight), BWColor.Black, false, false);

            // =========================================================
            // Display label
            paintTextCentered(new Position(unit.getPX() - 4 * trainedUnitString.length(), unit.getPY() + 16),
                    BWColor.getColorString(BWColor.White) + trainedUnitString, false);
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

        paintMessage("Killed: ", BWColor.White, x, y, true);
        paintMessage("Lost: ", BWColor.White, x, y + dy, true);
        paintMessage("-----------", BWColor.Grey, x, y + 2 * dy, true);
        paintMessage("Price: ", BWColor.White, x, y + 3 * dy, true);

        paintMessage(Atlantis.KILLED + "", BWColor.Green, x + dx, y, true);
        paintMessage(Atlantis.LOST + "", BWColor.Red, x + dx, y + dy, true);

        int balance = Atlantis.KILLED_RESOURCES - Atlantis.LOST_RESOURCES;
        BWColor color = balance >= 0 ? BWColor.Green : BWColor.Red;
        paintMessage((balance >= 0 ? "+" : "") + balance, color, x + dx, y + 3 * dy, true);
    }
    
    /**
     * Paint red "X" on every enemy unit that has been targetted.
     */
    private static void paintTemporaryTargets() {
        for (Unit ourUnit : SelectUnits.our().list()) {
            
            // Paint "x" on every unit that has been targetted by one of our units.
            if (ourUnit.isAttacking() && ourUnit.getTarget() != null) {
                paintMessage("X", BWColor.Red, ourUnit.getTarget().getPX(), ourUnit.getTarget().getPY(), false);
            }
        }
    }

    // =========================================================
    // Lo-level
    private static void paintSideMessage(String text, BWColor color) {
        paintSideMessage(text, color, 0);
    }

    private static void paintSideMessage(String text, BWColor color, int yOffset) {
        if (color == null) {
            color = BWColor.White;
        }

        int screenX = 10;
        int screenY = 10 + 15 * (yOffset == 0 ? sideMessageTopCounter : sideMessageBottomCounter);
        paintMessage(text, color, screenX, yOffset + screenY, true);

        if (yOffset == 0) {
            sideMessageTopCounter++;
        } else {
            sideMessageBottomCounter++;
        }
    }

    private static void paintMessage(String text, BWColor color, int x, int y, boolean screenCoord) {
        getBwapi().drawText(new Position(x, y), BWColor.getColorString(color) + text, screenCoord);
    }

    private static void paintCircle(Position position, int radius, BWColor color) {
        if (position == null) {
            return;
        }
        getBwapi().drawCircle(position, radius, color, false, false);
    }

    private static void paintLine(Position start, Position end, BWColor color) {
        if (start == null || end == null) {
            return;
        }
        getBwapi().drawLine(start, end, color, false);
    }

    private static void paintTextCentered(Position position, String text, BWColor color) {
        paintTextCentered(position, text, color, false);
    }

    private static void paintTextCentered(Position position, String text, boolean screenCords) {
        paintTextCentered(position, text, null, screenCords);
    }

    private static void paintTextCentered(Position position, String text, BWColor color, boolean screenCords) {
        if (position == null || text == null) {
            return;
        }
        getBwapi().drawText(position.translated((int) (-3.7 * text.length()), -2), BWColor.getColorString(color) + text, screenCords);
    }

    private static JNIBWAPI getBwapi() {
        return Atlantis.getBwapi();
    }

}
