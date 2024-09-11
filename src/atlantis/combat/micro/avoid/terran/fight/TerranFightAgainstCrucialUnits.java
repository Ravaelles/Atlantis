package atlantis.combat.micro.avoid.terran.fight;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;

public class TerranFightAgainstCrucialUnits extends Manager {

    private Selection enemies;

    public TerranFightAgainstCrucialUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        enemies = unit.enemiesNear().ofType(AUnitType.Protoss_Interceptor, AUnitType.Protoss_Carrier).inRadius(7, unit);

        return unit.hasAirWeapon()
            && unit.woundPercentMax(15)
            && enemies.empty();
    }

    @Override
    public Manager handle() {
        return (new AttackNearbyEnemies(unit)).invokeFrom(this);
    }
}
