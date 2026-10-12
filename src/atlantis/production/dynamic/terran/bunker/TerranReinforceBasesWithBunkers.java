package atlantis.production.dynamic.terran.bunker;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.expansion.PendingNextBase;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;

import java.util.ArrayList;

import static atlantis.units.AUnitType.Terran_Bunker;

public class TerranReinforceBasesWithBunkers extends Commander {
    @Override
    public boolean applies() {
        if (!We.terran()) return false;
        if (A.everyFrameExceptNthFrame(35)) return false;
        if (Count.bases() <= 2) return false;
        if (Count.bunkersWithUnfinished() > Count.basesWithUnfinished()) return false;

        if (
            Have.barracks()
                && Count.existingOrInProductionOrInQueue(AtlantisRaceConfig.BASE) >= 2
                && Count.withPlanned(AUnitType.Terran_Bunker) <= Count.basesWithPlanned()
//                && CountInQueue.count(AUnitType.Terran_Bunker, 10) <= 0
        ) {
            int maxBunkersInProgress = ArmyStrength.ourArmyRelativeStrength() <= 70 ? 2 : 1;
            if (Count.inProductionOrInQueue(Terran_Bunker) >= maxBunkersInProgress) return false;

            return true;
        }

        return false;
    }

    @Override
    protected boolean handle() {
        for (HasPosition position : allBases(true, true)) {
            if (position == null) continue;

            if (ensurePositionHasBunker(position)) return true;
        }

        return false;
    }

    /**
     * Moved here from {@code information.generic.AllOfOurBasePositions} (WO-13):
     * its only caller was this commander, and the information->production edges
     * inside it (pending base, not-started constructions) belong to the
     * production side that consumes them.
     */
    private static ArrayList<HasPosition> allBases(boolean includeNotStarted, boolean includeNotFinished) {
        ArrayList<HasPosition> positions = new ArrayList<>();

        if (includeNotStarted) {
            HasPosition nextBase = PendingNextBase.basePositionOrNull();
            positions.add(nextBase);
        }

        if (includeNotFinished) {
            for (Construction construction : ConstructionRequests.notStartedOfType(AtlantisRaceConfig.BASE)) {
                APosition position = construction.buildPosition();
                positions.add(position);
            }
        }

        positions.addAll(Select.ourBasesWithUnfinished().list());

        return positions;
    }

    private boolean ensurePositionHasBunker(HasPosition position) {
        if (Count.hasExistingOrPlannedBuildingNear(Terran_Bunker, 8, position)) return false;

        return requestBunkerAt(positionForBunkerNear(position));
    }

    private HasPosition positionForBunkerNear(HasPosition position) {
        AChoke choke = position.nearestChoke();
        if (choke == null) return position;

        return choke.translateTilesTowards(5, position);
    }

    protected boolean requestBunkerAt(HasPosition position) {
        return AddToQueue.withHighPriority(Terran_Bunker, position) != null;
    }
}
