package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.decisions.Decisions;
import atlantis.information.tech.ATech;
import atlantis.production.dynamic.ADynamicTech;
import atlantis.production.orders.build.AddToQueue;
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
            handleResearch(TechType.Lockdown);
        }

        if (
            !ATech.isResearched(TechType.Tank_Siege_Mode) && (
                Decisions.wantsToBeAbleToProduceTanksSoon()
                || (Enemy.protoss() && Have.machineShop())
                || Count.tanks() >= 1
            )
        ) {
            handleResearch(TechType.Tank_Siege_Mode);
        }

        if (OurStrategy.get().goingBio()) {
            handleUpgrade(UpgradeType.Terran_Infantry_Weapons);
            handleUpgrade(UpgradeType.Terran_Infantry_Armor);
            handleUpgrade(UpgradeType.Caduceus_Reactor);
        }
    }

}
