package atlantis.combat.micro.terran.bunker.position;

import atlantis.map.base.define.DefineNatural;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Terran_Bunker;

public class ShouldPlaceAtNatural {
    public static boolean shouldPlaceAtNatural() {
        AChoke naturalChoke = Chokes.natural();
        HasPosition naturalBase = DefineNatural.natural();

        return naturalBase != null
            && naturalChoke != null
            && Count.existingOrPlannedBuildingsNear(Terran_Bunker, 8, naturalBase) <= 1;
    }
}
