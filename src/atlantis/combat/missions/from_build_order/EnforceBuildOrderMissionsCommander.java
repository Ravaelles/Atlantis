package atlantis.combat.missions.from_build_order;

import atlantis.architecture.Commander;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.game.A;

public class EnforceBuildOrderMissionsCommander extends Commander {
    private static int _lastEnforcedAt = -1;
    private static Mission mission;

    public boolean applies() {
        return DynamicMissionsFromBuildOrder.hasAnyDynamicMission()
            && A.supplyUsed() >= 6
            && (_lastEnforcedAt == -1 || (A.ago(_lastEnforcedAt) <= 30 * 60))
            && (mission = DynamicMissionsFromBuildOrder.defineDynamicMissionsForCurrentSupply(A.supplyUsed())) != null;
    }

    @Override
    protected void handle() {
        if (_lastEnforcedAt == -1) _lastEnforcedAt = A.now();

        Missions.forceGlobalMissionFromBuildOrder(mission, "BuildOrderMission");
    }
}
