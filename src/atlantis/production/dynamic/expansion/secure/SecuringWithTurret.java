package atlantis.production.dynamic.expansion.secure;

import atlantis.combat.micro.terran.bunker.position.NewBunkerPositionFinder;
import atlantis.map.position.APosition;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Terran_Missile_Turret;

public class SecuringWithTurret {
    private final SecuringBase securingBase;

    public SecuringWithTurret(SecuringBase securingBase) {
        this.securingBase = securingBase;
    }

    public boolean hasTurretSecuring() {
        APosition position = (new NewBunkerPositionFinder(securingBase.baseToSecure())).find();

        if (position == null) return true;

        return Count.existingOrUnfinishedBuildingsNear(
            Terran_Missile_Turret,
            7,
            position
        ) > 0;
    }
}