
package atlantis.combat.micro.terran.vessel;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.micro.generic.managers.DetectHiddenEnemyClosestToBase;
import atlantis.combat.micro.generic.managers.FollowAlphaScout;
import atlantis.combat.micro.generic.managers.FollowArmy;
import atlantis.combat.micro.generic.managers.SpreadOutDetectors;
import atlantis.terran.repair.managers.GoToRepairAsAirUnit;
import atlantis.terran.repair.managers.UnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class TerranScienceVessel extends MobileDetector {
    public TerranScienceVessel(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isScienceVessel();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            UseVesselTechs.class,
            AvoidEnemies.class,
            UnitBeingReparedManager.class,
            GoToRepairAsAirUnit.class,
            SpreadOutDetectors.class,
            DetectHiddenEnemyClosestToBase.class,
            FollowAlphaScout.class,
            FollowArmy.class,
        };
    }

    public AUnitType type() {
        return AUnitType.Terran_Science_Vessel;
    }
}
