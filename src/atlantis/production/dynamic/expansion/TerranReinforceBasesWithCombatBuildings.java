package atlantis.production.dynamic.expansion;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.dynamic.terran.reinforce.ReinforceWithBunkerAtNearestChoke;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Terran_Bunker;

public class TerranReinforceBasesWithCombatBuildings extends ReinforceBasesWithCombatBuildings {
    public TerranReinforceBasesWithCombatBuildings() {
    }

    @Override
    public boolean applies() {
        if (!super.applies()) return false;

        if (Count.inProductionOrInQueue(Terran_Bunker) > 0) return false;

        return true;
    }

    @Override
    protected void makeSureIsReinforced(HasPosition basePosition) {
        if (A.everyNthGameFrame(67)) {
            (new ReinforceWithBunkerAtNearestChoke(basePosition)).invoke();
        }
    }
}
