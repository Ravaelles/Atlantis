package atlantis.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.generic.MobileDetector;
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
    protected Manager handle() {
        return super.handle();
    }

    public AUnitType type() {
        return AUnitType.Protoss_Observer;
    }

}
