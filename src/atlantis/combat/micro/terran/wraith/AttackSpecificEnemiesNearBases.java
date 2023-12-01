package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class AttackSpecificEnemiesNearBases extends Manager {
    private Selection specificEnemies;

    public AttackSpecificEnemiesNearBases(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isAir() && unit.hasGroundWeapon()
            && (specificEnemies = targets()).notEmpty();
    }

    private Selection targets() {
        return EnemyUnits.discovered()
            .ofType(targetTypes())
            .inRadius(30, Select.ourBasesWithUnfinished())
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
