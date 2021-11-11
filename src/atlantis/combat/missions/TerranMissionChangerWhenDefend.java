package atlantis.combat.missions;

import atlantis.Atlantis;
import atlantis.strategy.OurStrategy;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
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
        if (shouldDefendMainBase()) {
            return false;
        }

//        if (TerranMissionChangerWhenContain.shouldChangeMissionToDefend()) {
//            return false;
//        }
        // === Protoss ===================================================

        if (Enemy.protoss()) {
//            if (Missions.isFirstMission()) {
            if (Atlantis.LOST <= 5) {
                return Count.ourCombatUnits() >= 4;
            }

            return Count.ourCombatUnits() >= 15 || Count.tanks() >= 2;
        }

        // === Zerg ======================================================

        else if (Enemy.zerg()) {
            if (Missions.isFirstMission()) {
                if (OurStrategy.get().isRush()) {
                    return Count.ourCombatUnits() >= 12 || A.resourcesBalance() >= 350;
                }
            }

    //        return Count.ourCombatUnits() >= Math.max(24, 12 + Missions.counter());
            return Count.ourCombatUnits() >= 15 || Count.tanks() >= 2;
        }

        // =========================================================

        return Count.ourCombatUnits() >= 15 || Count.tanks() >= 2;
    }

}
