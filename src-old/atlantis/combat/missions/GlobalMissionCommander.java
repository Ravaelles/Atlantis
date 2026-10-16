package atlantis.combat.missions;

import atlantis.architecture.Commander;

public class GlobalMissionCommander extends Commander {

//    @Override
//    protected Class<? extends Commander>[] subcommanders() {
//        return new Class[] {
//            GlobalMissionCommander.class,
//        };
//    }

    @Override
    protected boolean handle() {
        MissionChanger.evaluateGlobalMission();

        // Global mission is de facto Alpha squad's mission
//        Alpha alpha = Alpha.get();
//        if (alpha.mission() == null) {
//            alpha.setMission(Missions.globalMission());
//        }
        return false;
    }

}
