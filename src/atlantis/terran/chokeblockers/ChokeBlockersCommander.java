package atlantis.terran.chokeblockers;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.generic.ArmyStrength;
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

        if (A.supplyUsed() <= 13) return false;
        if (A.supplyUsed() >= 45) return false;

        choke = ChokeToBlock.get();
        if (choke == null) return false;

        if (AGame.killsLossesResourceBalance() >= 1800) return false;

        int bunkers = Count.ourWithUnfinished(AUnitType.Terran_Bunker);
//        if (bunkers <= 0) return false;
        if (bunkers >= 3 || (A.seconds() >= 450 && Count.bases() != 1)) return false;

        if (Count.tanks() >= 2) return false;

        int combatUnits = Count.ourCombatUnits();

        if (combatUnits >= 14 && ArmyStrength.ourArmyRelativeStrength() >= 270) return false;

        return Atlantis.KILLED <= 8 || combatUnits <= 18;
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
