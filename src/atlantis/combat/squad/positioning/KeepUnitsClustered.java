package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.protoss.keep_formation.ProtossKeepFormation;
import atlantis.units.AUnit;

public class KeepUnitsClustered extends Manager {
    public KeepUnitsClustered(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossKeepFormation.class,
        };
    }
}
