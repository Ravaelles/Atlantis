package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class SiegeAgainstRegularBuildings extends Manager {
    private AUnit enemyBuilding = null;

    public SiegeAgainstRegularBuildings(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if ((new WouldBlockChokeBySieging(unit)).handle() != null) return false;

        if (
            unit.id() % 5 == 0
                ||
                (unit.isHealthy() && unit.lastStartedAttackMoreThanAgo(30 * 40))
        ) {
            enemyBuilding = Select.enemy().buildings().inRadius(9.9, unit).nearestTo(unit);
        }

        return enemyBuilding != null;
    }

    protected Manager handle() {
        return usedManager(WantsToSiege.wantsToSiegeNow(this, "SiegeBuilding"));
    }
}
