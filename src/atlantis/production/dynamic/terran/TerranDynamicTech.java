package atlantis.production.dynamic.terran;

import atlantis.production.dynamic.ADynamicTech;
import atlantis.strategy.OurStrategy;
import atlantis.strategy.decisions.OurDecisions;
import atlantis.util.A;
import atlantis.wrappers.ATech;
import bwapi.TechType;
import bwapi.UpgradeType;


public class TerranDynamicTech extends ADynamicTech {

    public static void update() {
        if (A.notNthGameFrame(50)) {
            return;
        }

        if (OurStrategy.get().goingBio()) {
            handleUpgrade(UpgradeType.Terran_Infantry_Weapons);
            handleUpgrade(UpgradeType.Terran_Infantry_Armor);
            handleUpgrade(UpgradeType.Caduceus_Reactor);
        }

        if (
                OurDecisions.wantsToBeAbleToProduceTanksSoon()
                || (A.supplyUsed(70) && !ATech.isResearched(TechType.Tank_Siege_Mode))
        ) {
            handleUpgrade(TechType.Tank_Siege_Mode);
        }
    }

}
