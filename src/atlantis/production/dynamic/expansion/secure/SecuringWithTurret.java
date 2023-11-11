package atlantis.production.dynamic.expansion.secure;

import atlantis.combat.micro.terran.bunker.position.NewBunkerPositionFinder;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Terran_Missile_Turret;

public class SecuringWithTurret {
    private final SecuringBase securingBase;

    public SecuringWithTurret(SecuringBase securingBase) {
        this.securingBase = securingBase;
    }

    public boolean hasTurretSecuring() {
        return Count.existingOrUnfinishedBuildingsNear(
            Terran_Missile_Turret,
            7,
            (new NewBunkerPositionFinder(securingBase.baseToSecure())).find()
        ) > 0;
    }
}