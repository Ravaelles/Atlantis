package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.debug.painter.APainter;
import atlantis.game.CameraCommander;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.map.region.ARegion;
import atlantis.map.region.ARegionBoundary;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import bwapi.Color;

public class RoamAroundEnemyBase extends Manager {
    public RoamAroundEnemyBase(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return ScoutState.scoutsKilledCount == 0;
    }

    @Override
    public Manager handle() {
        ScoutState.scoutingAroundBaseWasInterrupted = false;

        // === Remain at the enemy base if it's known ==============

        AUnit enemyBase = EnemyUnits.enemyBase();
        APosition enemy = null;

        if (enemyBase != null) {
            enemy = enemyBase.position();
        }

        if (enemy == null) {
            AUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
            if (enemyBuilding != null) {
                enemy = enemyBuilding.position();
            }
        }
//        APosition enemyBase = Select.main().position();

        if (enemy != null) {
            if (enemy.position().x >= 3200) return null;

            ScoutState.enemyBaseRegion = enemy.region();

            if (ScoutState.scoutingAroundBasePoints.isEmpty()) {
                initializeEnemyRegionPolygonPoints();
            }

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
        }

        return null;
    }

    private void initializeEnemyRegionPolygonPoints() {
        APosition center = (new Positions(ScoutState.enemyBaseRegion.boundaries())).average();

        for (ARegionBoundary position : ScoutState.enemyBaseRegion.boundaries()) {
            double groundDistance = center.distTo(position);

            if (
                position.isWalkable()
                    && groundDistance >= 2
                    && groundDistance <= 17
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

        // Define direction
        HasPosition goTo = null;
        int step = 1;
        do {
            int deltaIndex = ScoutState.unitingAroundBaseDirectionClockwise ? step : -step;

            goTo = ScoutState.unitingAroundBaseLastPolygonPoint != null ? APosition.create(ScoutState.unitingAroundBaseLastPolygonPoint) : null;

            if (goTo == null || ScoutState.scoutingAroundBaseWasInterrupted) {
                goTo = useNearestBoundaryPoint(ScoutState.enemyBaseRegion);
            }
            else {
                if (unit.distTo(goTo) <= 1.8) {
                    ScoutState.unitingAroundBaseNextPolygonIndex = (ScoutState.unitingAroundBaseNextPolygonIndex + deltaIndex)
                        % ScoutState.scoutingAroundBasePoints.size();
                    if (ScoutState.unitingAroundBaseNextPolygonIndex < 0) {
                        ScoutState.unitingAroundBaseNextPolygonIndex = ScoutState.scoutingAroundBasePoints.size() - 1;
                    }

                    goTo = ScoutState.scoutingAroundBasePoints.get(ScoutState.unitingAroundBaseNextPolygonIndex);

                    if (Select.all().inRadius(0.5, goTo).empty()) {
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
        ScoutState.unitingAroundBaseNextPolygonIndex = ScoutState.scoutingAroundBasePoints.getLastIndex();
        return nearest;
    }
}
