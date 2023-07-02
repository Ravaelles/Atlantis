package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.decisions.Decisions;
import atlantis.information.tech.ATech;
import atlantis.production.dynamic.ADynamicTech;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import bwapi.TechType;
import bwapi.UpgradeType;


public class TerranDynamicTech extends ADynamicTech {

    public static void update() {
        if (A.notNthGameFrame(39)) {
            return;
        }

//        System.out.println("Count.tanks() = " + Count.tanks() + " / " + Have.factory());
        if (
            Have.factory() && ATech.isNotResearchedOrPlanned(TechType.Tank_Siege_Mode) && (
                Count.tanks() >= 1
                || Decisions.wantsToBeAbleToProduceTanksSoon()
                || (A.seconds() >= 350 || Count.tanks() >= 2)
            )
        ) {
            AUnit machineShop = Select.ourOfType(AUnitType.Terran_Machine_Shop).random();
//            System.err.println("-------- machineShop = " + machineShop);
            if (machineShop != null) {
//                System.err.println("Tank_Siege_Mode @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                AddToQueue.tech(TechType.Tank_Siege_Mode);
                return;
            }
//            if (machineShop != null) {
//                AddToQueue.tech(TechType.Tank_Siege_Mode);
//                return;
//            }
        }

        if (Count.ghosts() >= 1 && ATech.isNotResearchedOrPlanned(TechType.Lockdown)) {
            AddToQueue.tech(TechType.Lockdown);
            return;
        }

        if (OurStrategy.get().goingBio()) {
            if (Count.infantry() >= 8 && AGame.canAffordWithReserved(100, 100)) {
                if (ATech.isNotResearchedOrPlanned(TechType.Stim_Packs)) {
                    AddToQueue.tech(TechType.Stim_Packs);
                    return;
                }

                if (ATech.isNotResearchedOrPlanned(UpgradeType.U_238_Shells)) {
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
