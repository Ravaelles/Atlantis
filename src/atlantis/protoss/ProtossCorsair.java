package atlantis.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.micro.generic.managers.*;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ProtossCorsair extends MobileDetector {
    public ProtossCorsair(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCorsair();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AsAirAvoidAntiAir.class,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_Corsair;
    }

}
