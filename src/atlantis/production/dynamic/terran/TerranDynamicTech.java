package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.decisions.Decisions;
import atlantis.information.tech.ATech;
import atlantis.production.ProductionOrder;
import atlantis.production.dynamic.ADynamicTech;
import atlantis.production.orders.build.AddToQueue;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import bwapi.TechType;
import bwapi.UpgradeType;


public class TerranDynamicTech extends ADynamicTech {

    public static void update() {
        if (A.notNthGameFrame(35)) {
            return;
        }

        if (
            !ATech.isResearched(TechType.Tank_Siege_Mode) && (
                Count.tanks() >= 1 || Decisions.wantsToBeAbleToProduceTanksSoon() || A.seconds() >= 600
            )
        ) {
            AUnit machineShop = Select.ourOfType(AUnitType.Terran_Machine_Shop).first();
            if (machineShop != null) {
                AddToQueue.tech(TechType.Tank_Siege_Mode);
                return;
            }
        }

        if (Count.ghosts() >= 1) {
            AddToQueue.tech(TechType.Lockdown);
        }

        if (OurStrategy.get().goingBio()) {
            if (Count.infantry() >= 8 && AGame.canAffordWithReserved(100, 100)) {
                if (!ATech.isResearched(TechType.Stim_Packs)) {
                    AddToQueue.tech(TechType.Stim_Packs);
                    return;
                }

                if (!ATech.isResearched(UpgradeType.U_238_Shells)) {
                    AddToQueue.upgrade(UpgradeType.U_238_Shells);
                    return;
                }
            }

            if (Count.infantry() >= 12 && AGame.canAffordWithReserved(100, 150)) {
                if (ATech.getUpgradeLevel(UpgradeType.Terran_Infantry_Armor) <= 1) {
                    AddToQueue.upgrade(UpgradeType.Terran_Infantry_Armor);
                    return;
                }
                if (ATech.getUpgradeLevel(UpgradeType.Terran_Infantry_Weapons) <= 1) {
                    AddToQueue.upgrade(UpgradeType.Terran_Infantry_Weapons);
                    return;
                }
            }

            // Medic +50 energy
//            AddToQueue.upgrade(UpgradeType.Caduceus_Reactor);
        }
    }

}
