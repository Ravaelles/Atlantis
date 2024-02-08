package atlantis.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemiesIfNeeded;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.micro.generic.managers.DetectHiddenEnemyClosestToBase;
import atlantis.combat.micro.generic.managers.FollowAlphaScout;
import atlantis.combat.micro.generic.managers.FollowArmy;
import atlantis.combat.micro.generic.managers.SpreadOutDetectors;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Color;

public class ProtossObserver extends MobileDetector {
    public ProtossObserver(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isObserver()) {
            unit.paintCircleFilled(6, Color.Purple);
        }

        return unit.isObserver();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AvoidEnemiesIfNeeded.class,
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
