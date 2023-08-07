package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.dynamic.DynamicTech;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Count;
import atlantis.util.Enemy;
import bwapi.UpgradeType;

import static bwapi.UpgradeType.Singularity_Charge;


public class ProtossDynamicTechResearch extends Commander {

    public static boolean update() {
        if (A.notNthGameFrame(71)) {
            return false;
        }

        if (singularityCharge()) {
            return true;
        }

        if (Count.zealots() >= 10) {
            return DynamicTech.handleUpgrade(UpgradeType.Leg_Enhancements);
        }
        if (Count.ourCombatUnits() >= 16) {
            return DynamicTech.handleUpgrade(UpgradeType.Protoss_Ground_Weapons);
        }
        else if (Count.ourCombatUnits() >= 25) {
            return DynamicTech.handleUpgrade(UpgradeType.Protoss_Ground_Armor);
        }

        return false;
    }

    private static boolean singularityCharge() {
        if (A.hasGas(180) || Count.dragoons() >= (Enemy.terran() ? 2 : 7)) {
            AddToQueue.upgrade(Singularity_Charge);
            return true;
        }

        return false;
    }

}
