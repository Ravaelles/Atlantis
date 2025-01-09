package atlantis.production.constructions.position.modifier;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class PositionAtMainChoke {
    public static APosition atMainChoke(Construction construction) {
        if (construction != null) {
            construction.setMaxDistance(9);
        }

        AUnit main = Select.mainOrAnyBuilding();
        AChoke mainChoke = Chokes.mainChoke();
        if (mainChoke != null) {
            return APosition.create(mainChoke.center()).translateTilesTowards(
                main,
                1.2
//                    (mainChoke.width() <= 4 ? 1.7 : )
//                    2.8 + (mainChoke.width() <= 4 ? 1.7 : 0)
            );
        }

        return null;
    }
}
