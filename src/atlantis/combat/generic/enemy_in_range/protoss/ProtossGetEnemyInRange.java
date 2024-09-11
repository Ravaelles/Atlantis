package atlantis.combat.generic.enemy_in_range.protoss;

import atlantis.units.AUnit;

public class ProtossGetEnemyInRange {
    public static AUnit enemyInRange(AUnit unit) {
        return unit.enemiesNear()
            .nonBuildingsButAllowCombatBuildings()
            .canBeAttackedBy(unit, 0)
            .mostWounded();
    }
}
