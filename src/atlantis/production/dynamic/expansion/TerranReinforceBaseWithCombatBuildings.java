package atlantis.production.dynamic.expansion;

import atlantis.combat.micro.terran.TerranBunkersInMain;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.dynamic.terran.reinforce.ReinforceWithBunkerAtNearestChoke;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Terran_Bunker;

public class TerranReinforceBaseWithCombatBuildings extends ReinforceBaseWithCombatBuildings {
    private APosition basePosition;

    public TerranReinforceBaseWithCombatBuildings(APosition basePosition) {
        this.basePosition = basePosition;
    }

    @Override
    public boolean applies() {
        if (Count.inProductionOrInQueue(Terran_Bunker) > 0) return false;

        if (basePosition != null) {
            return Select.ourOfType(Terran_Bunker).inRadius(15, basePosition).empty();
        }

        return true;
    }

    @Override
    protected void makeSureIsReinforced(APosition basePosition) {
        if (A.everyNthGameFrame(67)) {
            (new ReinforceWithBunkerAtNearestChoke(basePosition)).handle();
        }


        if (A.everyNthGameFrame(77)) {
            (new TerranBunkersInMain()).handle();
//            ((TerranBunker) TerranBunker.get()).handleDefensiveBunkers();
        }
        if (A.everyNthGameFrame(71)) {
            (new TerranMissileTurretsForMain()).buildIfNeeded();
        }
        if (A.everyNthGameFrame(73)) {
            (new TerranMissileTurretsForNonMain()).buildIfNeeded();
        }
    }
}
