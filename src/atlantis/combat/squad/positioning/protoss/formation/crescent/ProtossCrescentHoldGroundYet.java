package atlantis.combat.squad.positioning.protoss.formation.crescent;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossCrescentHoldGroundYet extends Manager {
    public ProtossCrescentHoldGroundYet(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false;
        return unit.lastActionMoreThanAgo(2, Actions.ATTACK_UNIT)
            && unit.enemiesICanAttack(0.03).empty();
    }

    @Override
    public Manager handle() {
        unit.holdPosition("HoldGround");
        return usedManager(this);
    }
}
