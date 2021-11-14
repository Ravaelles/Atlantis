package atlantis.combat.missions;

import atlantis.AGame;

public class AMissionManager {

    public static void updateGlobalMission() {
        if (AGame.everyNthGameFrame(90)) {
            MissionChanger.evaluateGlobalMission();
        }
    }

}
