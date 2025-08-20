package atlantis.protoss.arbiter;

import atlantis.architecture.Manager;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.micro.generic.managers.*;
import atlantis.map.scout.ScoutFreeBases;
import atlantis.protoss.corsair.*;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class Arbiter extends MobileDetector {
    public Arbiter(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.is(type());
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AsAirAvoidTowers.class,
            AsAirAvoidDeadlyAntiAir.class,
            AsAirAvoidAntiAir.class,
            SpreadOutArbiters.class,
            FollowAlphaLeader.class,
            FollowArmy.class,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_Arbiter;
    }

}
