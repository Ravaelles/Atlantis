package atlantis.combat.micro.terran.bunker.position;

import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.production.dynamic.expansion.ExpansionCommander;
import atlantis.production.dynamic.expansion.terran.TerranShouldExpand;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Terran_Bunker;

public class ShouldPlaceBunkerAtNatural {
    public static boolean shouldPlaceAtNatural() {
        if (!TerranShouldExpand.shouldExpand()) return false;

        AChoke naturalChoke = Chokes.natural();
        HasPosition naturalBase = DefineNaturalBase.natural();

        return naturalBase != null
            && naturalChoke != null
            && Count.existingOrPlannedBuildingsNear(Terran_Bunker, 8, naturalBase) <= 1;
    }
}
