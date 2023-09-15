package atlantis.production.dynamic.zerg;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.util.We;
import bwapi.TechType;
import bwapi.UpgradeType;


public class ZergDynamicTechResearch extends Commander {
    @Override
    public boolean applies() {
        return We.zerg() && A.everyNthGameFrame(39);
    }

    @Override
    protected void handle() {
        if (Count.ghosts() >= 2) {
            AddToQueue.tech(TechType.Lockdown);
        }

        int hydras = Count.hydralisks();
        if (hydras >= 8 && AGame.canAffordWithReserved(70, 50)) {
            if (
                !ATech.isResearched(UpgradeType.Muscular_Augments)
            ) {
                AddToQueue.upgrade(UpgradeType.Muscular_Augments);
                return;
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
                AddToQueue.upgrade(UpgradeType.Zerg_Melee_Attacks);
                return;
            }
            if (ATech.getUpgradeLevel(UpgradeType.Zerg_Carapace) <= (A.supplyUsed() / 64)) {
                AddToQueue.upgrade(UpgradeType.Zerg_Carapace);
                return;
            }
        }
    }

}
