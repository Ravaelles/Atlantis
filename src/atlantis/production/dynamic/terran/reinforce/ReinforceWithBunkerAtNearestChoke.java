package atlantis.production.dynamic.terran.reinforce;

import atlantis.architecture.Commander;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Terran_Bunker;

public class ReinforceWithBunkerAtNearestChoke extends Commander {
    private APosition positionToReinforce;
    private APosition positionForBunker;

    public ReinforceWithBunkerAtNearestChoke(APosition position) {
        this.positionToReinforce = position;
    }

    @Override
    public boolean applies() {
        positionForBunker = positionToReinforce;
        AChoke choke = Chokes.nearestChoke(positionToReinforce);
        if (choke != null) {
            positionForBunker = choke.translateTilesTowards(positionToReinforce, 5);
        }

        if (Select.ourOfType(Terran_Bunker).inRadius(7, positionForBunker).count() > 0) return false;

        return positionForBunker != null;
    }

    @Override
    public void handle() {
        if (applies()) {
            haveBunkerAtTheNearestChoke();
        }
    }

    private void haveBunkerAtTheNearestChoke() {
        AddToQueue.withTopPriority(Terran_Bunker, positionForBunker);
    }
}