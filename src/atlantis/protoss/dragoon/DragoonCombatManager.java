package atlantis.protoss.dragoon;

import atlantis.architecture.Manager;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class DragoonCombatManager extends MobileDetector {
    public DragoonCombatManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isDragoon();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            DragoonTooFarFromReaver.class
//            DragoonLongNotAttackedVZ.class,
//            ProtossDragoonSeparate.class,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_Dragoon;
    }
}
