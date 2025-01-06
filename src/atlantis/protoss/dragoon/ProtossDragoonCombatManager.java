package atlantis.protoss.dragoon;

import atlantis.architecture.Manager;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.squad.positioning.protoss.dragoon.ProtossDragoonSeparate;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ProtossDragoonCombatManager extends MobileDetector {
    public ProtossDragoonCombatManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isDragoon();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossDragoonLongNotAttacked.class,
            ProtossDragoonSeparate.class,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_Dragoon;
    }
}
