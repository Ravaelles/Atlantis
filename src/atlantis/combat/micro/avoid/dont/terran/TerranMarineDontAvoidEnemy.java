package atlantis.combat.micro.avoid.dont.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class TerranMarineDontAvoidEnemy extends Manager {
    public TerranMarineDontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isMarine()
            && unit.isMissionDefend()
            && unit.isHealthy()
        ) {
            if (
                unit.friendsNear().inRadius(2, unit).count() >= 4
                    && unit.meleeEnemiesNearCount(3) == 0
            ) return true;

            if (unit.nearestOurTankDist() <= 3 && unit.nearestEnemyDist() > 3) return true;
        }

        return false;
    }

    @Override
    public Manager handle() {
        return this;
    }
}
