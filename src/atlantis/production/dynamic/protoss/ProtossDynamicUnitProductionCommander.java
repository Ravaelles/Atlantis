
package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.generic.OurArmy;
import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.production.dynamic.protoss.units.*;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.select.Count;
import atlantis.util.HasReason;
import atlantis.util.We;

public class ProtossDynamicUnitProductionCommander extends Commander implements HasReason {
    public static String reason = "-";

    @Override
    public boolean applies() {
        return We.protoss()
            && freeToSpendResources();
//            && !ProtossShouldExpand.needToSaveMineralsForExpansion();
    }

    private static boolean freeToSpendResources() {
        if (!A.hasMinerals(500) && ShouldExpand.shouldExpand()) return decision(false, "ExpansionMinerals");
        if (A.hasMinerals(550)) return decision(true, "Minerals++");

        if (A.supplyUsed() >= 25) {
            int reservedMinerals = A.inRange(0, ReservedResources.minerals(), 410);
            int mineralsMargin = A.supplyUsed() < 40 ? 150 : 200;
//            int reservedGas = ReservedResources.gas();
//            int gasMargin = A.supplyUsed() < 40 ? 50 : 125;

            if (reservedMinerals > 0 && !A.hasMinerals(mineralsMargin + reservedMinerals))
                return decision(false, "MissingMinerals");
//            if (reservedGas > 0 && !A.hasGas(gasMargin + reservedMinerals))
//                return decision(false, "MissingGas");
        }

        if (hasTooFewUnits()) return decision(true, "TooFewUnits");
        if (manyBasesAndHasMinerals()) return decision(true, "ConstStream");
        if (inEarlyGamePhaseMakeSureNotToBeTooWeak()) return decision(true, "PreventWeak");

        if (keepSomeResourcesInLaterGamePhases()) return decision(false, "KeepResources");

//        System.err.println(A.now() + " 2dyna produce: " + A.minerals() + "/" + reservedMinerals);

        return decision(true, "OK");
    }

    private static boolean manyBasesAndHasMinerals() {
        return A.hasMinerals(A.supplyUsed() <= 36 ? 250 : 310) && Count.basesWithUnfinished() >= 2;
    }

    private static boolean hasTooFewUnits() {
        int combatUnits = Count.ourCombatUnits();

        if (A.seconds() >= 700 && combatUnits <= 30) return true;
        if (A.seconds() >= 600 && combatUnits <= 27) return true;
        if (A.seconds() >= 500 && combatUnits <= 23) return true;
        if (A.seconds() >= 400 && combatUnits <= 18) return true;

        return false;
    }

    private static boolean decision(boolean b, String reason) {
        ProtossDynamicUnitProductionCommander.reason = reason;
        return b;
    }

    private static boolean inEarlyGamePhaseMakeSureNotToBeTooWeak() {
        return A.seconds() <= 400
            && (OurArmy.strength() < 0.85 || Count.zealots() <= 2 || Count.dragoons() <= 3);
    }

    private static boolean keepSomeResourcesInLaterGamePhases() {
        if (
            A.seconds() >= 450
                && !A.hasMinerals(500)
                && Count.basesWithUnfinished() <= 2
        ) return true;

        return !A.hasMinerals(320) && A.seconds() > 320;
    }

    protected void handle() {
        if (!AGame.everyNthGameFrame(7)) return;

        ProduceScarabs.scarabs();
        ProduceObserver.observers();
        ProduceArbiters.arbiters();
        ProduceShuttles.shuttles();
        ProduceReavers.reavers();
        ProduceDarkTemplar.dt();
        ProduceHighTemplar.ht();
        ProduceCorsairs.corsairs();

        ProduceDragoon.dragoon();
        ProduceZealot.zealot();
    }

    @Override
    public String reason() {
        return reason;
    }
}
