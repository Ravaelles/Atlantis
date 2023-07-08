package atlantis.combat.micro.zerg.overlord;

import atlantis.information.enemy.EnemyInfo;
import atlantis.map.scout.AScoutManager;
import atlantis.units.AUnit;
import atlantis.units.managers.Manager;

public class WeDontKnowEnemyLocation extends Manager {

    public WeDontKnowEnemyLocation(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !EnemyInfo.hasDiscoveredAnyBuilding();
    }

    public Manager handle() {
        if (applies()) {
            unit.setTooltipTactical("Find enemy");

            if (AScoutManager.tryFindingEnemy(unit)) {
                return usedManager(this);
            }
        }

        return null;
    }
}
