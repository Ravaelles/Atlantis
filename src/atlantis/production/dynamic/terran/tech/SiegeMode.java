package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.decisions.terran.TerranDecisions;
import atlantis.information.generic.OurArmy;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import bwapi.TechType;

public class SiegeMode extends Commander {
    @Override
    public boolean applies() {
        return Have.factory() && shouldResearchNow();
    }

    private static boolean shouldResearchNow() {
        if (TerranDecisions.DONT_PRODUCE_TANKS_AT_ALL.isFalse()) return false;
        if (!ATech.isNotResearchedOrPlanned(TechType.Tank_Siege_Mode)) return false;

        return Enemy.terran() ? shouldResearchAgainstTerran() : shouldResearchAgainstProtossAndZerg();
    }

    private static boolean shouldResearchAgainstProtossAndZerg() {
        int tanks = Count.tanks();

        return tanks >= 2 || (tanks >= 1 && A.seconds() >= 430);
    }

    private static boolean shouldResearchAgainstTerran() {
        int tanks = Count.tanks();

        return (tanks >= 3 || A.seconds() >= 440) && OurArmy.strength() >= 200;
    }

    @Override
    protected void handle() {
        AUnit machineShop = Select.ourOfType(AUnitType.Terran_Machine_Shop).random();
        if (machineShop != null) {
            AddToQueue.tech(TechType.Tank_Siege_Mode);
        }

        // No machine shop
        else {
            if (Count.existingOrInProductionOrInQueue(AUnitType.Terran_Machine_Shop) == 0) {
                AddToQueue.withTopPriority(AUnitType.Terran_Machine_Shop);
            }
        }
    }

    public static boolean isResearched() {
        return ATech.isResearched(TechType.Tank_Siege_Mode);
    }
}
