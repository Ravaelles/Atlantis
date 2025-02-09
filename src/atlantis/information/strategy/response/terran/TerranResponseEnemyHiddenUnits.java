package atlantis.information.strategy.response.terran;

import atlantis.information.strategy.response.StrategyResponse;
import atlantis.map.position.HasPosition;
import atlantis.map.region.ARegion;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class TerranResponseEnemyHiddenUnits extends StrategyResponse {
    public boolean handle() {
        if (Have.notEvenPlanned(AUnitType.Terran_Engineering_Bay)) {
            AddToQueue.withTopPriority(AUnitType.Terran_Engineering_Bay);
        }

        if (buildAtBunker()) return true;

        return buildAnywhere();
    }

    private static boolean buildAnywhere() {
        int howMany = 2;
        for (int i = 0; i < howMany; i++) {
            AddToQueue.withHighPriority(
                AUnitType.Terran_Missile_Turret
//                Chokes.mainChoke().translateTilesTowards(6, Select.mainOrAnyBuilding())
            );
        }
        return true;
    }

    private static boolean buildAtBunker() {
        AUnit bunker = Select.ourBuildingsWithUnfinished().bunkers().mostDistantTo(Select.mainOrAnyBuilding());
        if (bunker != null) {
            HasPosition at = bunker;
            ARegion region = bunker.position().region();
            if (region != null) at = at.translateTilesTowards(1.5, region.center());

            AddToQueue.withHighPriority(AUnitType.Terran_Missile_Turret, at);
            AddToQueue.withHighPriority(AUnitType.Terran_Missile_Turret, at);
            return true;
        }
        return false;
    }
}
