package atlantis.production.dynamic.expansion.secure;

import atlantis.combat.micro.terran.bunker.position.NewBunkerPositionFinder;
import atlantis.map.base.IsNatural;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class SecuringWithBunker {
    private final SecuringBase securingBase;
    private NewBunkerPositionFinder newBunkerPositionFinder;

    public SecuringWithBunker(SecuringBase securingBase) {
        this.securingBase = securingBase;
        newBunkerPositionFinder = (new NewBunkerPositionFinder(securingBase.baseToSecure()));
    }

    public boolean hasBunkerSecuring() {
        APosition bunkerPosition = newBunkerPositionFinder.find();

        if (bunkerPosition == null) return false;

        return Count.existingOrUnfinishedBuildingsNear(
//            AUnitType.Terran_Bunker, 8, securingBase.getBaseToSecure()
            AUnitType.Terran_Bunker, 10, bunkerPosition
        ) >= expectedNumOfBunkers();
    }

    private int expectedNumOfBunkers() {
        return IsNatural.isPositionNatural(securingBase.baseToSecure().position()) ? 2 : 1;
    }
}