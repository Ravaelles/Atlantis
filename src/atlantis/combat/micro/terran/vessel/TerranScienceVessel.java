
package atlantis.combat.micro.terran.vessel;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            DetectorAvoidAntiAir::new,
            UseVesselTechs::new,
            ProtossAvoidEnemies::new,
            DetectHiddenEnemyClosestToBase::new,
            DetectNewBasePotentiallyBlocked::new,
            SpreadOutDetectors::new,
            FollowAlphaLeader::new,
            FollowArmy::new,

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
