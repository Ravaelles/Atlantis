package atlantis.combat.siting;

import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Terran_Bunker;

public class ShouldPlaceBunkerAtNatural {
    public static boolean shouldPlaceAtNatural() {
        // TERRAN EXCLUDED (owner 2026-10-28): race out of refactor scope, dormant under D8.
        return false;

//        AChoke naturalChoke = Chokes.natural();
//        HasPosition naturalBase = DefineNaturalBase.natural();
//
//        return naturalBase != null
//            && naturalChoke != null
//            && Count.existingOrPlannedBuildingsNear(Terran_Bunker, 8, naturalBase) <= 1;
    }
}
