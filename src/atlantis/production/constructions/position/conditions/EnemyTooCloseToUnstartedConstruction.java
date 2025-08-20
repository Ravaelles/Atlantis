package atlantis.production.constructions.position.conditions;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

public class EnemyTooCloseToUnstartedConstruction {
    public static String _STATUS = null;

    private static Cache<Selection> cache = new Cache<>();

    public static boolean check(AUnit builder, AUnitType building, APosition position) {
        _STATUS = null;

        AUnit enemy = defineEnemies(position).nearestTo(position);
        if (enemy == null) return false;

        double groundDist = enemy.groundDist(position) - enemy.groundWeaponRange();
        if (groundDist > 8) return false;

        if (building.isCombatBuilding() && position.distToMain() <= 30) {
            return false;
        }

        _STATUS = enemy.name() + "," + A.digit(groundDist);
        return true;
    }

    private static Selection defineEnemies(APosition position) {
        return cache.get(
            "defineEnemies",
            31,
            () -> EnemyUnits.discovered().combatUnits().havingAntiGroundWeapon()
        );
    }
}
