package atlantis.production.dynamic.terran.tech;

import atlantis.game.A;
import atlantis.information.decisions.terran.TerranDecisions;
import atlantis.information.generic.Army;
import atlantis.information.tech.ATech;
import atlantis.production.dynamic.protoss.tech.TechResearchCommander;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;
import bwapi.TechType;

public class ResearchSiegeMode extends TechResearchCommander {
    @Override
    public TechType what() {
        return TechType.Tank_Siege_Mode;
    }

    @Override
    public boolean applies() {
        if (isResearched) return false;

        return Have.factory() && shouldResearchNow();
    }

    private static boolean shouldResearchNow() {
        if (TerranDecisions.DONT_PRODUCE_TANKS_AT_ALL.isFalse() && Count.tanks() <= 0) return false;
        if (!ATech.isNotResearchedOrPlanned(TechType.Tank_Siege_Mode)) return false;

        return Enemy.terran() ? shouldResearchAgainstTerran() : shouldResearchAgainstProtossAndZerg();
    }

    private static boolean shouldResearchAgainstProtossAndZerg() {
        int tanks = Count.tanks();

        return tanks >= 2 || (tanks >= 1 && A.canAffordWithReserved(150, 100));
    }

    private static boolean shouldResearchAgainstTerran() {
        int tanks = Count.tanks();

        return (tanks >= 3 || A.seconds() >= 440) && Army.strength() >= 200;
    }

//    @Override
//    protected void handle() {
//        AUnit machineShop = Select.ourFree(AUnitType.Terran_Machine_Shop).first();
//        if (machineShop != null) {
//            AddToQueue.tech(TechType.Tank_Siege_Mode);
//        }
//
//        // No machine shop
//        else {
//            if (Count.existingOrInProductionOrInQueue(AUnitType.Terran_Machine_Shop) == 0) {
//                AddToQueue.withTopPriority(AUnitType.Terran_Machine_Shop);
//            }
//        }
//    }

    public static boolean isResearched() {
        return isResearched = ATech.isResearched(TechType.Tank_Siege_Mode);
    }
}
