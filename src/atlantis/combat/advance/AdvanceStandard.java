package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.terran.tank.TerranTank;
import atlantis.combat.missions.MissionManager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class AdvanceStandard extends MissionManager {
    public AdvanceStandard(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.squad().isLeader(unit);
    }

    protected Manager handle() {
        if (focusPoint != null) {
            if (unit.isTankSieged()) {
                TerranTank.wantsToUnsiege(unit);
            }
            else {
//                AttackNearbyEnemies attackNearbyEnemies = new AttackNearbyEnemies(unit);
//                if (attackNearbyEnemies.invoke() != null) {
//                    return usedManager(attackNearbyEnemies);
//                }

                unit.move(focusPoint, Actions.MOVE_FOCUS, "Advance");
            }
            return usedManager(this);
        }

        return null;
    }
}
