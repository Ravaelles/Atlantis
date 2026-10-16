package atlantis.protoss.observer;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.battle.avoid.protoss.ProtossAvoidEnemies;
import atlantis.battle.detectors.MobileDetector;
import atlantis.battle.detectors.*;
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
            // BATTLE CUTOVER: building-close avoid (deleted) - generic avoids cover.
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
