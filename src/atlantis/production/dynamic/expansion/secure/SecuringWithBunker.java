package atlantis.production.dynamic.expansion.secure;

import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class SecuringWithBunker {
    private final SecuringBase securingBase;

    public SecuringWithBunker(SecuringBase securingBase) {
        this.securingBase = securingBase;
    }

    public boolean hasBunkerSecuring() {
        return Count.existingOrUnfinishedBuildingsNear(AUnitType.Terran_Bunker, 8, securingBase.getBaseToSecure()) > 0;
    }
}