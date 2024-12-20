package atlantis.production.constructing.position.protoss;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class DefineCannonNearTo {
    public static HasPosition define() {
        if (Count.bases() <= 1) {
            AChoke mainChoke = Chokes.mainChoke();
            if (mainChoke == null) return null;

            AUnit main = Select.mainOrAnyBuilding();
            if (main == null) return null;

            return main.translatePercentTowards(50, mainChoke);
        }

        return Select.ourBases().last();
    }
}
