package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.generic.TerranArmyComposition;
import atlantis.production.dynamic.terran.TerranDynamicInfantry;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ProduceMedicsAndFirebats {
    public static boolean medics() {
        if (Count.ofType(AUnitType.Terran_Academy) == 0) return false;

        int medics = Count.medics();
        if (!Decisions.shouldMakeTerranBio()) {
            if (Have.academy() && Count.infantry() >= 4 && medics <= 0) return false;
        }

        // =========================================================

        int marines = Count.marines();
        if (A.hasGas(25) && medics == 0 && marines > 0) {
            return AddToQueue.maxAtATime(AUnitType.Terran_Medic, 2);
        }

        // We have medics, but all of them are depleted from energy
        if (medics > 0 && Select.ourOfType(AUnitType.Terran_Medic).havingEnergy(30).isEmpty()) {
            return AddToQueue.maxAtATime(AUnitType.Terran_Medic, 4);
        }

        if (TerranDynamicInfantry.needToSaveForFactory()) return false;

        if (!AGame.canAffordWithReserved(60, 30)) return false;

        Selection barracks = Select.ourOfType(AUnitType.Terran_Barracks).free();
        if (barracks.isNotEmpty()) {

            // Firebats
            if (!Enemy.terran()) {
                if (marines >= 4 && medics >= 3 && Count.ourOfTypeWithUnfinished(AUnitType.Terran_Firebat) < minFirebats()) {
                    return AddToQueue.maxAtATime(AUnitType.Terran_Firebat, 2);
                }
            }

            // Medics
            if (TerranArmyComposition.medicsToInfantryRatioTooLow()) {
                return AddToQueue.maxAtATime(AUnitType.Terran_Medic, 4);
            }
        }

        return false;
    }

    private static int minFirebats() {
        if (Enemy.terran()) return 0;
        if (Count.marines() <= 7) return 0;

        if (Enemy.protoss()) {
            return Math.max(4, (Count.marines() / 10 + Count.medics() / 6 - 1));
        }

        // Zerg
        return Math.max(4, Count.medics() / 4);
    }
}
