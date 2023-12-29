package atlantis.combat.micro.avoid.terran.avoid;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public class ShouldNeverAvoidAsTerran extends HasUnit {
    public ShouldNeverAvoidAsTerran(AUnit unit) {
        super(unit);
    }

    public boolean shouldNeverAvoid() {
        if (asMarine()) return true;
        if (asMedic()) return true;

        return false;
    }

    private boolean asMedic() {
        if (!unit.isMedic()) return false;

        return unit.isHealthy();
    }

    private boolean asMarine() {
        if (!unit.isMarine()) return false;

        if (enemyWeakAirUnitsNearby()) {
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
