package atlantis.combat.squad.mission;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class ChangeSquadToAttack extends ChangeSquadMission {

    public static boolean shouldChangeToAttack(Squad squad) {
        return changeMissionToAttack(squad, "Bravery lol");
    }

    protected static boolean changeMissionToAttack(Squad squad, String reason) {
//        System.err.println("Change SQUAD to ATTACK - " + reason);
        squad.setMission(Missions.ATTACK);
        return true;
    }

}