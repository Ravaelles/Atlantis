package atlantis.combat.missions;

import atlantis.Atlantis;
import atlantis.enemy.EnemyInformation;
import atlantis.units.select.Count;
import atlantis.util.A;
import atlantis.util.Enemy;

public class TerranMissionChangerWhenDefend extends MissionChanger {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        if (EnemyInformation.isEnemyNearAnyOurBuilding()) {
            return false;
        }

//        if (TerranMissionChangerWhenContain.shouldChangeMissionToDefend()) {
//            return false;
//        }
        // === Protoss ===================================================

        if (Enemy.protoss()) {
//            if (Missions.isFirstMission()) {
            if (Atlantis.LOST <= 3 && Count.ourCombatUnits() >= 4) {
                return true;
            }

            return Count.ourCombatUnits() >= 15 || Count.tanks() >= 2;
        }

        // === Zerg ======================================================

        else if (Enemy.zerg()) {
//            if (Atlantis.LOST <= 3) {
//                return true;
//            }

            return A.supplyUsed(140);

//            if (Missions.counter() <= 2) {
//                if (OurStrategy.get().isRush()) {
//                    return Count.ourCombatUnits() >= 12 || A.resourcesBalance() >= 350;
//                }
//            }
//
//    //        return Count.ourCombatUnits() >= Math.max(24, 12 + Missions.counter());
//            return Count.ourCombatUnits() >= 25 || Count.tanks() >= 3;
        }

        // =========================================================

        return Count.ourCombatUnits() >= 15 || Count.tanks() >= 2;
    }

}
