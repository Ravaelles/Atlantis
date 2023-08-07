package atlantis.production.dynamic.expansion;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Terran_Bunker;

public class TerranReinforceNewBaseWithCombatBuilding extends ReinforceNewBaseWithCombatBuilding {
    private APosition basePosition;

    public TerranReinforceNewBaseWithCombatBuilding() {
    }

    @Override
    public boolean applies() {
        if (Count.inProductionOrInQueue(Terran_Bunker) > 0) return false;

        if (basePosition != null) {
            return Select.ourOfType(Terran_Bunker).inRadius(7, basePosition).empty();
        }

        return true;
    }

    @Override
    protected void makeSureIsReinfoced(APosition basePosition) {
        this.basePosition = basePosition;

        if (!applies()) return;

        APosition bunkerPosition = basePosition;

        System.err.println("Main position: " + Select.main());
        System.out.println("this basePosition = " + basePosition);

        AChoke choke = Chokes.nearestChoke(basePosition);
        System.out.println("choke = " + choke);
        if (choke != null) {
            bunkerPosition = basePosition.translateTilesTowards(choke, 5);
        }

        System.err.println("Reinforcing base with bunker at " + bunkerPosition);

        AddToQueue.withTopPriority(Terran_Bunker, bunkerPosition);
    }
}
