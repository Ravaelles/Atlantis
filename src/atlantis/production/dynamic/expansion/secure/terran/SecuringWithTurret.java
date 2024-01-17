package atlantis.production.dynamic.expansion.secure.terran;

import atlantis.combat.micro.terran.bunker.position.NewBunkerPositionFinder;
import atlantis.map.position.APosition;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Terran_Missile_Turret;

public class SecuringWithTurret {
    private final SecuringBaseAsTerran securingBase;

    public SecuringWithTurret(SecuringBaseAsTerran securingBase) {
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