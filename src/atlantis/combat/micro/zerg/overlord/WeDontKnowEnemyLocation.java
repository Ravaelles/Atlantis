package atlantis.combat.micro.zerg.overlord;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.scout.ScoutManager;
import atlantis.units.AUnit;

public class WeDontKnowEnemyLocation extends Manager {
    public WeDontKnowEnemyLocation(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !EnemyInfo.hasDiscoveredAnyBuilding();
    }

    protected Manager handle() {
        unit.setTooltipTactical("Find enemy");

        return (new ScoutManager(unit)).invoke();
    }
}
