package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.generic.TerranArmyComposition;
import atlantis.production.dynamic.terran.TerranDynamicInfantry;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.*;

public class ProduceMedicsAndFirebats {
    public static boolean medics() {
        if (!A.hasGas(1) || Count.ofType(AUnitType.Terran_Refinery) == 0) return false;
        if (Count.ofType(AUnitType.Terran_Academy) == 0) return false;

        int medics = Count.medics();
        int medicsUnfinished = Count.ourUnfinishedOfType(Terran_Medic);
        int marines = Count.marines();

        if (medicsUnfinished >= 3) return false;

        if (!Decisions.shouldMakeTerranBio()) {
            if (Have.academy() && Count.infantry() >= 4 && medics <= 0) return false;
        }

//        if (!ShouldProduceInfantry.canProduceInfantry(medics)) return false;

        if (medics <= 1 && marines >= 2) {
            return produceMedic();
        }

        // =========================================================

        // We have medics, but all of them are depleted from energy
        if (medics > 0 && Select.ourOfType(Terran_Medic).havingEnergy(30).isEmpty()) {
            return produceMedic();
        }

        if (TerranDynamicInfantry.needToSaveForFactory()) return false;
        if (!AGame.canAffordWithReserved(60, 30)) return false;

        Selection barracks = Select.ourFree(AUnitType.Terran_Barracks);
        if (barracks.isNotEmpty()) {

            // Firebats
            if (!Enemy.terran()) {
                int unfinishedFirebats = Count.inProductionOrInQueue(Terran_Firebat);
                if (unfinishedFirebats == 0) {
                    if (marines >= 4 && medics >= 3 && unfinishedFirebats < minFirebats()) {
                        return AddToQueue.maxAtATime(Terran_Firebat, 1) != null;
                    }
                }
            }

            // Medics
            if (TerranArmyComposition.medicsToInfantryRatioTooLow()) {
                if (Count.medics() <= 4 && Count.inProductionOrInQueue(Terran_Marine) <= 2) {
                    return produceMedic();
                }
            }
        }

        return false;
    }

    private static boolean produceMedic() {
        return AddToQueue.maxAtATime(Terran_Medic, 2) != null;
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
