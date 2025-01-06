package atlantis.map.scout.enemy;

import atlantis.architecture.Manager;
import atlantis.debug.painter.APainter;
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
    private HasPosition enemyMain;
    private APosition position = null;

    public RoamAroundEnemyBase(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        ScoutState.scoutingAroundBaseWasInterrupted = false;
//        APosition enemyBase = Select.main().position();

        enemyMain = EnemyMainBase.get();
        if (enemyMain == null) return false;

        position = enemyMain.position();
        if (position == null) return false;

        if (position.position().x >= 3200) return false;

        ScoutState.enemyBaseRegion = enemyMain.position().region();
        if (enemyMain == null) return false;

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
        ScoutState.unitingAroundBaseNextPolygonIndex = ScoutState.scoutingAroundBasePoints.getLastIndexForScout();
        return nearest;
    }
}
