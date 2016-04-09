package atlantis.buildings.managers;
//
//    public static void update(AUnit base) {
//
//        // Train new workers if allowed
////        if (AtlantisWorkerCommander.shouldTrainWorkers(true)) {
////            if (hasSlotToProduceUnit(base)) {
////                base.train(AtlantisConfig.WORKER);
////            }
////        }
//    }
//    
//    // =========================================================
//
//    private static boolean hasSlotToProduceUnit(AUnit base) {
//        if (AtlantisGame.getSupplyFree() == 0) {
//            return false;
//        }
//        
//        if (AtlantisGame.playsAsZerg()) {
//            return !base.getLarva().isEmpty();
//        }
//        else {
//            return base.getTrainingQueue().isEmpty();
//        }
//    }
//
//}
