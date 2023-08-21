package atlantis.production.dynamic.terran;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.enemy.EnemyFlags;
import atlantis.production.dynamic.terran.abundance.TerranAbundance;
import atlantis.production.dynamic.terran.units.ProduceGhosts;
import atlantis.production.dynamic.terran.units.ProduceMarines;
import atlantis.production.dynamic.terran.units.ProduceMedicsAndFirebats;
import atlantis.production.dynamic.terran.units.ProduceWraiths;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;
import atlantis.util.We;

import static atlantis.production.AbstractDynamicUnits.buildToHave;
import static atlantis.units.AUnitType.Terran_Science_Facility;
import static atlantis.units.AUnitType.Terran_Science_Vessel;

public class TerranDynamicUnitsCommander extends Commander {
    @Override
    public boolean applies() {
        return We.terran();
    }

    @Override
    protected void handle() {
        scienceVessels();

        TerranDynamicFactoryUnits.handleFactoryProduction();

        ProduceWraiths.wraiths();

        if (Count.infantry() <= 14 || (Enemy.protoss() && Count.tanks() >= 4) || A.hasMinerals(500)) {
            ProduceGhosts.ghosts();
            ProduceMedicsAndFirebats.medics();
            ProduceMarines.marines();
        }

        (new TerranAbundance()).invoke();
    }

    // =========================================================

    private static void scienceVessels() {
        if (!Have.notEvenPlanned(Terran_Science_Facility)) {
            if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) {
                AddToQueue.withTopPriority(Terran_Science_Facility);
            }
            return;
        }

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

}
