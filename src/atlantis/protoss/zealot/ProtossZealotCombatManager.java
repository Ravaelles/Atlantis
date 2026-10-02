package atlantis.protoss.zealot;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.generic.MobileDetector;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ZealotStayInBackOfGoon::new,
            ProtossZealotTooFarFromDragoon::new,
            ProtossZealotSeparateFromMeleeEnemies::new,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_Zealot;
    }
}
