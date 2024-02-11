package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.combat.missions.Missions;
import atlantis.decions.Decision;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.range.OurDragoonWeaponRange;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

import static bwapi.UpgradeType.Singularity_Charge;

public class SingularityCharge extends Commander {
    private boolean isResearched = false;

    private int dragoons;

    @Override
    public boolean applies() {
        if (isResearched) return false;

        if (CountInQueue.count(Singularity_Charge, 10) > 0) return false;

        if (ATech.isResearched(Singularity_Charge)) {
            OurDragoonWeaponRange.onSingularityChargeResearched();
            isResearched = true;
            return false;
        }

        dragoons = Count.dragoons();

        Decision decision;

        if ((decision = againstProtoss()).notIndifferent()) return decision.toBoolean();

        if (!A.hasMinerals(300) && ArmyStrength.ourArmyRelativeStrength() <= 80) return false;

        if (A.hasGas(150) && (A.hasGas(260) || dragoons >= 3)) return true;

        return false;
    }

    private Decision againstProtoss() {
        int minDragoons = Missions.isGlobalMissionSparta() ? 2 : 4;

        if (Enemy.protoss()) {
            if (dragoons <= minDragoons) return Decision.FALSE;
            if (dragoons >= minDragoons + 2) return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    @Override
    protected void handle() {
        AddToQueue.upgrade(Singularity_Charge);
    }
}
