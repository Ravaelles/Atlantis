package atlantis.combat.missions.contain;

import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.squad.ASquadCohesionManager;
import atlantis.combat.squad.SquadScout;
import atlantis.units.AUnit;
import atlantis.util.A;

public class MissionContain extends Mission {

    public MissionContain() {
        super("Contain");
        focusPointManager = new MissionContainFocusPoint();
    }

    @Override
    public boolean update(AUnit unit) {
        AFocusPoint focusPoint = focusPoint();
        unit.setTooltip("#Contain(" + (focusPoint != null ? A.digit(focusPoint.distTo(unit)) : null) + ")");

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

        if (enemy.hasWeaponRangeToAttack(unit, 1.1) || unit.hasWeaponRangeToAttack(enemy, 1.1)) {
//        if (enemy.distTo(unit) <= 6.1 || unit.hasWeaponRange(enemy, 0.8)) {
            return true;
        }

        // Attack enemies near squad center
        if (enemy.distTo(unit.squad().median()) <= 6) {
            return true;
        }

        // Allow to defend bases
        if (enemyIsNearBase(enemy)) {
            return true;
        }

        if (unit.distTo(enemy) > unit.distTo(focusPoint) + 1.2) {
            return false;
        }

        if (unit.isStimmed()) {
            return true;
        }

        if (!unit.isWounded() || unit.lastStartedAttackMoreThanAgo(30 * 5)) {
            return true;
        }

        return false;
    }

}