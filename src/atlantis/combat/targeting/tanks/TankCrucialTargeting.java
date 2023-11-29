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
        if ((target = enemies.ofType(AUnitType.Protoss_Reaver).nearestTo(unit)) != null) return target;
        if ((target = enemies.ofType(AUnitType.Protoss_Dark_Templar).nearestTo(unit)) != null) return target;
        if ((target = enemies.ofType(AUnitType.Protoss_High_Templar).nearestTo(unit)) != null) return target;
        if ((target = enemies.ofType(AUnitType.Protoss_Archon).mostWounded()) != null) return target;

        // Terran
        if ((target = enemies.ofType(AUnitType.Terran_Siege_Tank_Siege_Mode).mostWounded()) != null) return target;
        if ((target = enemies.ofType(AUnitType.Terran_Siege_Tank_Tank_Mode).mostWounded()) != null) return target;

        // Zerg
        if ((target = enemies.ofType(AUnitType.Zerg_Defiler).nearestTo(unit)) != null) return target;
        if ((target = enemies.ofType(AUnitType.Zerg_Ultralisk).mostWounded()) != null) return target;

        return null;
    }
}
