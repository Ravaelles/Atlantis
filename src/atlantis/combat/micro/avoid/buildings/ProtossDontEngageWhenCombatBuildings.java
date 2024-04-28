package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ProtossDontEngageWhenCombatBuildings extends Manager {
    public ProtossDontEngageWhenCombatBuildings(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.protoss()) return false;
        if (unit.distToBase() <= 30) return false;
        if (!unit.isMissionAttack()) return false;

        if (unit.lastRetreatedAgo() <= 30 * 12) return true;
        
        if (unit.squadSize() >= 20) return false;
        if (unit.combatEvalRelative() >= 5) return false;

        return EnemyCombatBuildingsTooStrong.tooStrong(unit);
    }

    @Override
    public Manager handle() {
        AUnit enemyCB = nearestCB();
        if (enemyCB == null) return null;

        double dist = unit.distTo(enemyCB);
        if (dist <= 9) {
            if (unit.moveToMain(Actions.MOVE_AVOID) && unit.addLog("CarefulCB")) return usedManager(this);
        }
        else if (dist <= 11 && !unit.isAttacking()) {
            if (unit.holdPosition("HoldCB") && unit.addLog("HoldCB")) return usedManager(this);
        }

        return null;
    }

    private AUnit nearestCB() {
        return unit.enemiesNear().combatBuildingsAnti(unit).nearestTo(unit);
    }
}
