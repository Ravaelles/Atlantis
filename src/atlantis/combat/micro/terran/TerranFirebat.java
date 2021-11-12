package atlantis.combat.micro.terran;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class TerranFirebat {

    public static boolean shouldContinueMeleeFighting(AUnit unit) {
        if (unit.hp() >= 40) {
            return true;
        }

        int medics = Select.ourOfType(AUnitType.Terran_Medic).havingEnergy(20).inRadius(3, unit).count();

        if (medics >= 2) {
            return true;
        }

        int enemies = Select.enemyCombatUnits().canAttack(unit, 0).count();

        int enemyModifier = Enemy.zerg() ? 15 : 30;
        return unit.hpPercent(enemies * enemyModifier);
    }

}
