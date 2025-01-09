package atlantis.combat.missions.defend.protoss;

import atlantis.architecture.Manager;
import atlantis.information.enemy.OurBuildingUnderAttack;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossForceStickToMainDuringDefend extends Manager {
    public ProtossForceStickToMainDuringDefend(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.cooldown() >= 3
            && ProtossStickCombatToMainBaseEarly.should()
            && OurBuildingUnderAttack.none()
            && unit.distToMain() >= (unit.hasCooldown() ? 6 : 2)
            && (unit.isZealot() || unit.shields() <= 25);
    }

    @Override
    public Manager handle() {
        if (unit.moveToSafety(Actions.MOVE_SAFETY)) {
            return usedManager(this);
        }

        return null;
    }
}
