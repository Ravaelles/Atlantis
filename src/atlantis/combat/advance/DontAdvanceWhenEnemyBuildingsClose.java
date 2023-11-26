package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.TerranTank;
import atlantis.combat.micro.terran.tank.sieging.ForceSiege;
import atlantis.combat.missions.MissionManager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

public class DontAdvanceWhenEnemyBuildingsClose extends MissionManager {
    public DontAdvanceWhenEnemyBuildingsClose(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return A.supplyUsed() < 185
            && A.minerals() < 2200
//            && !unit.isRunning()
            && unit.isCombatUnit()
//            && !unit.isTank()
            && Count.tanks() >= 2
            && EnemyUnits.discovered().buildings().inRadius(8.7, unit).notEmpty();
    }

    protected Manager handle() {
//        System.err.println("@ " + A.now() + " - DontAdvanceWhenEnemyBuildingsClose " + unit);

        if (unit.isTank()) return asTank();

        return asNonTank();
    }

    private Manager asNonTank() {
        unit.holdPosition("Steady");
        return usedManager(this);
    }

    private Manager asTank() {
        if (unit.hasSiegedOrUnsiegedRecently()) return null;

        if (unit.isTankUnsieged()) {
            ForceSiege.forceSiegeNow(this, "RemainHere");
            return usedManager(this);
        }

        return null;
    }
}
