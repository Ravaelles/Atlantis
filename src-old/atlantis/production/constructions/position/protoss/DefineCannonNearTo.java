package atlantis.production.constructions.position.protoss;

import atlantis.information.strategy.Strategy;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class DefineCannonNearTo {
    public static HasPosition define() {
        int bases = Count.bases();

        HasPosition at = null;

        if (Strategy.get().isExpansion() || bases <= 2) {
            at = atNatural();
            if (at != null) return at;
        }

        if (bases <= 1) {
            at = atMainChoke();
            if (at != null) return at;
        }

        return Select.ourBases().last().translateByTiles(2, 2);
    }

    private static APosition atMainChoke() {
        AChoke mainChoke = Chokes.mainChoke();
        if (mainChoke == null) return null;

        AUnit main = Select.mainOrAnyBuilding();
        if (main == null) return null;

        return mainChoke.translateTilesTowards(8, mainChoke);
    }

    private static HasPosition atNatural() {
        HasPosition cannon = null;

        AChoke natural = Chokes.natural();
        if (natural != null) {
            cannon = Select.ourOfTypeWithUnfinished(AUnitType.Protoss_Photon_Cannon)
                .inRadius(7, natural)
                .nearestTo(natural);
            if (cannon != null) return cannon;

            cannon = ConstructionRequests.nearestOfTypeTo(AUnitType.Protoss_Photon_Cannon, natural, 7);
            if (cannon != null) return cannon;

            return natural;
        }
        return null;
    }
}
