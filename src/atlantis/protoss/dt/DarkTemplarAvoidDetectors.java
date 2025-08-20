package atlantis.protoss.dt;

import atlantis.architecture.Manager;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class DarkTemplarAvoidDetectors extends Manager {
    private AUnit detector;

    public DarkTemplarAvoidDetectors(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        detector = detector();
        if (detector == null) return false;

        if (detector.isCannon()) return true;

        if (unit.hp() >= 60 && unit.groundDistToMain() <= 50) return false;

        if (shouldIgnoreBecauseHealthyAndNoRangedNear()) return false;

        return detector != null;
    }

    private boolean shouldIgnoreBecauseHealthyAndNoRangedNear() {
        return unit.shields() >= 18
            && unit.enemiesNear().ranged().canAttack(unit, 1.3 + unit.woundPercent() / 30.0).empty();
    }

    @Override
    public Manager handle() {
        if (detector.isABuilding() || unit.hp() >= 90) {
            if (unit.moveToSafety(Actions.MOVE_AVOID)) {
                return usedManager(this, "DTDetector2Safety");
            }
        }

        int moveDistance = detector.isABuilding() ? 2 : 6;
        if (unit.moveAwayFrom(detector, moveDistance, Actions.MOVE_AVOID, "DTDetectorAway")) {
            return usedManager(this);
        }

        return null;
    }

    private AUnit detector() {
        Selection detectors;
//        if (unit.shields() >= 30) detectors = unit.enemiesNear().detectors().onlyCompleted().visibleOnMap();
//        else detectors = EnemyUnits.discovered().detectors().inRadius(16, unit);

        detectors = EnemyUnits.discovered().detectors().inRadius(16, unit);

        if (detectors.empty()) return null;

        double radius = Enemy.protoss() ? 11.3 : 11.3;

        if (detectors.nonBuildings().notEmpty()) radius += 1.2;

        return detectors.inRadius(radius + unit.woundPercent() / 15.0, unit).nearestTo(unit);
    }
}
