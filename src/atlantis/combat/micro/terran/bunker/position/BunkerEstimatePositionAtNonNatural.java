package atlantis.combat.micro.terran.bunker.position;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Select;

public class BunkerEstimatePositionAtNonNatural {
    public static APosition define(HasPosition positionToSecure) {
        if (positionToSecure == null) positionToSecure = Select.ourBases().last();

        AChoke choke;
        if ((choke = Chokes.nearestChoke(positionToSecure)) != null) {
            return positionToSecure.translateTilesTowards(3, choke);
        }

        return positionToSecure.translateByTiles(3, 1);
//        return Select.ourBasesWithUnfinished().last();
    }
}
