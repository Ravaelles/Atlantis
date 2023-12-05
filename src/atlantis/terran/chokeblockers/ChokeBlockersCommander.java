package atlantis.terran.chokeblockers;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

public class ChokeBlockersCommander extends Commander {
    private AChoke choke;

    @Override
    public boolean applies() {
        if (Enemy.terran()) return false;

        return checkIfApplies();
    }

    private boolean checkIfApplies() {
        if (AGame.notNthGameFrame(5)) return false;
        if (Missions.isGlobalMissionAttack()) return false;

        choke = Chokes.mainChoke();
        if (choke == null || choke.width() >= 4.5) return false;

        if (AGame.killsLossesResourceBalance() >= 1800) return false;

        int bunkers = Count.ourWithUnfinished(AUnitType.Terran_Bunker);
        if (bunkers <= 0 || bunkers >= 3 || Count.bases() != 1) return false;

        if (Count.tanks() >= 2) return false;

        return Atlantis.KILLED <= 7 || Count.ourCombatUnits() <= 18;
    }

    @Override
    protected void handle() {
        ChokeBlockers chokeBlockers = ChokeBlockers.get();

        chokeBlockers.assignWorkersIfNeeded();

        actWithWorker(chokeBlockers.worker1);
        actWithWorker(chokeBlockers.worker2);
    }

    private void actWithWorker(AUnit unit) {
        if (unit != null && unit.isAlive()) {
            (new ChokeBlockerManager(unit)).invoke();
        }
    }
}
