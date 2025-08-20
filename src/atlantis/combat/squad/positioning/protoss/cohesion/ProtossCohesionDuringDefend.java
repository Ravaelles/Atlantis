package atlantis.combat.squad.positioning.protoss.cohesion;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.We;

public class ProtossCohesionDuringDefend extends ProtossCohesion {
    public ProtossCohesionDuringDefend(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.protoss()) return false;
        if (Count.ourCombatUnits() <= 5) return false;

        return super.applies();
    }
}
