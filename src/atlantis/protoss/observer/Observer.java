package atlantis.protoss.observer;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.buildings.AvoidCombatBuildingClose;
import atlantis.combat.micro.avoid.special.protoss.ProtossObserverAvoidDetectors;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AvoidCombatBuildingClose.class,
            DetectorAvoidAntiAir.class,
            AvoidEnemies.class,
//            ProtossObserverAvoidDetectors.class,
            ObserverAvoidEnemyDetectors.class,
            AsThirdObserverScoutEnemyBases.class,
            SpreadOutDetectors.class,
            DetectHiddenEnemyClosestToBase.class,
            DetectHiddenEnemyClosestToAlpha.class,
            DetectNewBasePotentiallyBlocked.class,
            FollowAlphaLeader.class,
            FollowArmy.class,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_Observer;
    }
}
