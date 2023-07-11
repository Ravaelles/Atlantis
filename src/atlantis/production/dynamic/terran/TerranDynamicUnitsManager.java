package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyFlags;
import atlantis.production.AbstractDynamicUnits;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Terran_Science_Vessel;

public class TerranDynamicUnitsManager extends AbstractDynamicUnits {

    public static void update() {
        scienceVessels();

        TerranDynamicFactoryUnits.handleFactoryProduction();

        wraiths();

        if (Count.infantry() <= 14 || (Enemy.protoss() && Count.tanks() >= 4) || A.hasMinerals(500)) {
            TerranDynamicInfantry.ghosts();
            TerranDynamicInfantry.medics();
            TerranDynamicInfantry.marines();
        }
    }

    // =========================================================

    private static void scienceVessels() {
        if (Have.notEvenPlanned(Terran_Science_Vessel)) {
            if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) {
                AddToQueue.withTopPriority(Terran_Science_Vessel);
            }
            return;
        }

        int limit = Math.max(
            1 + (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT ? 2 : 0),
            A.supplyTotal() / 35
        );
        buildToHave(Terran_Science_Vessel, limit);
    }

    protected static boolean wraiths() {
        int startProducingWraithsSinceSupply = 90;

//        if (Enemy.zerg()) {
//            return false;
//        }

        if (
            Count.ofType(AUnitType.Terran_Starport) == 0
        ) {
            return false;
        }

        int wraiths = Count.ofType(AUnitType.Terran_Wraith);

        if (wraiths >= 5 && !AGame.canAffordWithReserved(200, 200)) {
            return false;
        }

        if (A.supplyUsed() >= startProducingWraithsSinceSupply && wraiths <= 1) {
            return AddToQueue.addToQueueIfNotAlreadyThere(AUnitType.Terran_Wraith);
        }

        if (A.supplyUsed() <= 160 && wraiths >= startProducingWraithsSinceSupply + wraiths * 15) {
            return false;
        }

        if (wraiths >= 6 && !AGame.canAffordWithReserved(150, 150)) {
            return false;
        }

        return AddToQueue.addToQueueIfNotAlreadyThere(AUnitType.Terran_Wraith);
    }
}
