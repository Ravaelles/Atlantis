package atlantis.combat.missions.contain;

import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.squad.ASquadCohesionManager;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

public class MissionContain extends Mission {

    public MissionContain() {
        super("Contain");
        focusPointManager = new MissionContainFocusPoint();
    }

    @Override
    public boolean update(AUnit unit) {
        AFocusPoint focusPoint = focusPoint();
        unit.setTooltipTactical("#Contain(" + (focusPoint != null ? A.digit(focusPoint.distTo(unit)) : null) + ")");

        // =========================================================

        if (focusPoint == null) {
            MissionChanger.forceMissionAttack();
            return false;
        }

        // =========================================================

//        if (handleUnitSafety(unit, true, true)) {
//            return true;
//        }

//        if (SquadScout.handle(unit)) {
//            return true;
//        }

        if (ASquadCohesionManager.handle(unit)) {
            return true;
        }

        // Focus point is well known
        return MoveToContainFocusPoint.move(unit, focusPoint);

        // =========================================================
    }

    @Override
    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        AFocusPoint focusPoint = focusPoint();

        if (enemy.hasWeaponRangeToAttack(unit, 0.2)) {
            return true;
        }

        if (wouldCrossChokeToAttack(unit, enemy, focusPoint)) {
            return false;
        }

        // Allow to defend bases
        if (enemyIsNearAnyOurBuilding(enemy)) {
            return true;
        }

        if (unit.isStimmed()) {
            return true;
        }

        // Attack enemies near squad center
        if (enemy.distTo(unit.squad().center()) <= 12) {
            return true;
        }

        return false;
    }


    private boolean wouldCrossChokeToAttack(AUnit unit, AUnit enemy, AFocusPoint focusPoint) {
        if (focusPoint != null && focusPoint.isAroundChoke()) {
            HasPosition squad = unit.squadCenter();
            double squadToEnemy = squad.distTo(enemy);
            double squadToFocus = squad.distTo(focusPoint);

            if (squadToEnemy > 0.8 + squadToFocus) {
                return false;
            }
        }

        return false;
    }

}