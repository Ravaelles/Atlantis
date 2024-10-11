package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ReaverProduceScarab extends Manager {
    public ReaverProduceScarab(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isStartingAttack()) return false;
        if (unit.isAttackFrame()) return false;

        return unit.scarabCount() <= 3 && A.hasMinerals(20) && !unit.isTrainingAnyUnit();
    }

    @Override
    public Manager handle() {
        if (unit.u().train(AUnitType.Protoss_Scarab.ut())) {
            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - ProduceScarab");
            return usedManager(this);
        }

        return null;
    }
}
