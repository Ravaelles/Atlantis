package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class SiegeVsRegularBuildings extends Manager {
    private AUnit enemyBuilding = null;

    public SiegeVsRegularBuildings(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.distToLeader() >= 8) return false;

        if ((new WouldBlockChokeBySieging(unit)).applies()) return false;
        if (unit.lastSiegedOrUnsiegedAgo() <= 30 * (7 + unit.id() % 6)) return false;

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
        if (WantsToSiege.wantsToSiegeNow(unit, this, "SiegeBuilding")) return usedManager(this);

        return null;
    }
}
