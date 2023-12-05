package atlantis.combat.micro.avoid.dont.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class TerranMarineDontAvoidEnemy extends Manager {
    public TerranMarineDontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isMarine()
            && unit.isMissionDefend()
            && unit.isHealthy()
            && unit.friendsNear().inRadius(2, unit).count() >= 4
            && unit.meleeEnemiesNearCount(3) == 0;
    }

    @Override
    public Manager handle() {
        return this;
    }
}
