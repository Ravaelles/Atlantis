package atlantis.combat.micro.avoid.terran.avoid;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public class ShouldNeverAvoidAsTerran extends HasUnit {
    public ShouldNeverAvoidAsTerran(AUnit unit) {
        super(unit);
    }

    public boolean shouldNeverAvoid() {
        if (unit.isMarine() && enemyWeakAirUnitsNearby()) {
            unit.addLog("NeverAvoidMarine");
            return true;
        }

        return false;
    }

    private boolean enemyWeakAirUnitsNearby() {
        return unit.cooldown() >= 2
            && unit.combatEvalRelative() > 0.8
            && unit.hpMoreThan(20)
            && unit.enemiesNear().air().havingAntiGroundWeapon().inRadius(6, unit).size() > 0;
    }
}
