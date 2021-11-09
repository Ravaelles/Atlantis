package atlantis.combat.missions;

import atlantis.strategy.OurStrategy;
import atlantis.units.select.Count;
import atlantis.util.A;
import atlantis.util.Enemy;

public class TerranMissionChangerWhenDefend extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
//        if (TerranMissionChangerWhenContain.shouldChangeMissionToDefend()) {
//            return false;
//        }
        // === Protoss ===================================================

        if (Enemy.protoss()) {
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
