package atlantis.protoss.zealot;

import atlantis.architecture.Manager;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.squad.positioning.protoss.zealot.ProtossZealotSeparateFromMeleeEnemies;
import atlantis.combat.squad.positioning.protoss.zealot.ProtossZealotTooFarFromDragoon;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ProtossZealotCombatManager extends MobileDetector {
    public ProtossZealotCombatManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isZealot();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossZealotLongNotAttacked.class,
            ProtossZealotTooFarFromDragoon.class,
            ProtossZealotSeparateFromMeleeEnemies.class,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_Zealot;
    }
}
