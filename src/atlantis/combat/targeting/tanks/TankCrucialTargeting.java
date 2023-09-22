package atlantis.combat.targeting.tanks;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.HasUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import java.util.List;

public class TankCrucialTargeting extends HasUnit {
    private final Selection enemies;

    public TankCrucialTargeting(AUnit unit, List<AUnit> enemies) {
        super(unit);
        this.enemies = Select.from(enemies, "TankCrucialTargeting:" + unit.id()).inShootRangeOf(unit);
    }

    public AUnit crucialTarget() {
        AUnit target = null;

        // Protoss
        if ((target = enemies.ofType(AUnitType.Protoss_Reaver).first()) != null) return target;
        if ((target = enemies.ofType(AUnitType.Protoss_Dark_Templar).first()) != null) return target;
        if ((target = enemies.ofType(AUnitType.Protoss_High_Templar).first()) != null) return target;
        if ((target = enemies.ofType(AUnitType.Protoss_Archon).first()) != null) return target;

        // Terran
        if ((target = enemies.ofType(AUnitType.Terran_Siege_Tank_Siege_Mode).first()) != null) return target;
        if ((target = enemies.ofType(AUnitType.Terran_Siege_Tank_Tank_Mode).first()) != null) return target;

        // Zerg
        if ((target = enemies.ofType(AUnitType.Zerg_Defiler).first()) != null) return target;
        if ((target = enemies.ofType(AUnitType.Zerg_Ultralisk).first()) != null) return target;

        return null;
    }
}
