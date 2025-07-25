package atlantis.combat.missions.defend.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.information.enemy.OurBuildingUnderAttack;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossForceStickToMainDuringDefend extends Manager {
    public ProtossForceStickToMainDuringDefend(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (Missions.isGlobalMissionSparta()) return false;

        return unit.cooldown() >= 8
            && ProtossStickCombatToMainBaseEarly.should()
            && OurBuildingUnderAttack.none()
            && unit.distToMain() >= (unit.cooldown() >= 8 ? 5 : 7)
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
