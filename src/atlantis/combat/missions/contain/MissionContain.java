package atlantis.combat.missions.contain;

import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.squad.ASquadCohesionManager;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.Units;

public class MissionContain extends Mission {

    public MissionContain() {
        super("Contain");
        focusPointManager = new MissionContainFocusPoint();
    }

    // =========================================================

    @Override
    public boolean update(AUnit unit) {
        AFocusPoint focusPoint = focusPoint();
        unit.setTooltipTactical("#Contain(" + (focusPoint != null ? A.digit(focusPoint.distTo(unit)) : null) + ")");

        if (focusPoint == null) {
            MissionChanger.forceMissionAttack("InvalidFocusPoint");
            return false;
        }

//        if (handleUnitSafety(unit, true, true)) {
//            return true;
//        }

//        if (SquadScout.handle(unit)) {
//            return true;
//        }

        if (ASquadCohesionManager.handle(unit)) {
            return true;
        }

        return (new MoveToContainFocusPoint()).move(unit, focusPoint);

    }

    // =========================================================

    @Override
    public double optimalDist(AUnit unit) {
        return (new MoveToContainFocusPoint()).optimalDist(unit);
    }

    @Override
    public boolean forcesUnitToFight(AUnit unit, Units enemies) {
        if (unit.isRanged() && unit.hp() >= 34 && unit.lastUnderAttackMoreThanAgo(30 * 3)) {
            if (
                unit.lastStartedAttackMoreThanAgo(50)
                    && unit.enemiesNear().inRadius(13, unit).onlyMelee()
            ) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        AFocusPoint focusPoint = focusPoint();

        if (enemy.hasWeaponRangeToAttack(unit, unit.isMelee() ? 0.3 : 1.3)) {
            return true;
        }

        if (unit.lastUnderAttackMoreThanAgo(30 * 5) && unit.combatEvalRelative() >= 3) {
            return true;
        }

        if (enemy.friendsNear().combatBuildingsAntiLand().inRadius(7.6, unit).atLeast(1)) {
            if (enemy.combatEvalRelative() >= 0.5) {
                return false;
            }
        }

        if (!enemy.distToNearestChokeLessThan(5)) {
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
        if (
            unit.friendsNear().inRadius(3, unit).atLeast(4)
                || enemy.distTo(unit.squad().center()) <= 12
        ) {
            return true;
        }

        return false;
    }

    // =========================================================

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