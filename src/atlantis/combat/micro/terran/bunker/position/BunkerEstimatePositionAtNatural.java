package atlantis.combat.micro.terran.bunker.position;

import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.select.Select;

public class BunkerEstimatePositionAtNatural {
    public static APosition define() {
        APosition naturalBase = DefineNaturalBase.natural();
        AChoke naturalChoke = Chokes.natural();

        if (naturalBase == null || naturalChoke == null) return Select.ourBasesWithUnfinished().last().position();

        return naturalBase
            .translateTilesTowards(5, naturalChoke)
            .translatePercentTowards(60, naturalBase)
            .translatePercentTowards(40, naturalChoke);
    }
}
