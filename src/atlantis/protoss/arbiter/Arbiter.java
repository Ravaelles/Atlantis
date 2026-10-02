package atlantis.protoss.arbiter;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            AsAirAvoidTowers::new,
            AsAirAvoidDeadlyAntiAir::new,
            AsAirAvoidAntiAir::new,
            SpreadOutArbiters::new,
            FollowAlphaLeader::new,
            FollowArmy::new,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_Arbiter;
    }

}
