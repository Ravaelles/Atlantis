package atlantis.production.dynamic;

import atlantis.architecture.Commander;


public class DynamicUnitsProductionCommander extends Commander {
//    @Override
//    protected Class<? extends Commander>[] subcommanders() {
//        Class[] raceSpecific;
//
//        if (We.terran()) {
//            raceSpecific = new Class[]{
//                TerranDynamicTech.class,
//                TerranDynamicUnitsCommander.class,
//                TerranDynamicBuildingsCommander.class,
//            };
//        }
//        else if (We.protoss()) {
//            raceSpecific = new Class[]{
//                ProtossDynamicTech.class,
//                ProtossDynamicUnitsManager.class,
//                ProtossDynamicBuildingsCommander.class,
//            };
//        }
//        else {
//            raceSpecific = new Class[]{
//                ZergDynamicTech.class,
//                ZergDynamicUnitsCommander.class,
//                ZergDynamicBuildingsCommander.class,
//            };
//        }
//
//        Class[] generic = new Class[]{
//            DynamicTrainWorkersCommander.class
//        };
//
//        return mergeCommanders(raceSpecific, generic);
//    }
}
