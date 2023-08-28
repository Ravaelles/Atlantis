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
        if ((new WouldBlockChokeBySieging(unit)).invoke() != null) return false;

        if (unit.lastSiegeUnsiegedAgo() <= 30 * 9) return false;

        if (
            unit.id() % 5 == 0
                ||
                (unit.isHealthy() && unit.lastStartedAttackMoreThanAgo(30 * 40))
        ) {
            enemyBuilding =
                Select.enemy().buildings().visibleOnMap().inRadius(7.9 + unit.id() % 4, unit).nearestTo(unit);
        }

        return enemyBuilding != null;
    }

    protected Manager handle() {
        if (WantsToSiege.wantsToSiegeNow(unit, "SiegeBuilding")) return usedManager(this);

        return null;
    }
}
