package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.strategy.decisions.OurDecisions;
import atlantis.information.tech.ATech;
import atlantis.production.dynamic.ADynamicTech;
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
            handleTech(TechType.Tank_Siege_Mode);
        }
    }

}
