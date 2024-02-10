package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.generic.OurArmyStrength;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

import static bwapi.UpgradeType.Protoss_Ground_Weapons;

public class ProtossGroundWeapons extends Commander {
    @Override
    public boolean applies() {
        if (ATech.isResearched(Protoss_Ground_Weapons)) return false;
        if (TooWeakToTech.check()) return false;

        if (A.hasGas(320 + (Enemy.zerg() ? 80 : 0)) && A.hasMinerals(550)) {
            if (Count.ourCombatUnits() <= 15) return false;

            return true;
        }

        return false;
    }

    @Override
    protected void handle() {
        AddToQueue.upgrade(Protoss_Ground_Weapons);
    }
}
