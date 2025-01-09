package atlantis.information.strategy.response.terran;

import atlantis.game.A;
import atlantis.information.strategy.response.StrategyResponse;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.map.region.ARegion;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class TerranResponseEnemyHiddenUnits extends StrategyResponse {
    private int howManyAllowed = 3;

    public boolean handle() {
        if (Have.notEvenPlanned(AUnitType.Terran_Engineering_Bay)) {
            AddToQueue.withTopPriority(AUnitType.Terran_Engineering_Bay);
        }

        if (buildAtBunker()) return true;

        return buildAnywhere();
    }

    private boolean buildAnywhere() {
        int n = A.supplyUsed() >= 45 ? 3 : 2;
//        ErrorLog.debug("### TerranResponseEnemyHiddenUnits - buildAnywhere " + n + " turrets");

        for (int i = 0; i < n; i++) {
            if (AddToQueue.withHighPriority(
                AUnitType.Terran_Missile_Turret,
                Chokes.mainChoke().translateTilesTowards(6, Select.mainOrAnyBuilding())
            ) != null) {
                howManyAllowed--;
            }
        }
        return true;
    }

    private boolean buildAtBunker() {
        AUnit bunker = Select.ourBuildingsWithUnfinished().bunkers().mostDistantTo(Select.mainOrAnyBuilding());
        if (bunker != null) {
            HasPosition at = bunker;
            ARegion region = bunker.position().region();
            if (region != null) at = at.translateTilesTowards(1.5, region.center());

            int n = howManyAllowed;
            if (n <= 0) return false;

//            ErrorLog.debug("### TerranResponseEnemyHiddenUnits - buildAtBunker " + n + " turrets");

            for (int i = 0; i < n; i++) {
                if (AddToQueue.withHighPriority(AUnitType.Terran_Missile_Turret, at) != null) {
                    howManyAllowed--;
                }
            }
            return true;
        }
        return false;
    }
}
