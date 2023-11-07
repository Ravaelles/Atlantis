package atlantis.combat.micro.avoid.terran;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public class ShouldNeverAvoidAsTerran extends HasUnit {
    public ShouldNeverAvoidAsTerran(AUnit unit) {
        super(unit);
    }

    public boolean shouldNeverAvoid() {
        if (unit.isMarine() && enemyWeakAirUnitsNearby()) return true;

        return false;
    }

    private boolean enemyWeakAirUnitsNearby() {
        return unit.enemiesNear().air().havingGroundWeapon().inRadius(6, unit).size() > 0
            && unit.hpMoreThan(20)
            && unit.combatEvalRelative() > 0.8;
    }
}
