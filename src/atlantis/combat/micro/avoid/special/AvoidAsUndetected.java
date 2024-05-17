package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.special.protoss.ProtossObserverAvoidDetectors;
import atlantis.units.AUnit;

public class AvoidAsUndetected extends Manager {
    public AvoidAsUndetected(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossObserverAvoidDetectors.class,
        };
    }
}
