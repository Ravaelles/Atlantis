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
        return (unit.isStopped() || unit.isHoldingPosition())
            && !EnemyInfo.weKnowAboutAnyRealUnit()
            && !EnemyInfo.hasDiscoveredAnyBuilding();
    }

    protected Manager handle() {
        unit.setTooltipTactical("Find enemy");
//        if (true) throw new RuntimeException("wut / " + this.parentsStack());

        if ((new ScoutManager(unit)).forceHandle() != null) return usedManager(this);

        return null;
    }
}
