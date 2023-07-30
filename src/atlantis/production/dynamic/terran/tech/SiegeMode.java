package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.decisions.Decisions;
import atlantis.information.tech.ATech;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import bwapi.TechType;

public class SiegeMode extends Commander {
    @Override
    public boolean applies() {
        return Have.factory() && ATech.isNotResearchedOrPlanned(TechType.Tank_Siege_Mode) && (
            Count.tanks() >= 1
                || Decisions.wantsToBeAbleToProduceTanksSoon()
                || A.seconds() >= 350
        );
    }

    @Override
    public void handle() {
        AUnit machineShop = Select.ourOfType(AUnitType.Terran_Machine_Shop).random();
//            System.err.println("-------- machineShop = " + machineShop);
        if (machineShop != null) {
//                System.err.println("Tank_Siege_Mode @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            AddToQueue.tech(TechType.Tank_Siege_Mode);
        }

        // No machine shop
        else {
            if (Count.existingOrInProductionOrInQueue(AUnitType.Terran_Machine_Shop) == 0) {
                AddToQueue.withTopPriority(AUnitType.Terran_Machine_Shop);
                System.out.println("Enqueueing Machine Shop for Siege Mode");
            }
        }
    }

    public static boolean isResearched() {
        return ATech.isResearched(TechType.Tank_Siege_Mode);
    }
}
