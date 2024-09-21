package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class DetectorAvoidAntiAir extends Manager {

    private HasPosition enemyAAPosition;

    public DetectorAvoidAntiAir(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!Enemy.zerg() && unit.shieldHealthy()) return false;

        enemyAAPosition = enemyAntiAirInRange(unit);
        if (enemyAAPosition == null) return false;

        return true;
    }

    public Manager handle() {
        if (unit.moveAwayFrom(enemyAAPosition, 5, Actions.MOVE_FORMATION, "ObserverAvoidAA")) return usedManager(this);

        return null;
    }

    private HasPosition enemyAntiAirInRange(AUnit unit) {
//        return Select.enemy().havingAntiAirWeapon().inRadius(6.0 + unit.shieldWound() / 20, unit).center();
        Selection enemies = unit.enemiesNear().havingAntiAirWeapon();
        HasPosition enemy = enemies.canAttack(unit, 3.5 + unit.shieldWound() / 15).center();

        if (enemy != null) return enemy;

        // === vs Scourge ==========================================

        AUnit nearScourge = getDangerouslyNearScourge(unit);
        if (nearScourge != null) return nearScourge;

        // =========================================================

        AUnit nearestEnemy = enemies.inRadius(6 + unit.shieldWound() / 25, unit).nearestTo(unit);
        if (nearestEnemy != null) return nearestEnemy;

        return null;
    }

    private static AUnit getDangerouslyNearScourge(AUnit unit) {
        return unit.enemiesNear()
            .ofType(AUnitType.Zerg_Scourge)
            .inRadius(7 + (unit.idIsOdd() ? 2 : 0), unit)
            .nearestTo(unit);
    }
}
