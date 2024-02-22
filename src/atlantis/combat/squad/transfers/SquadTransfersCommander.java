package atlantis.combat.squad.transfers;

import atlantis.architecture.Commander;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.beta.Beta;
import atlantis.game.A;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class SquadTransfersCommander extends Commander {

    @Override
    protected void handle() {
        updateSquadTransfers();
    }

    private static boolean updateSquadTransfers() {
        if (shouldHaveBeta()) {
            Beta beta = Beta.get();
            beta.handleReinforcements();
        }
        else {
            removeBeta();
        }

        return false;
    }

    private static boolean shouldHaveBeta() {
        if (EnemyWhoBreachedBase.numberOfAttacksOnBase() > 0) return true;

//        return false;
        return (A.supplyUsed(85) || Missions.isGlobalMissionAttack() || Count.ourCombatUnits() >= 20);
    }

    private static void removeBeta() {
        Alpha alpha = Alpha.get();
        Beta beta = Beta.get();

        for (int i = 0; i < beta.size(); i++) {
            AUnit transfer = beta.get(0);
            (new SquadReinforcements(alpha)).transferUnitToSquad(transfer);
        }
    }

    public static void removeUnitFromSquads(AUnit unit) {
        Squad squad = unit.squad();

        if (squad != null) {
            squad.removeUnit(unit);
            unit.setSquad(null);
        }

        Alpha.get().removeUnit(unit);

//        if (unit.isOur() && unit.isCombatUnit()) {

//        }
    }
}
