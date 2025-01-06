package atlantis.production.constructing.position.protoss;

import atlantis.information.strategy.OurStrategy;
import atlantis.map.base.Bases;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class DefineCannonNearTo {
    public static HasPosition define() {
        int bases = Count.bases();

        if (OurStrategy.get().isExpansion() || bases <= 2) {
            AChoke natural = Chokes.natural();
            if (natural != null) {
                AUnit cannon =
                    Select.ourOfTypeWithUnfinished(AUnitType.Protoss_Photon_Cannon)
                        .inRadius(8, natural)
                        .nearestTo(natural);
                if (cannon != null) return cannon;

                return natural;
            }
        }

        if (bases <= 1) {
            AChoke mainChoke = Chokes.mainChoke();
            if (mainChoke == null) return null;

            AUnit main = Select.mainOrAnyBuilding();
            if (main == null) return null;

            return main.translatePercentTowards(50, mainChoke);
        }

        return Select.ourBases().last().translateByTiles(2, 2);
    }
}
