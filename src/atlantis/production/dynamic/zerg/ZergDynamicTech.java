package atlantis.production.dynamic.zerg;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.tech.ATech;
import atlantis.production.dynamic.ADynamicTech;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Count;
import bwapi.TechType;
import bwapi.UpgradeType;


public class ZergDynamicTech extends ADynamicTech {

    public static boolean update() {
        if (A.notNthGameFrame(35)) {
            return false;
        }

//        if (Count.ghosts() >= 1) {
//            AddToQueue.tech(TechType.Lockdown);
//        }

        int hydras = Count.hydralisks();
        if (hydras >= 8 && AGame.canAffordWithReserved(70, 50)) {
            if (
                !ATech.isResearched(UpgradeType.Muscular_Augments)
            ) {
                return AddToQueue.upgrade(UpgradeType.Muscular_Augments);
            }

//            if (
//                !ATech.isResearched(UpgradeType.U_238_Shells)
//            ) {
//                AddToQueue.upgrade(UpgradeType.U_238_Shells);
//                return;
//            }
        }

        int zerglings = Count.zerglings();
//        AGame.canAffordWithReserved(100, 100)
        if (zerglings >= 12 && A.hasGas(50)) {
            if (ATech.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) <= (A.supplyUsed() / 55)) {
                return AddToQueue.upgrade(UpgradeType.Zerg_Melee_Attacks);
            }
            if (ATech.getUpgradeLevel(UpgradeType.Zerg_Carapace) <= (A.supplyUsed() / 64)) {
                return AddToQueue.upgrade(UpgradeType.Zerg_Carapace);
            }
        }

        return false;
    }

}
