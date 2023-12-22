package atlantis.production.dynamic.reinforce.terran;

import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.production.dynamic.reinforce.ReinforceBasesWithCombatBuildings;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Terran_Bunker;

public class TerranReinforceBasesWithCombatBuildings extends ReinforceBasesWithCombatBuildings {
    @Override
    public boolean applies() {
        if (!super.applies()) return false;

        int maxBunkersInProgress = ArmyStrength.ourArmyRelativeStrength() <= 70 ? 2 : 1;
        if (Count.inProductionOrInQueue(Terran_Bunker) >= maxBunkersInProgress) return false;

        return true;
    }

    @Override
    protected void handle() {
        if (handleEarlyGameTrouble()) return;
    }

    private boolean handleEarlyGameTrouble() {
        if (A.everyFrameExceptNthFrame(97)) return false;

        if (ArmyStrength.ourArmyRelativeStrength() <= 70) {
            if (Count.bunkers() <= 2) {
                (new ReinforceWithBunkerAtNearestChoke(Chokes.mainChoke())).invokeCommander();
                return true;
            }
        }

        return false;
    }

    @Override
    protected void makeSureIsReinforced(HasPosition basePosition) {
        (new ReinforceWithBunkerAtNearestChoke(basePosition)).invokeCommander();
    }
}
