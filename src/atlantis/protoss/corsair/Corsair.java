package atlantis.protoss.corsair;

import atlantis.architecture.Manager;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AsAirAvoidTowers.class,
            AsAirAvoidDeadlyAntiAir.class,
            AsAirAvoidAntiAir.class,
            CorsairChangeLocationIfRanTooLong.class,
            CorsairHuntMutas.class,
            CorsairDanceToOverlord.class,
            CorsairHuntOverlords.class,
            CorsairHuntKnownOverlords.class,
            CorsairExploreEnemyMain.class,
            ScoutFreeBases.class,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_Corsair;
    }

}
