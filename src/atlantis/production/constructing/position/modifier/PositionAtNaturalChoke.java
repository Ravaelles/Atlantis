package atlantis.production.constructing.position.modifier;

import atlantis.map.base.ABaseLocation;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructing.Construction;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class PositionAtNaturalChoke {
    public static APosition atNaturalChoke(Construction construction) {
        if (construction != null) {
            construction.setMaxDistance(9);
        }

        AUnit main = Select.mainOrAnyBuilding();

        AChoke chokepointForNatural = Chokes.natural();
        if (chokepointForNatural != null && main != null) {
            ABaseLocation natural = DefineNaturalBase.naturalIfMainIsAt(main.position());
//                return APosition.create(chokepointForNatural.center()).translateTilesTowards(natural, 5);
//            return natural.translateTilesTowards(chokepointForNatural.center(), 8);
            return natural.translatePercentTowards(chokepointForNatural.center(), 75);
        }

        return null;
    }
}
