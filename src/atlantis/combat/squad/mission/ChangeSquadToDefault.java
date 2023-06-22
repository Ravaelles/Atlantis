package atlantis.combat.squad.mission;

import atlantis.combat.missions.Mission;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;

public class ChangeSquadToDefault extends ChangeSquadMission {

    public static boolean shouldChangeToDefault(Squad squad) {
        if (EnemyStrategy.get().isRushOrCheese() && GamePhase.isEarlyGame()) {
            return false;
        }
//        Mission defaultMission = defaultMission();

        return changeMissionToMainMission(squad, "Be good soldier");
    }

    protected static boolean changeMissionToMainMission(Squad squad, String reason) {
        squad.setMission(defaultMission());
        return true;
    }

    private static Mission defaultMission() {
        return Alpha.get().mission();
    }

}