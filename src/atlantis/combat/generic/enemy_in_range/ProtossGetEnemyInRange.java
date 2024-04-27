package atlantis.combat.generic.enemy_in_range;

import atlantis.units.AUnit;

public class ProtossGetEnemyInRange {
    public static AUnit getEnemyInRange(AUnit unit) {
        return unit.enemiesNear()
            .nonBuildingsButAllowCombatBuildings()
            .canBeAttackedBy(unit, 0)
            .mostWounded();
    }
}
