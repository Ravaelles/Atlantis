package atlantis.combat.retreating;

import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.game.GameSpeed;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.units.actions.Action;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Vector;
import atlantis.util.We;
import bwapi.Color;
import java.util.ArrayList;

public class ARunningManager {

    //    public static double MIN_DIST_TO_REGION_BOUNDARY = 1;
    public static int STOP_RUNNING_IF_STOPPED_MORE_THAN_AGO = 8;
    public static int STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO = 6;
    public static double Near_UNIT_MAKE_SPACE = 0.75;
    //    private static final double SHOW_BACK_TO_ENEMY_DIST_MIN = 2;
    private static final double SHOW_BACK_DIST_DEFAULT = 6;
    private static final double SHOW_BACK_DIST_VULTURE = 5;
    private static final double SHOW_BACK_DIST_DRAGOON = 3;
    public static int ANY_DIRECTION_RADIUS_DEFAULT = 3;
    public static int ANY_DIRECTION_RADIUS_VULTURE = 4;
    public static int ANY_DIRECTION_RADIUS_DRAGOON = 4;
    public static int ANY_DIRECTION_RADIUS_INFANTRY = 2;
    public static double NOTIFY_UNITS_IN_RADIUS = 0.65;

    private final AUnit unit;
    private APosition runTo = null;
    private boolean fallbackMode = false; // When nothing else seems to work

    // =========================================================

    public ARunningManager(AUnit unit) {
        this.unit = unit;
    }

    // =========================================================

    public static boolean shouldStopRunning(AUnit unit) {
//        System.out.println(unit.id() + " // " + unit.isRunning()
//                + " // " + AAvoidUnits.shouldNotAvoidAnyUnit(unit));
//        System.out.println(unit.isRunning() + " // " + unit.runningManager().isRunning() + " // " + unit.action().isRunning());
        if (!unit.isRunning()) {
            return false;
        }

        if (unit.isUnitAction(Actions.RUN_IN_ANY_DIRECTION) && unit.lastActionLessThanAgo(20)) {
            unit.setTooltipTactical("InAnyDir");
            return false;
        }

        if (unit.hp() > 30 && unit.lastStartedRunningMoreThanAgo(150)) {
            unit.setTooltipTactical("RanTooLong");
            return true;
        }

        if (We.terran() && unit.isHealthy() && unit.lastUnderAttackLessThanAgo(30)) {
            unit.setTooltipTactical("HealthyNow");
            return true;
        }

        if (
            unit.lastStartedRunningMoreThanAgo(20) && !AvoidEnemies.shouldNotAvoidAnyUnit(unit))
        {
            unit.setTooltip("StopMan", false);
            return true;
        }

        if (unit.isWounded() && unit.nearestEnemyDist() >= 3) {
            return false;
        }

        if (
            unit.lastStoppedRunningMoreThanAgo(STOP_RUNNING_IF_STOPPED_MORE_THAN_AGO)
                && unit.lastStartedRunningMoreThanAgo(STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO)
                && !unit.isUnderAttack(unit.isAir() ? 250 : 5)
                //                && AAvoidUnits.shouldNotAvoidAnyUnit(unit)
                || AvoidEnemies.shouldNotAvoidAnyUnit(unit)
        ) {
            unit.setTooltip("StopRun", false);
            return true;
        }

        return false;
    }

//    public boolean runFromHere() {
//        return runFrom(null, -1);
//    }

    //    public boolean runFrom(Object unitOrPosition, double dist) {
    public boolean runFrom(HasPosition runAwayFrom, double dist, Action action) {
        if (runAwayFrom == null || runAwayFrom.position() == null) {
            System.err.println("Null unit to run from");
            stopRunning();
            throw new RuntimeException("Null unit to run from");
        }

        if (handleOnlyCombatBuildingsAreDangerouslyClose(unit)) {
            return true;
        }

//        HasPosition runAwayFrom = defineRunAwayFrom(unitOrPosition);

        // === Define run to position ==============================

        runTo = findBestPositionToRun(runAwayFrom, dist);

        // === Actual run order ====================================

        if (runTo != null && unit.distTo(runTo) >= 0.001) {
            dist = unit.distTo(runTo);
            unit.setTooltip("RunToDist(" + String.format("%.1f", dist) + ")", false);
            return makeUnitRun(action);
        }

//        System.err.println("=== RUN ERROR =================");
//        System.err.println("Unit position = " + unit.position() + " // " + unit);
//        System.err.println("runTo = " + runTo);
//        System.err.println("_lastPosition = " + _lastPosition);
//        System.err.println("Our count = " + Select.ourWithUnfinished().exclude(unit).inRadius(unit.size(), unit).count());
//        System.err.println("Neutral count = " + Select.neutral().inRadius(unit.size(), unit).count());

        unit.setTooltip("Cant run", false);
        return false;
    }

//    private HasPosition defineRunAwayFrom(Object unitOrPosition) {
//        HasPosition runAwayFrom = (HasPosition) unitOrPosition;
//
//        if (unitOrPosition instanceof AUnit) {
//            AUnit runAwayFromAsUnit = ((AUnit) unitOrPosition);
//            runAwayFrom = runAwayFromAsUnit.position();
//        }
//        else if (unitOrPosition instanceof APosition) {
//            runAwayFrom = (APosition) unitOrPosition;
//        }
//
//        // Fix against same unit position / run away from position
//        if (unit.distToLessThan(runAwayFrom, 0.05)) {
//            runAwayFrom = unit.enemiesNear().nearestTo(unit);
//        }
//
//        // Fix against same unit position / run away from position
//        if (unit.distToLessThan(runAwayFrom, 0.05)) {
//            runAwayFrom = runAwayFrom.translateByPixels(20, 20);
//        }
//
//        return runAwayFrom;
//    }

    private APosition handleRunToMainAsAFallback(AUnit unit, HasPosition runAwayFrom) {
        if (Select.main() != null && Select.main().distToMoreThan(runAwayFrom, 15)) {
            if (Select.our().inRadius(0.7, unit).exclude(unit).atMost(2)) {
                return Select.main().position();
            }
        }

        return null;
    }

//    public boolean runFromCloseEnemies() {
//
//        // Define which enemies are considered as close enough to be dangerous
//        closeEnemies = defineCloseEnemies(unit);
//        if (closeEnemies.isEmpty()) {
//            stopRunning();
//            return false;
//        }
//
//        // ===========================================
//        // Define "center of gravity" for the set of enemies
//
//        enemyMedian = closeEnemies.averageDistanceWeightedTo(unit, 0.33);
//        APainter.paintCircleFilled(enemyMedian, 10, Color.Orange);
//
//        // Run from given position
//        return runFrom(enemyMedian, -1);
//    }

    // =========================================================

    /**
     * Running behavior which will make unit run straight away from the enemy.
     */
    private APosition findBestPositionToRun(HasPosition runAwayFrom, double dist) {
        if ((runTo = shouldRunTowardsBunker()) != null) {
            return runTo;
        }

        if (shouldRunTowardsBase()) {
            return runTo = Select.main().position();
        }

        // === Run directly away from the enemy ========================================

        if (unit.friendsInRadius(1).atLeast(1) && A.notNthGameFrame(30)) {
            runTo = findRunPositionShowYourBackToEnemy(runAwayFrom, dist);
        }

        // === Get run to position - as far from enemy as possible =====================

        if (runTo == null) {
            runTo = findRunPositionInAnyDirection(runAwayFrom);
        }
        if (runTo == null) {
            fallbackMode = true;
            runTo = findRunPositionInAnyDirection(runAwayFrom);
        }

        // =============================================================================

//        System.out.println("runTo = " + runTo + " // " + unit);
        if (
            runTo != null
                && unit.distTo(runTo) <= 0.02
//                && isPossibleAndReasonablePosition(unit, runTo.position(), true)
        ) {
            // Info: This is a known issue, I couldn't debug this, but it shouldn't be a huge problem...
            System.err.println("Invalid run position, dist = " + unit.distTo(runTo));
            APainter.paintLine(unit, runTo, Color.Purple);
            APainter.paintLine(
                    unit.translateByPixels(0, 1),
                    runTo.translateByPixels(0, 1),
                    Color.Purple
            );
            runTo = findRunPositionInAnyDirection(runAwayFrom);

            if (runTo != null) {
                unit.setAction(Actions.RUN_IN_ANY_DIRECTION);
                unit.setTooltipTactical("RunAnywhere");
            }

            APainter.paintCircleFilled(runTo, 8, Color.Red);
        }

        // === Run to base as a fallback ===========================

        if (runTo == null) {
            runTo = handleRunToMainAsAFallback(unit, runAwayFrom);
        }

        // =============================================================================

        return runTo;
    }

    // =========================================================

    private APosition shouldRunTowardsBunker() {
        if (!We.terran() || !GamePhase.isEarlyGame()) {
            return null;
        }

        if (unit.isTerranInfantry() && Count.bunkers() > 0) {
            AUnit bunker = Select.ourOfType(AUnitType.Terran_Bunker).nearestTo(unit);
            if (bunker != null && bunker.distToMoreThan(unit, 5)) {
                return bunker.position();
            }
        }

        return null;
    }

    /**
     * Running behavior which will make unit run toward main base.
     */
    private boolean shouldRunTowardsBase() {
        AUnit main = Select.main();

        if (main == null) {
            return false;
        }

        if (OurStrategy.get().isRushOrCheese() && A.seconds() <= 300) {
            return false;
        }

        if (!unit.hasPathTo(main)) {
            return false;
        }

        double distToMain = unit.distTo(main);

        if (main == null) {
            return false;
        }

        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(4);
        if (distToMain >= 40 || (distToMain > 15 && meleeEnemiesNearCount == 0 && unit.isMissionDefend())) {
            return true;
        }

        if (A.seconds() >= 380) {
            return false;
        }

        if (unit.isScout()) {
            return false;
        }

        // If already close to the base, don't run towards it, no point
        if (distToMain < 50) {
            return false;
        }

        if (meleeEnemiesNearCount >= 1) {
            return false;
        }

        // Only run towards our main if our army isn't too numerous, otherwise units gonna bump upon each other
        if (Count.ourCombatUnits() > 10) {
            return false;
        }

        if (unit.lastStartedRunningLessThanAgo(30) && unit.lastStoppedRunningLessThanAgo(30)) {
            return false;
        }

        if (Count.ourCombatUnits() <= 10 || unit.isNearEnemyBuilding()) {
            if (unit.meleeEnemiesNearCount(3) == 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Simplest case: add enemy-to-you-vector to your own position.
     */
    private APosition findRunPositionShowYourBackToEnemy(HasPosition runAwayFrom, double dist) {
        APosition runTo = showBackToEnemyIfPossible(runAwayFrom);

        if (runTo != null && unit.distToMoreThan(runTo, 0.002)) {
            return runTo;
        }

        return null;
    }

    private APosition showBackToEnemyIfPossible(HasPosition runAwayFrom) {
        APosition runTo;
        runAwayFrom = runAwayFrom.position();
        double vectorLength = unit.distTo(runAwayFrom);
        double runDistInPixels = showBackRunPixelRadius(unit, runAwayFrom);

        if (vectorLength < 0.01) {
//            CameraManager.centerCameraOn(unit);
//            System.err.println("Serious issue: run vectorLength = " + vectorLength);
//            System.err.println("runner = " + unit + " // " + unit.position());
//            System.err.println("runAwayFrom = " + runAwayFrom);
//            System.err.println("unit.distTo(runAwayFrom) = " + unit.distTo(runAwayFrom));
//            A.printStackTrace();
//            GameSpeed.pauseGame();
            return null;
        }

        Vector vector = new Vector(unit.x() - runAwayFrom.x(), unit.y() - runAwayFrom.y());
        vector.normalize();
        vector.scale(runDistInPixels);

        // Apply opposite 2D vector
        runTo = unit.position().translateByVector(vector);

//        System.out.println("vector = " + vector);
//        System.out.println("unit = " + unit.position().toStringPixels() + " // " + runTo.toStringPixels() + " // " + unit.distTo(runTo));

        // === Ensure position is in bounds ========================================

        int oldX = runTo.getX();
        int oldY = runTo.getY();

        runTo = runTo.makeValidFarFromBounds();

        // If vector changed (meaning we almost reached map boundaries) disallow it
        if (runTo.getX() != oldX && runTo.getY() != oldY) {
//            throw new RuntimeException("aaa " + unit + " // " + unit.position() + " // " + runAwayFrom);
            return null;
        }

        // =========================================================

        // If run distance is acceptably long and it's connected, it's ok.
        if (isPossibleAndReasonablePosition(unit, runTo, true, "O", "X")) {
//        if (isPossibleAndReasonablePosition(unit, runTo, true, null, null)) {
//            APainter.paintLine(unit.position(), runTo, Color.Purple);
//            APainter.paintLine(unit.translateByPixels(-1, -1), runTo, Color.Purple);
            return runTo;
        }
        else {
//            System.err.println("Not possible to show back");
            return null;
        }
    }

    private double showBackRunPixelRadius(AUnit unit, HasPosition runAwayFrom) {
        if (unit.isVulture()) {
            return SHOW_BACK_DIST_VULTURE * 32;
        }
        if (unit.isDragoon()) {
            return SHOW_BACK_DIST_DRAGOON * 32;
        }

        return (SHOW_BACK_DIST_DEFAULT * 32);
    }

    /**
     * Returns a place where run to, searching in all directions, which is walkable, inbounds and most distant
     * to given runAwayFrom position.
     */
//    private APosition findPositionToRunInAnyDirection(AUnit unit, HasPosition runAwayFrom) {
//
//        // === Define run from ====================================================
////        Units unitsInRadius = Select.enemyRealUnits().melee().inRadius(4, unit).units();
////        APosition runAwayFrom = unitsInRadius.median();
//        if (runAwayFrom == null) {
//            System.err.println("Run away from is null in findRunPositionAtAnyDirection");
//            return null;
//        }
//
//        // === Define if we don't want to go towards region polygon points ========
//
////        boolean avoidCornerPoints = AMap.getDistanceToAnyRegionPolygonPoint(unit.getPosition()) > 1.5;
//
//        // ========================================================================
//
//        APosition unitPosition = unit.position();
//        int radius = runAnyDirectionInitialRadius(unit);
//        APosition bestPosition = null;
//        while (bestPosition == null && radius >= 0.3) {
//            bestPosition = findRunPositionInAnyDirection(unitPosition, runAwayFrom, radius);
//            radius -= 1;
//        }
//
//        // =========================================================
//
////        if (bestPosition != null) {
////            APainter.paintLine(unit, bestPosition, Color.Green);
////            APainter.paintLine(unit.getPosition().translateByPixels(1, 1), bestPosition.translateByPixels(1, 1), Color.Green);
////        }
//
////        AtlantisPainter.paintCircleFilled(unit.getPosition(), 7, Color.Purple);
////        AtlantisPainter.paintLine(unit.getPosition(), bestPosition, Color.Green);
////        AtlantisPainter.paintLine(unit.getPosition().translateByPixels(1, 1), bestPosition.translateByPixels(1, 1), Color.Green);
//        return bestPosition;
//    }
    private APosition findRunPositionInAnyDirection(HasPosition runAwayFrom) {
        int radius = runAnyDirectionInitialRadius(unit);

        // Build list of possible run positions, basically around the clock
        ArrayList<APosition> potentialPositionsList = new ArrayList<>();
//        APainter.paintCircleFilled(enemyMedian, 8, Color.Purple); // @PAINT EnemyMedian

        for (int dtx = -radius; dtx <= radius; dtx++) {
            for (int dty = -radius; dty <= radius; dty++) {
                if (!fallbackMode && dtx != -radius && dtx != radius && dty != -radius && dty != radius) {
                    continue;
                }

                // Create position, Make sure it's inbounds
//                APosition potentialPosition = unit.translateByTiles(dtx, dty).makeValidFarFromBounds();
                APosition potentialPosition = unit.translateByTiles(dtx, dty);

                // If has path to given point, add it to the list of potential points
//                APainter.paintLine(unitPosition, potentialPosition, Color.Purple);
//                if (isPossibleAndReasonablePosition(unit, potentialPosition, false, "v", "x")) {
                if (
                    isPossibleAndReasonablePosition(unit, potentialPosition, false, null, null)
                        && !potentialPosition.isCloseToMapBounds()
                ) {
                    potentialPositionsList.add(potentialPosition);
                }
            }
        }

//        System.out.println("potentialPositionsList = " + potentialPositionsList.size());

        // =========================================================
        // Find the location that would be most distant to the enemy location
        double mostDistant = -99;
        APosition bestPosition = null;
        for (APosition position : potentialPositionsList) {

            // Score is calculated as:
            // - being most distant to enemy we're running from,
            AUnit closestAlly = unit.friendsNear().nearestTo(unit);
            double tooCloseFriendFactor = (closestAlly == null ? 0 : closestAlly.distTo(position) / 10);
            double positionScore = runAwayFrom.distTo(position) - tooCloseFriendFactor;
            if (bestPosition == null || positionScore >= mostDistant) {
                bestPosition = position;
                mostDistant = positionScore;
            }
        }

        return bestPosition;
    }

    private int runAnyDirectionInitialRadius(AUnit unit) {
        if (unit.isVulture()) {
            return ANY_DIRECTION_RADIUS_VULTURE;
        }
        else if (unit.isDragoon()) {
            return ANY_DIRECTION_RADIUS_DRAGOON;
        }
        else if (unit.isInfantry()) {
            return ANY_DIRECTION_RADIUS_INFANTRY;
        }

        return ANY_DIRECTION_RADIUS_DEFAULT;
    }

    /**
     * Tell other units that might be blocking our escape route to move.
     */
    private boolean notifyNearUnitsToMakeSpace(AUnit unit) {
        if (We.protoss() && unit.friendsNear().inRadius(0.3, unit).atMost(1)) {
            return false;
        }

        if (unit.isAir() || unit.isLoaded()) {
            return false;
        }

//        if (unit.enemiesNear().melee().inRadius(4, unit).empty()) {
//            return false;
//        }

        Selection friendsTooClose = Select.ourRealUnits()
            .exclude(unit)
            .groundUnits()
            .inRadius(NOTIFY_UNITS_IN_RADIUS, unit);

        if (friendsTooClose.count() <= 1) {
            return false;
        }

        for (AUnit otherUnit : friendsTooClose.list()) {
            if (canBeNotifiedToMakeSpace(otherUnit)) {
                AUnit runFrom = otherUnit.enemiesNear().nearestTo(otherUnit);
                if (runFrom == null || !runFrom.hasPosition()) {
                    continue;
                }

//                System.err.println(otherUnit + " // notified by " + unit + " (" + unit.hp() + ")");

                otherUnit.runningManager().runFrom(runFrom, Near_UNIT_MAKE_SPACE, Actions.MOVE_SPACE);
                APainter.paintCircleFilled(unit, 10, Color.Yellow);
                APainter.paintCircleFilled(otherUnit, 7, Color.Grey);
                otherUnit.setTooltip("MakeSpace" + A.dist(otherUnit, unit), false);
            }
        }
        return true;
    }

    private boolean canBeNotifiedToMakeSpace(AUnit unit) {
        return !unit.isRunning() && !unit.type().isReaver() && unit.lastStartedRunningMoreThanAgo(3);
//        return !unit.isRunning() && !unit.type().isReaver();
    }

    // =========================================================

    /**
     * Returns true if given run position is traversable, land-connected and not very, very far
     */
    public boolean isPossibleAndReasonablePosition(
        AUnit unit, APosition position
    ) {
//        return isPossibleAndReasonablePosition(unit, position, true, "#", "%");
        return isPossibleAndReasonablePosition(unit, position, true, null, null);
    }

    public boolean isPossibleAndReasonablePosition(
        AUnit unit, APosition position, boolean includeNearWalkability, String charForIsOk, String charForNotOk
    ) {
        if (position == null) {
            return false;
        }

        if (unit.isAir()) {
            return true;
        }

        position = position.makeWalkable(1);

        if (position == null) {
            return false;
        }

//        System.out.println("position.isWalkable() = " + position.isWalkable());
//        System.out.println("unit.hasPathTo(position) = " + unit.hasPathTo(position));
//        System.out.println("unit.position().groundDistanceTo(position) = " + unit.position().groundDistanceTo(position));
        int walkRadius = 32;

        boolean nearbyWalkable = position.isCloseToMapBounds() || (
            position.translateByPixels(-walkRadius, -walkRadius).isWalkable()
                && position.translateByPixels(walkRadius, walkRadius).isWalkable()
                && position.translateByPixels(walkRadius, -walkRadius).isWalkable()
                && position.translateByPixels(-walkRadius, -walkRadius).isWalkable()
        );
        boolean isWalkable = position.isWalkable() && nearbyWalkable;
//                && (
//                    !includeNearWalkability


        boolean isOkay = isWalkable
//                )
//                && (!includeUnitCheck || Select.our().exclude(this.unit).inRadius(0.6, position).count() <= 0)
//                && Select.ourWithUnfinished().exclude(unit).inRadius(unit.size(), position).count() <= 0
            && Select.all().inRadius(unit.size() * 1.7, position).exclude(unit).isEmpty()
//                && distToNearestRegionBoundaryIsOkay(position)
            && unit.hasPathTo(position)
            && unit.position().groundDistanceTo(position) <= 18
//                && Select.neutral().inRadius(1.2, position).count() == 0
//                && Select.enemy().inRadius(1.2, position).count() == 0
//                && Select.ourBuildings().inRadius(1.2, position).count() == 0
            ;

        if (charForIsOk != null) {
            APainter.paintTextCentered(position, isOkay ? charForIsOk : charForNotOk, isOkay ? Color.Green : Color.Red);
        }

        return isOkay;
    }

    private boolean handleOnlyCombatBuildingsAreDangerouslyClose(AUnit unit) {
        if (unit.isRunning()) {
            return false;
        }

        // Check if only combat buildings are dangerously close. If so, don't run in any direction.
        Units dangerous = AvoidEnemies.unitsToAvoid(unit, true);

        if (dangerous.isEmpty()) {
            return false;
        }

        Selection combatBuildings = Select.from(dangerous).combatBuildings(false);
        if (dangerous.size() == combatBuildings.size() && unit.enemiesNear().combatUnits().atMost(1)) {
            if (combatBuildings.nearestTo(unit).distToMoreThan(unit, 7.9)) {
                if (unit.isMoving()) {
                    unit.holdPosition("Steady", true);
                }
                return true;
            }
        }

        return false;
    }

//    private boolean distToNearestRegionBoundaryIsOkay(APosition position) {
//        ARegion region = position.region();
//        if (region != null) {
//            return true;
//        }
//
//        for (ARegionBoundary boundary : region.boundaries()) {
//            if (boundary.distToLessThan(unit, MIN_DIST_TO_REGION_BOUNDARY)) {
//                return false;
//            }
//        }
//
//        return true;
//    }

    private boolean makeUnitRun(Action action) {
        if (unit == null) {
            return false;
        }

        if (runTo == null) {
            stopRunning();
            System.err.println("RunTo should not be null!");
            unit.setTooltip("Fuck!", false);
            return true;
        }

        // === Valid run position ==============================

        else {
            // Update last time run order was issued
            unit._lastStartedRunning = A.now();
//            if (true) throw new RuntimeException("aaa");
//            A.printStackTrace();
            unit.move(runTo, action, "Run(" + A.digit(unit.distTo(runTo)) + ")", false);

            // Make all other units very close to it run as well
            notifyNearUnitsToMakeSpace(unit);

            return true;
        }
    }

    // === Getters ========================================

    public APosition getRunToPosition() {
        return runTo;
    }

    public boolean isRunning() {
        if (runTo != null && unit.distTo(runTo) >= 0.002) {
            return true;
//            if (unit.lastStartedRunningAgo(3)) {
//                return true;
//            } else {
//                stopRunning();
//                return false;
//            }
        }

//        stopRunning();
        return false;
    }

    public void stopRunning() {
        runTo = null;
        unit._lastStoppedRunning = A.now();
    }
}
