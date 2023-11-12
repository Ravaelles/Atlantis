package atlantis.production.dynamic.expansion.secure;

import atlantis.combat.micro.terran.bunker.position.NewBunkerPositionFinder;
import atlantis.map.base.IsNatural;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class SecuringWithBunker {
    private final SecuringBase securingBase;

    public SecuringWithBunker(SecuringBase securingBase) {
        this.securingBase = securingBase;
    }

    public boolean hasBunkerSecuring() {
        return Count.existingOrUnfinishedBuildingsNear(
//            AUnitType.Terran_Bunker, 8, securingBase.getBaseToSecure()
            AUnitType.Terran_Bunker, 8, (new NewBunkerPositionFinder(securingBase.baseToSecure())).find()
        ) >= expectedNumOfBunkers();
    }

    private int expectedNumOfBunkers() {
        return IsNatural.isPositionNatural(securingBase.baseToSecure().position()) ? 2 : 1;
    }
}