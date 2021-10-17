package atlantis.combat.missions;

import atlantis.combat.micro.managers.AdvanceUnitsManager;
import atlantis.combat.squad.ASquadCohesionManager;
import atlantis.combat.squad.SquadScout;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;

public class MissionContain extends Mission {

    protected MissionContain() {
        super("Contain");
        focusPointManager = new MissionContainFocusPointManager();
    }

    @Override
    public boolean update(AUnit unit) {
        unit.setTooltip("#Contain");
        APosition focusPoint = focusPoint();

        // =========================================================

//        if (handleUnitSafety(unit, true, true)) {
//            return true;
//        }

        if (SquadScout.handle(unit)) {
            return true;
        }

        if (ASquadCohesionManager.handle(unit)) {
            return true;
        }

        // Focus point is well known
        if (focusPoint != null && AdvanceUnitsManager.moveToFocusPoint(unit, focusPoint)) {
            return true;
        }

        // =========================================================

        return false;
    }

    @Override
    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        APosition focusPoint = focusPoint();

        if (enemy.distanceTo(unit) <= 6.1 || unit.inWeaponRange(enemy, 0.8)) {
            return true;
        }

        // Only attack enemies near squad center
        if (enemy.distanceTo(focusPoint) <= 13) {
            return true;
        }

        // Allow to defend base
        APosition natural = AMap.getNaturalBaseLocation();
        if (natural != null && enemy.distanceTo(natural) <= 35) {
            return true;
        }

        return false;
    }
}