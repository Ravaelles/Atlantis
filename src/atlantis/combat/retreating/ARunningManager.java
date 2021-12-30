package atlantis.combat.retreating;

import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.debug.APainter;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.actions.UnitActions;
import atlantis.util.A;
import bwapi.Color;

import java.util.ArrayList;


public class ARunningManager {

    //    public static double MIN_DIST_TO_REGION_BOUNDARY = 1;
    public static int STOP_RUNNING_IF_STOPPED_MORE_THAN_AGO = 8;
    public static int STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO = 2;
    public static double NEARBY_UNIT_MAKE_SPACE = 0.75;
    private static final double SHOW_BACK_TO_ENEMY_DIST_MIN = 2;
    private static final double SHOW_BACK_TO_ENEMY_DIST = 3;
    public static int ANY_DIRECTION_INIT_RADIUS_INFANTRY = 3;
    public static double NOTIFY_UNITS_IN_RADIUS = 0.80;

    private final AUnit unit;
    private static APosition _lastPosition;
//    private APosition runAwayFrom = null;
    private APosition runTo;
//    private Units closeEnemies;
//    private APosition enemyMedian = null;

    // =========================================================
    
    public ARunningManager(AUnit unit) {
        this.unit = unit;
    }

    // =========================================================

    public static boolean shouldStopRunning(AUnit unit) {
//        System.out.println(unit.id() + " // " + unit.isRunning()
//                + " // " + AAvoidUnits.shouldNotAvoidAnyUnit(unit));
        if (
                unit.isRunning()
                && unit.lastStoppedRunningMoreThanAgo(STOP_RUNNING_IF_STOPPED_MORE_THAN_AGO)
                && unit.lastStartedRunningMoreThanAgo(STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO)
                && !unit.isUnderAttack(unit.isAir() ? 250 : 5)
                && AAvoidUnits.shouldNotAvoidAnyUnit(unit)
        ) {
            unit.runningManager().stopRunning();
            unit.setTooltip("StopRun");
            return true;
        }

        return false;
    }

//    public boolean runFromHere() {
//        return runFrom(null, -1);
//    }

    public boolean runFrom(Object unitOrPosition, double dist) {
        if (unitOrPosition == null) {
            System.err.println("Null unit to run from");
            stopRunning();
            throw new RuntimeException("Null unit to run from");
        }

        if (handleOnlyCombatBuildingsAreDangerouslyClose(unit)) {
            return true;
        }

        HasPosition runAwayFrom = defineRunAwayFrom(unitOrPosition);

        // === Define run to position ==============================

        runTo = findBestPositionToRun(unit, runAwayFrom, dist);

        // === Actual run order ====================================

        if (runTo != null && unit.distTo(runTo) >= 0.001) {
            dist = unit.distTo(runTo);
            unit.setTooltip("StartRun(" + String.format("%.1f", dist) + ")");
            return makeUnitRun();
        }

//        System.err.println("=== RUN ERROR =================");
//        System.err.println("Unit position = " + unit.position() + " // " + unit);
//        System.err.println("runTo = " + runTo);
//        System.err.println("_lastPosition = " + _lastPosition);
//        System.err.println("Our count = " + Select.ourIncludingUnfinished().exclude(unit).inRadius(unit.size(), unit).count());
//        System.err.println("Neutral count = " + Select.neutral().inRadius(unit.size(), unit).count());
//        System.err.println();
//                && unit.position().groundDistanceTo(position) <= 18

        unit.setTooltip("Cant run");
        return false;
    }

    private HasPosition defineRunAwayFrom(Object unitOrPosition) {
        HasPosition runAwayFrom = (HasPosition) unitOrPosition;

        if (unitOrPosition instanceof AUnit) {
            AUnit runAwayFromAsUnit = ((AUnit) unitOrPosition);
            runAwayFrom = runAwayFromAsUnit.position();
        }
        else if (unitOrPosition instanceof APosition) {
            runAwayFrom = (APosition) unitOrPosition;
        }

        // Fix against same unit position / run away from position
        if (unit.distToLessThan(runAwayFrom, 0.05)) {
//            runAwayFrom = runAwayFrom.translateByPixels(20, 20);
            runAwayFrom = unit.enemiesNearby().nearestTo(unit);
        }

        return runAwayFrom;
    }

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
    private APosition findBestPositionToRun(AUnit unit, HasPosition runAwayFrom, double dist) {

        // === Run directly away from the enemy ========================================

        APosition runTo = findRunPositionShowYourBackToEnemy(runAwayFrom, dist);

        // === Get run to position - as far from enemy as possible =====================

        if (runTo == null) {
            runTo = findRunPositionInAnyDirection(runAwayFrom);
        }

        // =============================================================================

//        System.out.println("runTo = " + runTo + " // " + unit);
        if (
                runTo != null
                && unit.distTo(runTo) < 0.002
//                && isPossibleAndReasonablePosition(unit, runTo.position(), true)
        ) {
//            System.err.println("Invalid run position, dist = " + unit.distTo(runTo));
//            APainter.paintLine(unit, runTo, Color.Purple);
//            APainter.paintLine(
//                    unit.translateByPixels(0, 1),
//                    runTo.translateByPixels(0, 1),
//                    Color.Purple
//            );
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
    /**
     * Running behavior which will make unit run toward main base.
     */
//    private boolean shouldRunTowardsMainBase(AUnit unit, APosition runAwayFrom) {
//        AUnit mainBase = Select.mainBase();
//        if (mainBase != null) {
//            if (PositionUtil.distanceTo(mainBase, unit) > 30) {
//                return true;
////                return mainBase.translated(0, 3 * 64);
//            }
//        }
//
//        return false;
////        return findPositionToRun_preferAwayFromEnemy(unit, runAwayFrom);
//    }

    /**
     * Simplest case: add enemy-to-you-vector to your own position.
     */
    private APosition findRunPositionShowYourBackToEnemy(HasPosition runAwayFrom, double dist) {
        double minDist = SHOW_BACK_TO_ENEMY_DIST_MIN;
        double maxDist = (dist > 0 ? dist : SHOW_BACK_TO_ENEMY_DIST);

        if (unit.isVulture()) {
            minDist = maxDist;
        }

        double currentDist = maxDist;

        do {
            APosition runTo = showBackToEnemyIfPossible(runAwayFrom);

            if (runTo != null && unit.distToMoreThan(runTo, 0.002)) {
                return runTo;
            }

            currentDist -= 0.9;
        } while (currentDist >= minDist);

        return null;
    }

    private APosition showBackToEnemyIfPossible(HasPosition runAwayFrom) {
        APosition runTo;
        runAwayFrom = runAwayFrom.position();
        double vectorLength = unit.distTo(runAwayFrom);

        if (vectorLength < 0.01) {
            System.err.println("Serious issue: run vectorLength = " + vectorLength);
            System.err.println("runner = " + unit + " // " + unit.position());
            System.err.println("runAwayFrom = " + runAwayFrom);
            System.err.println("unit.distTo(runAwayFrom) = " + unit.distTo(runAwayFrom));
            A.printStackTrace();
        }

        double vectorTx = (unit.x() - runAwayFrom.x()) / 32.0;
        double vectorTy = (unit.y() - runAwayFrom.y()) / 32.0;

        // Apply opposite 2D vector
        runTo = unit.position().translateByTiles(vectorTx, vectorTy);

//        System.out.println("vectorTx = " + vectorTx);
//        System.out.println("vectorTy = " + vectorTy);
//        System.out.println("runTo = " + runTo + " // " + unit.position());

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
        } else {
            return null;
        }
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
                if (dtx != -radius && dtx != radius && dty != -radius && dty != radius) {
                    continue;
                }

                // Create position, Make sure it's inbounds
                APosition potentialPosition = unit.translateByTiles(dtx, dty).makeValidFarFromBounds();

                // If has path to given point, add it to the list of potential points
//                APainter.paintLine(unitPosition, potentialPosition, Color.Purple);
//                if (isPossibleAndReasonablePosition(unit, potentialPosition, false, "v", "x")) {
                if (isPossibleAndReasonablePosition(unit, potentialPosition, false, null, null)) {
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
            double dist = runAwayFrom.distTo(position);
            if (bestPosition == null || dist >= mostDistant) {
                bestPosition = position;
                mostDistant = dist;
            }
        }

        return bestPosition;
    }

    private int runAnyDirectionInitialRadius(AUnit unit) {
        if (unit.isVulture()){
            return 4;
        }

        if (unit.isInfantry()) {
            return ANY_DIRECTION_INIT_RADIUS_INFANTRY;
        }

        return 4;
    }

    /**
     * Tell other units that might be blocking our escape route to move.
     */
    private boolean notifyNearbyUnitsToMakeSpace(AUnit unit) {
        if (unit.isAir() || unit.isLoaded()) {
            return false;
        }

        if (unit.enemiesNearby().melee().inRadius(4, unit).empty()) {
            return false;
        }

        Selection friendsTooClose = Select.ourRealUnits()
                .exclude(unit)
                .groundUnits()
                .inRadius(NOTIFY_UNITS_IN_RADIUS, unit);

        if (friendsTooClose.count() <= 1) {
            return false;
        }

        for (AUnit otherUnit : friendsTooClose.list()) {
            if (canBeNotifiedToMakeSpace(otherUnit)) {
                AUnit runFrom = Select.enemyCombatUnits().inRadius(10, unit).nearestTo(otherUnit);
                if (runFrom == null) {
                    continue;
                }

                otherUnit.runningManager().runFrom(runFrom, NEARBY_UNIT_MAKE_SPACE);
                APainter.paintCircleFilled(unit, 10, Color.Yellow);
                APainter.paintCircleFilled(otherUnit, 7, Color.Grey);
                otherUnit.setTooltip("MakeSpace" + A.dist(otherUnit, unit));
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
            AUnit unit, APosition position, boolean includeNearbyWalkability, String charForIsOk, String charForNotOk
    ) {
        if (unit.isAir()) {
            return true;
        }

        _lastPosition = position;

//        System.out.println("position.isWalkable() = " + position.isWalkable());
//        System.out.println("unit.hasPathTo(position) = " + unit.hasPathTo(position));
//        System.out.println("unit.position().groundDistanceTo(position) = " + unit.position().groundDistanceTo(position));
        boolean isOkay = position.isWalkable()
//                && (
//                    !includeNearbyWalkability
//                    || (
//                        position.translateByPixels(-48, -48).isWalkable()
//                        && position.translateByPixels(48, 48).isWalkable()
//                        && position.translateByPixels(48, -48).isWalkable()
//                        && position.translateByPixels(-48, -48).isWalkable()
//                    )
//                )
//                && (!includeUnitCheck || Select.our().exclude(this.unit).inRadius(0.6, position).count() <= 0)
//                && Select.ourIncludingUnfinished().exclude(unit).inRadius(unit.size(), position).count() <= 0
                && Select.all().inRadius(unit.size() * 2, position).exclude(unit).isEmpty()
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
        Units dangerous = AAvoidUnits.unitsToAvoid(unit, true);

        if (dangerous.isEmpty()) {
            return false;
        }

        Selection combatBuildings = Select.from(dangerous).combatBuildings(false);
        if (dangerous.size() == combatBuildings.size() && unit.enemiesNearby().combatUnits().atMost(1)) {
            if (combatBuildings.nearestTo(unit).distToMoreThan(unit, 7.9)) {
                unit.holdPosition("Steady");
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

    private boolean makeUnitRun() {
        if (unit == null) {
            return false;
        }

        if (runTo == null) {
            stopRunning();
            System.err.println("RunTo should not be null!");
            unit.setTooltip("Fuck!");
            return true;
        }

        // === Valid run position ==============================

        else {
            // Update last time run order was issued
            unit._lastStartedRunning = A.now();
            unit.move(runTo, UnitActions.RUN, "Run(" + A.digit(unit.distTo(runTo)) + ")");

            // Make all other units very close to it run as well
            notifyNearbyUnitsToMakeSpace(unit);

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

        stopRunning();
        return false;
    }

    public void stopRunning() {
        runTo = null;
        unit._lastStoppedRunning = A.now();
    }
}
