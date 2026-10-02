package atlantis.protoss.observer;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.avoid.protoss.ProtossAvoidEnemies;
import atlantis.combat.micro.avoid.buildings.protoss.ProtossCombatBuildingClose;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.micro.generic.managers.*;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class Observer extends MobileDetector {
    public Observer(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isObserver();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossCombatBuildingClose::new,
            DetectorAvoidAntiAir::new,
            ProtossAvoidEnemies::new,
//            ProtossObserverAvoidDetectors.class,
            ObserverAvoidEnemyDetectors::new,
            AsThirdObserverScoutEnemyBases::new,
            SpreadOutDetectors::new,
            DetectHiddenEnemyClosestToBase::new,
            DetectHiddenEnemyClosestToAlpha::new,
            DetectNewBasePotentiallyBlocked::new,
            FollowAlphaLeader::new,
            FollowArmy::new,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_Observer;
    }
}
