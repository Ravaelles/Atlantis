package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.information.enemy.EnemyFlags;
import atlantis.production.AbstractDynamicUnits;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Terran_Science_Vessel;

public class TerranDynamicUnitsManager extends AbstractDynamicUnits {

    public static void update() {
        scienceVessels();

        TerranDynamicFactoryUnits.handleFactoryProduction();

        TerranDynamicInfantry.ghosts();
        TerranDynamicInfantry.medics();
        TerranDynamicInfantry.marines();
    }

    // =========================================================

    private static void scienceVessels() {
        if (Have.no(Terran_Science_Vessel)) {
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

}
