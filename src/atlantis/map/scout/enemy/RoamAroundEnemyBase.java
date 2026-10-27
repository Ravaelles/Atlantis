package atlantis.map.scout.enemy;

import atlantis.architecture.Manager;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.game.CameraCommander;
import atlantis.map.base.define.EnemyMainBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.map.region.ARegion;
import atlantis.map.region.ARegionBoundary;
import atlantis.map.scout.ScoutState;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import bwapi.Color;

public class RoamAroundEnemyBase extends Manager {
    /**
     * Frames without progress towards the current roam point before the
     * polygon index is advanced anyway. The index used to advance only on
     * arrival (within 1.8 tiles) - a scout that can never get there (blocked
     * point, chased loop) returned the same target every frame, and
     * {@code move()} suppresses re-issues to an unchanged target, so it stood
     * still holding a move order. 300 frames (~12 s) is the same order as the
     * construction overdue window, not a tuned value.
     */
    public static final int ROAM_STUCK_TIMEOUT_FRAMES = 300;
    private HasPosition enemyMain;
    private APosition position = null;
    public RoamAroundEnemyBase(AUnit unit) {
        super(unit);
    }

    /**
     * Pure timeout decision, so the rule is unit-testable without a game.
     * Tracked ({@code lastDist >= 0}), no progress beyond 0.1 tiles since
     * {@code lastProgressFrame}, and the wait exceeded the timeout.
     */
    public static boolean isRoamStuck(double distToPoint, double lastDist, int lastProgressFrame, int now) {
        return lastDist >= 0
            && distToPoint >= lastDist - 0.1
            && now - lastProgressFrame > ROAM_STUCK_TIMEOUT_FRAMES;
    }

    @Override
    public boolean applies() {
        ScoutState.scoutingAroundBaseWasInterrupted = false;
//        APosition enemyBase = Select.main().position();

        enemyMain = EnemyMainBase.get();
        if (enemyMain == null) return false;

        position = enemyMain.position();
        if (position == null) return false;

        ScoutState.enemyBaseRegion = enemyMain.position().region();

        // The region is null when the map model has no region for the tile
        // (uninitialised BWEM, OpenBW's region-less map) - and a null region
        // NPE'd below in initializeEnemyRegionPolygonPoints, killing this
        // unit's whole manager chain for the frame (measured on Wine:
        // RoamAroundEnemyBase.java:67). No region means nowhere to roam, so
        // decline instead of throwing; the per-factory invokedFor catch is the
        // backstop, not the plan.
        if (ScoutState.enemyBaseRegion == null) return false;

        if (ScoutState.scoutingAroundBasePoints.empty()) {
            initializeEnemyRegionPolygonPoints();
        }

        return ScoutState.scoutingAroundBasePoints.notEmpty();
    }

    @Override
    public Manager handle() {
        defineNextPolygonPointForEnemyBaseRoamingUnit();

        if (
            ScoutState.unitingAroundBaseLastPolygonPoint != null
                && unit.move(ScoutState.unitingAroundBaseLastPolygonPoint, Actions.MOVE_EXPLORE, "RoamAround", true)
        ) {
            return usedManager(this);
        }
        else {
            unit.setTooltipTactical("Can't find polygon point");
        }

        return null;
    }

    private void initializeEnemyRegionPolygonPoints() {
        APosition center = (new Positions(ScoutState.enemyBaseRegion.boundaries())).average();
        if (center == null) return;

        for (ARegionBoundary position : ScoutState.enemyBaseRegion.boundaries()) {
            double groundDistance = center.distTo(position);

            if (
                position.isWalkable()
                    && groundDistance >= 2
                    && groundDistance <= 14
                    && ScoutState.enemyBaseRegion.equals(position.position().region())
            ) {
                ScoutState.scoutingAroundBasePoints.addPosition(position);
            }
        }

//        if (MAKE_CAMERA_FOLLOW_unit_AROUND_BASE) {
//            AGame.changeSpeedTo(1);
//            AGame.changeSpeedTo(1);
//            APainter.paintingMode = APainter.MODE_FULL_PAINTING;
//        }
    }

    private void defineNextPolygonPointForEnemyBaseRoamingUnit() {
        ScoutState.unitingAroundBaseLastPolygonPoint = nextPointAroundEnemyBase();

        if (APainter.paintingMode == APainter.MODE_FULL_PAINTING) {
            APainter.paintLine(
                ScoutState.unitingAroundBaseLastPolygonPoint, unit.position(), Color.Yellow
            );
        }

        if (ScoutState.MAKE_CAMERA_FOLLOW_unit_AROUND_BASE) {
            CameraCommander.centerCameraOn(unit.position());
        }
    }

    private HasPosition nextPointAroundEnemyBase() {

        // Change roaming direction if we were forced to run from enemy units
        if (ScoutState.scoutingAroundBaseWasInterrupted) {
            ScoutState.unitingAroundBaseDirectionClockwise = !ScoutState.unitingAroundBaseDirectionClockwise;
        }

        // Progress tracking: re-arm on a new point, record approaches, and
        // force an advance when the scout makes no progress towards the
        // current point within the timeout (blocked point, chase loop).
        boolean stuckOnCurrent = false;
        HasPosition current = ScoutState.unitingAroundBaseLastPolygonPoint;
        if (current != null && !ScoutState.scoutingAroundBaseWasInterrupted) {
            double dist = unit.distTo(current);
            if (ScoutState.roamLastDistToPoint < 0) {
                ScoutState.roamLastDistToPoint = dist;
                ScoutState.roamLastProgressFrame = A.now();
            }
            else if (dist < ScoutState.roamLastDistToPoint - 0.1) {
                ScoutState.roamLastDistToPoint = dist;
                ScoutState.roamLastProgressFrame = A.now();
            }
            else {
                stuckOnCurrent = isRoamStuck(
                    dist, ScoutState.roamLastDistToPoint, ScoutState.roamLastProgressFrame, A.now()
                );
            }
        }

        // Define direction
        HasPosition goTo = null;
        int step = 1;
        do {
            int deltaIndex = ScoutState.unitingAroundBaseDirectionClockwise ? step : -step;

            goTo = ScoutState.unitingAroundBaseLastPolygonPoint != null ? APosition.create(ScoutState.unitingAroundBaseLastPolygonPoint) : null;

            if (goTo == null || ScoutState.scoutingAroundBaseWasInterrupted) {
                goTo = useNearestBoundaryPoint(ScoutState.enemyBaseRegion);
                ScoutState.roamLastDistToPoint = -1;
            }
            else {
                if (unit.distTo(goTo) <= 1.8 || stuckOnCurrent) {
                    ScoutState.unitingAroundBaseNextPolygonIndex = (ScoutState.unitingAroundBaseNextPolygonIndex + deltaIndex)
                        % ScoutState.scoutingAroundBasePoints.size();
                    if (ScoutState.unitingAroundBaseNextPolygonIndex < 0) {
                        ScoutState.unitingAroundBaseNextPolygonIndex = ScoutState.scoutingAroundBasePoints.size() - 1;
                    }

                    goTo = ScoutState.scoutingAroundBasePoints.get(ScoutState.unitingAroundBaseNextPolygonIndex);

                    if (Select.all().inRadius(0.5, goTo).empty()) {
                        ScoutState.roamLastDistToPoint = -1;
                        break;
                    }
                }
            }

            step++;
        } while (step <= 15);

        ScoutState.scoutingAroundBaseWasInterrupted = false;

        return ScoutState.unitingAroundBaseLastPolygonPoint = goTo;
    }

    private ARegionBoundary useNearestBoundaryPoint(ARegion region) {
        ARegionBoundary nearest = ScoutState.scoutingAroundBasePoints.nearestTo(unit.position());
        ScoutState.unitingAroundBaseNextPolygonIndex = ScoutState.scoutingAroundBasePoints.getLastIndexForScout();

        return nearest;
    }
}
