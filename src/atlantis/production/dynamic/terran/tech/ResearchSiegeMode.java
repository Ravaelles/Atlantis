package atlantis.production.dynamic.terran.tech;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.decisions.terran.TerranDecisions;
import atlantis.information.generic.Army;
import atlantis.information.tech.ATech;
import atlantis.production.dynamic.protoss.tech.TechResearchCommander;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import bwapi.TechType;

public class ResearchSiegeMode extends TechResearchCommander {
    private static final boolean isResearched = false;
    private static boolean isEnqueued = false;

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

    public static boolean isResearched() {
        return isResearched;
    }

    @Override
    public TechType what() {
        return TechType.Tank_Siege_Mode;
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

    @Override
    public boolean applies() {
        if (isResearched) return false;

        return Have.factory() && shouldResearchNow();
    }

    @Override
    protected boolean isEnqueued() {
        return isEnqueued;
    }

    @Override
    protected void setEnqueued(boolean isEnqueued) {
        ResearchSiegeMode.isEnqueued = isEnqueued;
    }
}
