package atlantis.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.micro.generic.managers.DetectHiddenEnemyClosestToBase;
import atlantis.combat.micro.generic.managers.FollowAlphaScout;
import atlantis.combat.micro.generic.managers.FollowArmy;
import atlantis.combat.micro.generic.managers.SpreadOutDetectors;
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
            AvoidEnemies.class,
            DetectHiddenEnemyClosestToBase.class,
            SpreadOutDetectors.class,
            FollowAlphaScout.class,
            FollowArmy.class,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_Observer;
    }

}
