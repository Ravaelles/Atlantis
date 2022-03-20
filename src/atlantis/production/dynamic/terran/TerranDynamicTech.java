package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.decisions.Decisions;
import atlantis.information.tech.ATech;
import atlantis.production.ProductionOrder;
import atlantis.production.dynamic.ADynamicTech;
import atlantis.production.orders.build.AddToQueue;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;
import bwapi.TechType;
import bwapi.UpgradeType;


public class TerranDynamicTech extends ADynamicTech {

    public static void update() {
        if (A.notNthGameFrame(46)) {
            return;
        }

        if (Count.ghosts() >= 2) {
            AddToQueue.tech(TechType.Lockdown);
        }

        if (
            !ATech.isResearched(TechType.Tank_Siege_Mode) && (
                Decisions.wantsToBeAbleToProduceTanksSoon()
                || (Enemy.protoss() && Have.machineShop())
                || Count.tanks() >= 1
            )
        ) {
            AddToQueue.tech(TechType.Tank_Siege_Mode);
        }

        if (OurStrategy.get().goingBio()) {
            AddToQueue.upgrade(UpgradeType.Terran_Infantry_Weapons);
            AddToQueue.upgrade(UpgradeType.Terran_Infantry_Armor);
            AddToQueue.upgrade(UpgradeType.Caduceus_Reactor);
        }
    }

}
