package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.generic.OurArmyStrength;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import static bwapi.UpgradeType.Protoss_Ground_Armor;
import static bwapi.UpgradeType.Protoss_Ground_Weapons;

public class ProtossGroundArmor extends Commander {
    @Override
    public boolean applies() {
        if (!Have.forge()) return false;
        if (ATech.isResearched(Protoss_Ground_Armor)) return false;
        if (CountInQueue.count(Protoss_Ground_Armor, 10) > 0) return false;
        if (TooWeakToTech.check()) return false;

        if (A.hasGas(270 + (Enemy.protoss() ? 100 : 0)) && A.hasMinerals(550)) {
            if (Count.ourCombatUnits() <= 17) return false;

            return true;
        }

        return false;
    }

    @Override
    protected void handle() {
        AddToQueue.upgrade(Protoss_Ground_Armor);
    }
}
