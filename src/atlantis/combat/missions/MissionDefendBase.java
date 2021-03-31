package atlantis.combat.missions;

import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;

public class MissionDefendBase extends Mission {

    protected MissionDefendBase(String name) {
        super(name);
    }

    @Override
    public boolean update(AUnit unit) {
        AUnit enemy = null;

        // === Attack units nears main =============================

        AUnit ourCenterUnit = Select.mainBase();
        if (ourCenterUnit == null) {
            ourCenterUnit = unit;
        }

        if (ourCenterUnit != null) {
            AUnit nearestEnemy = Select.enemy().visible().nearestTo(ourCenterUnit);
            if (nearestEnemy != null) {
                enemy = nearestEnemy;
            }
        }

        // Attack enemy
        if (enemy != null) {
            unit.setTooltip("#DefendBase");
            unit.attackUnit(enemy);
            return true;
        }

        // =========================================================

        return false;
    }

    @Override
    public APosition focusPoint() {
        return null;
    }

}
