package atlantis.combat.running;

import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
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
import atlantis.util.We;
import bwapi.Color;

public class ARunningManager {

    //    public static double MIN_DIST_TO_REGION_BOUNDARY = 1;
    public static int STOP_RUNNING_IF_STOPPED_MORE_THAN_AGO = 8;
    public static int STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO = 6;
    public static double Near_UNIT_MAKE_SPACE = 0.75;
    //    private static final double SHOW_BACK_TO_ENEMY_DIST_MIN = 2;
    protected static final double SHOW_BACK_DIST_DEFAULT = 6;
    protected static final double SHOW_BACK_DIST_DRAGOON = 3;
    protected static final double SHOW_BACK_DIST_TERRAN_INFANTRY = 6;
    protected static final double SHOW_BACK_DIST_VULTURE = 5;
    public static int ANY_DIRECTION_RADIUS_DEFAULT = 3;
    public static int ANY_DIRECTION_RADIUS_DRAGOON = 4;
    public static int ANY_DIRECTION_RADIUS_TERRAN_INFANTRY = 3;
    public static int ANY_DIRECTION_RADIUS_VULTURE = 4;
//    public static double NOTIFY_UNITS_IN_RADIUS = 0.65;
    public static double NOTIFY_UNITS_IN_RADIUS = 0.2;

    private final AUnit unit;
    private HasPosition runTo = null;
    private boolean allowedToNotifyNearUnitsToMakeSpace;
    private boolean fallbackMode = false; // When nothing else seems to work
    private final RunToPositionFinder runPositionFinder = new RunToPositionFinder(this);

    // =========================================================

    public ARunningManager(AUnit unit) {
        this.unit = unit;
    }

    // =========================================================

    public boolean runFromAndNotifyOthersToMove(HasPosition runFrom) {
        return runFrom(runFrom, 1.0, Actions.RUN_ENEMY, true);
    }

//    public boolean runFromHere() {
//        return runFrom(null, -1);
//    }

    //    public boolean runFrom(Object unitOrPosition, double dist) {
    public boolean runFrom(HasPosition runAwayFrom, double dist, Action action, boolean allowedToNotifyNearUnitsToMakeSpace) {
        this.allowedToNotifyNearUnitsToMakeSpace = allowedToNotifyNearUnitsToMakeSpace;

        if (runAwayFrom == null || runAwayFrom.position() == null) {
            System.err.println("Null unit to run from");
            stopRunning();
            throw new RuntimeException("Null unit to run from");
        }

        if (handleOnlyCombatBuildingsAreDangerouslyClose(unit)) {
            return true;
        }

        if (unit.isRunning()) {
            if (unit.lastStoppedRunningLessThanAgo(6)) {
                return true;
            }
            if (unit.lastActionLessThanAgo(20, Actions.RUN_IN_ANY_DIRECTION)) {
                return true;
            }
        }

        // === Define run to position ==============================

        runTo = runPositionFinder.findBestPositionToRun(runAwayFrom, dist);

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
    protected HasPosition findBestPositionToRun(HasPosition runAwayFrom, double dist) {

        // === Run directly away from the enemy ========================

        // === Run as far from enemy as possible =====================

        return runPositionFinder.findBestPositionToRun(runAwayFrom, dist);
    }

    protected HasPosition runInAnyDirection(HasPosition runAwayFrom) {

        //        if (runTo == null) {
//            fallbackMode = true;
//            runTo = findRunPositionInAnyDirection(runAwayFrom);
//        }

        // =============================================================================

//        System.out.println("runTo = " + runTo + " // " + unit);

        // === Run to base as a fallback ===========================

//        if (runTo == null) {
//            runTo = handleRunToMainAsAFallback(unit, runAwayFrom);
//        }

        // =============================================================================

//        System.err.println("Invalid run_any_dir NULL");
        return runPositionFinder.runInAnyDirection(runAwayFrom);
    }

    protected boolean shouldRunByShowingBackToEnemy() {
//        return true;
        return unit.isAir() || (A.notNthGameFrame(30) && unit.friendsInRadius(1.2).isEmpty());
    }

    // =========================================================

    protected HasPosition shouldRunTowardsBunker() {
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
    protected boolean shouldRunTowardsBase() {
        if (unit.isAir()) {
            return false;
        }

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
    private HasPosition findRunPositionShowYourBackToEnemy(HasPosition runAwayFrom) {

        return runPositionFinder.findRunPositionShowYourBackToEnemy(runAwayFrom);
    }

    private HasPosition showBackToEnemyIfPossible(HasPosition runAwayFrom) {

        // Apply opposite 2D vector

        // === Ensure position is in bounds ========================================

        // If vector changed (meaning we almost reached map boundaries) disallow it

        // =========================================================

        // If run distance is acceptably long and it's connected, it's ok.
        return runPositionFinder.showBackToEnemyIfPossible(runAwayFrom);
    }

    private double showBackRunPixelRadius(AUnit unit, HasPosition runAwayFrom) {

        return runPositionFinder.showBackRunPixelRadius(unit, runAwayFrom);
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

        // Build list of possible run positions, basically around the clock
        //        APainter.paintCircleFilled(enemyMedian, 8, Color.Purple); // @PAINT EnemyMedian

        //        System.out.println("potentialPositionsList = " + potentialPositionsList.size());

        // =========================================================
        // Find the location that would be most distant to the enemy location

        return runPositionFinder.findRunPositionInAnyDirection(runAwayFrom);
    }

    private int runAnyDirectionInitialRadius(AUnit unit) {

        return runPositionFinder.runAnyDirectionInitialRadius(unit);
    }

    /**
     * Tell other units that might be blocking our escape route to move.
     */
    private boolean notifyNearUnitsToMakeSpace(AUnit unit) {
        if (!allowedToNotifyNearUnitsToMakeSpace) {
            return false;
        }

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

                otherUnit.runningManager().runFrom(runFrom, Near_UNIT_MAKE_SPACE, Actions.MOVE_SPACE, true);
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
        return runPositionFinder.isPossibleAndReasonablePosition(unit, position);
    }

    public boolean isPossibleAndReasonablePosition(
        AUnit unit, APosition position, boolean includeNearWalkability, String charForIsOk, String charForNotOk
    ) {

        //        System.out.println("position.isWalkable() = " + position.isWalkable());
//        System.out.println("unit.hasPathTo(position) = " + unit.hasPathTo(position));
//        System.out.println("unit.position().groundDistanceTo(position) = " + unit.position().groundDistanceTo(position));

        //                && (
//                    !includeNearWalkability


        return runPositionFinder.isPossibleAndReasonablePosition(unit, position, includeNearWalkability, charForIsOk, charForNotOk);
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
            double minDist = unit.isGhost() ? 9.5 : 7.5;
            AUnit combatBuilding = combatBuildings.nearestTo(unit);
//            if (combatBuilding.distToMoreThan(unit, minDist)) {
            if (combatBuilding.distToLessThan(unit, minDist)) {
                if (unit.isHoldingPosition() && unit.lastActionMoreThanAgo(30)) {
                    if (unit.moveAwayFrom(combatBuilding, 0.3, "Careful", Actions.RUN_ENEMY)) {
                        return true;
                    }
                }
                else if (unit.isMoving() && unit.isAction(Actions.RUN_ENEMY)) {
                    unit.holdPosition("Steady", true);
                    return true;
                }
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

    public HasPosition runToPosition() {
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

    public AUnit unit() {
        return unit;
    }

    public HasPosition runTo() {
        return runTo;
    }

    protected HasPosition setRunTo(HasPosition runTo) {
        this.runTo = runTo;
        return runTo;
    }
}
