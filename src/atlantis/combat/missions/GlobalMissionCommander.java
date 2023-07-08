package atlantis.combat.missions;

import atlantis.architecture.Commander;
import atlantis.combat.squad.alpha.Alpha;

public class GlobalMissionCommander extends Commander {

//    @Override
//    protected Class<? extends Commander>[] subcommanders() {
//        return new Class[] {
//            GlobalMissionCommander.class,
//        };
//    }

    @Override
    public void handle() {
        MissionChanger.evaluateGlobalMission();

        // Global mission is de facto Alpha squad's mission
        Alpha alpha = Alpha.get();
        if (alpha.mission() == null) {
            alpha.setMission(Missions.globalMission());
        }
    }

}
