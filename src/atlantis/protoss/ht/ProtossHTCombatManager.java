package atlantis.protoss.ht;

import atlantis.architecture.Manager;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.squad.positioning.protoss.zealot.ProtossHtSeparateFromEnemies;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ProtossHTCombatManager extends MobileDetector {
    public ProtossHTCombatManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isZealot();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossHtSeparateFromEnemies.class,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_High_Templar;
    }
}
