package atlantis.production.dynamic.terran.bunker;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.generic.AllOfOurBasePositions;
import atlantis.information.generic.ArmyStrength;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.We;

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
        for (HasPosition position : AllOfOurBasePositions.allBases(true, true)) {
            if (position == null) continue;

            if (ensurePositionHasBunker(position)) return true;
        }

        return false;
    }

    private boolean ensurePositionHasBunker(HasPosition position) {
        if (Count.hasExistingOrPlannedBuildingNear(Terran_Bunker, 8, position)) return false;

        return (new ReinforceWithBunkerAtNearestChoke(position)).invokedCommander();
    }
}
