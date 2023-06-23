package atlantis.combat.squad.mission;

import atlantis.combat.missions.Mission;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;

public class ChangeSquadToDefault extends ChangeSquadMission {

    public static boolean shouldChangeToDefault(Squad squad) {
        Mission defaultMission = defaultMission();


        if (A.supplyUsed() <= 193) {
            if (
                defaultMission.isMissionAttackOrContain()
                    && GamePhase.isEarlyGame()
                    && EnemyStrategy.get().isRushOrCheese()
            ) {
                return false;
            }
        }

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
