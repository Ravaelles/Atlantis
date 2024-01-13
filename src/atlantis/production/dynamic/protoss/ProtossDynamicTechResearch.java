package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.production.dynamic.DynamicTech;
import atlantis.production.dynamic.protoss.tech.LegEnhancements;
import atlantis.production.dynamic.protoss.tech.ProtossGroundArmor;
import atlantis.production.dynamic.protoss.tech.ProtossGroundWeapons;
import atlantis.production.dynamic.protoss.tech.SingularityCharge;
import atlantis.production.dynamic.terran.tech.SiegeMode;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.util.Enemy;
import atlantis.util.We;
import bwapi.UpgradeType;

import static bwapi.UpgradeType.Singularity_Charge;


public class ProtossDynamicTechResearch extends Commander {
    @Override
    public boolean applies() {
        return We.protoss() && A.everyNthGameFrame(61);
    }

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            SingularityCharge.class,
//            LegEnhancements.class,
//            ProtossGroundWeapons.class,
//            ProtossGroundArmor.class,
        };
    }

//    @Override
//    public void handle() {
//        if (singularityCharge()) return;
//
//        if (!A.canAfford(500, 200)) return;
//
//        if (Count.zealots() >= 10) {
//            if (DynamicTech.handleUpgrade(UpgradeType.Leg_Enhancements)) return;
//        }
//        if (Count.ourCombatUnits() >= 16) {
//            if (DynamicTech.handleUpgrade(UpgradeType.Protoss_Ground_Weapons)) return;
//        }
//        else if (Count.ourCombatUnits() >= 25) {
//            if (DynamicTech.handleUpgrade(UpgradeType.Protoss_Ground_Armor)) return;
//        }
//    }
}
