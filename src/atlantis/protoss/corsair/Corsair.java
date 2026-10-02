package atlantis.protoss.corsair;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.micro.generic.managers.*;
import atlantis.map.scout.ScoutFreeBases;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class Corsair extends MobileDetector {
    public Corsair(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCorsair();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            AsAirAvoidTowers::new,
            CorsairAvoidDeadlyAntiAir::new,
            AsAirAvoidAntiAir::new,
            CorsairChangeLocationIfRanTooLong::new,
            CorsairHuntMutas::new,
            CorsairDanceToOverlord::new,
            CorsairHuntOverlords::new,
            CorsairSeparate::new,
            CorsairHuntKnownOverlords::new,
            CorsairExploreEnemyMain::new,
            ScoutFreeBases::new,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_Corsair;
    }

}
