package atlantis.protoss.observer;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.micro.generic.managers.*;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ProtossObserver extends MobileDetector {
    public ProtossObserver(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isObserver();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            DetectorAvoidAntiAir.class,
            AvoidEnemies.class,
            AsThirdObserverScoutEnemyBases.class,
            DetectHiddenEnemyClosestToBase.class,
            DetectNewBasePotentiallyBlocked.class,
            SpreadOutDetectors.class,
            FollowAlphaLeader.class,
            FollowArmy.class,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_Observer;
    }

}
