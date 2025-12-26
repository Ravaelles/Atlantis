
package atlantis.combat.micro.terran.vessel;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.protoss.ProtossAvoidEnemies;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.micro.generic.managers.*;
import atlantis.protoss.observer.DetectNewBasePotentiallyBlocked;
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
            DetectorAvoidAntiAir.class,
            UseVesselTechs.class,
            ProtossAvoidEnemies.class,
            DetectHiddenEnemyClosestToBase.class,
            DetectNewBasePotentiallyBlocked.class,
            SpreadOutDetectors.class,
            FollowAlphaLeader.class,
            FollowArmy.class,

//            ProtossAvoidEnemies.class,
//            UnitBeingReparedManager.class,
//            GoToRepairAsAirUnit.class,
//            SpreadOutDetectors.class,
//            DetectHiddenEnemyClosestToBase.class,
//            FollowAlphaLeader.class,
//            FollowArmy.class,
        };
    }

    public AUnitType type() {
        return AUnitType.Terran_Science_Vessel;
    }
}
