package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.combat.targeting.air.AAirUnitAirTargets;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;

public class AttackSpecificEnemies extends Manager {
    private Selection specificEnemies;

    public AttackSpecificEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isAir() && unit.hasGroundWeapon()
            && (specificEnemies = targets()).notEmpty();
    }

    private Selection targets() {
        return unit.enemiesNear()
            .ofType(targetTypes())
            .canBeAttackedBy(unit, 0.5)
            .effVisible();
    }

    private AUnitType[] targetTypes() {
        return new AUnitType[]{
            AUnitType.Terran_Siege_Tank_Tank_Mode, AUnitType.Terran_Siege_Tank_Siege_Mode,
            AUnitType.Protoss_Reaver, AUnitType.Protoss_High_Templar,
            AUnitType.Zerg_Defiler,
        };
    }

    @Override
    public Manager handle() {
        AUnit target = specificEnemies.mostWounded();

        if (target == null) return null;

        unit.attackUnit(target);
        return usedManager(this);
    }
}
