package atlantis.production.constructions.builders;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.PriorityWaiters;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

import java.util.HashSet;
import java.util.Set;

public class GetOptimalBuilder {
    public static AUnit forPosition(Construction construction, ProductionOrder order) {
        HasPosition positionToBuild = construction.buildPosition();

        if (positionToBuild == null) {
            ErrorLog.printMaxOncePerMinute("No position to build, so can't find builder ("
                + (order == null ? "-" : order.unitType()) + ")");

            return FreeWorkers.get().random();
        }

//        boolean allowRepairers = true;

        // Owner: always the free pool, never a hand-rolled fallback chain.
        // forPosition answers null when no worker is truly free - every
        // caller already owns that case (adopt-no-free-builder twin, fresh
        // request refusal with retry) - instead of burning a scout or
        // double-linking a builder that then starves one of its sites.
        return defineWorker(positionToBuild);
    }

    private static AUnit defineWorker(HasPosition positionToBuild) {
        AUnit otherBuilder = useAnotherProtossBuilderInsteadOfAssigningNewOne(positionToBuild);
        if (otherBuilder != null) return otherBuilder;

        return FreeWorkers.get().nearestTo(positionToBuild);
    }

    private static AUnit useAnotherProtossBuilderInsteadOfAssigningNewOne(HasPosition positionToBuild) {
        if (!We.protoss()) return null;
        if (!A.hasFreeSupply(1)) return null;
        if (A.hasMinerals(600)) return null;
        if (Select.ourWorkers().inRadius(12, positionToBuild).count() >= 4) return null;

        Selection candidates = Select.ourWorkers()
            .builders()
            .inRadius(13, positionToBuild);
        if (PriorityWaiters.coreOutranksPylons()) {
            // Owner 2026-10-15: a builder working a live Core is not spare
            // capacity while the Core outranks Pylons - take a free miner
            // instead (the fallthrough below), never the Core's probe.
            candidates = candidates.exclude(coreBuilders());
        }
        return candidates.nearestTo(positionToBuild);
    }

    private static Set<AUnit> coreBuilders() {
        Set<AUnit> builders = new HashSet<>();
        for (Construction construction : ConstructionRequests.notFinishedOfType(
            AUnitType.Protoss_Cybernetics_Core)) {
            if (construction.builder() != null) builders.add(construction.builder());
        }
        return builders;
    }
}
