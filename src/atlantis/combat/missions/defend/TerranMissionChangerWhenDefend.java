package atlantis.combat.missions.defend;

import atlantis.Atlantis;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.enemy.EnemyInformation;
import atlantis.strategy.OurStrategy;
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

    public static boolean shouldChangeMissionToContain() {
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
            if (Atlantis.LOST <= 2) {
                return true;
            }

//            return A.supplyUsed(140);

            if (A.resourcesBalance() >= 250) {
                return true;
            }

            if (Missions.counter() <= 3) {
                if (OurStrategy.get().isRush()) {
                    return Count.ourCombatUnits() >= 2;
                }
            }
//
//            return Count.ourCombatUnits() >= Math.max(24, 12 + Missions.counter());
            return Count.ourCombatUnits() >= 13 || Count.tanks() >= 3;
        }

        // =========================================================

        return Count.ourCombatUnits() >= 15 || Count.tanks() >= 2;
    }

}
