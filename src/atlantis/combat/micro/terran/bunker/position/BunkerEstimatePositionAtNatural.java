package atlantis.combat.micro.terran.bunker.position;

import atlantis.map.base.Bases;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;

public class BunkerEstimatePositionAtNatural {
    public static APosition define() {
        APosition naturalBase = Bases.natural();
        AChoke naturalChoke = Chokes.natural();

        if (naturalBase == null || naturalChoke == null) return null;

        return naturalBase
            .translateTilesTowards(5, naturalChoke)
            .translatePercentTowards(60, naturalBase)
            .translatePercentTowards(60, naturalChoke);
    }
}
