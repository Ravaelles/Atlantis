package atlantis.production.dynamic.expansion.secure;

import atlantis.map.base.Bases;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.base.NextBasePosition;

public class SecuringWithBunkerPosition {
    public static APosition bunkerPosition() {
        APosition basePosition = NextBasePosition.nextBasePosition();
        APosition natural = Bases.natural();

        if (basePosition == null || natural == null) return null;

        if (basePosition.distTo(natural) <= 9) {
            return forNatural(basePosition, natural);
        }

        return forNonNatural(basePosition);
    }

    public static APosition forNatural(HasPosition basePosition, APosition natural) {
        AChoke naturalChoke = Chokes.natural();

        return basePosition
            .translateTilesTowards(5, naturalChoke)
            .translatePercentTowards(50, natural)
            .translatePercentTowards(50, naturalChoke);
    }

    public static APosition forNonNatural(HasPosition basePosition) {
        AChoke choke;
        if ((choke = Chokes.nearestChoke(basePosition)) != null) {
            return basePosition.translateTilesTowards(3, choke);
        }

        return basePosition.translateByTiles(-3, 1);
    }
}
