package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.information.decisions.terran.TerranDecisions;
import atlantis.information.generic.TerranArmyComposition;
import atlantis.information.strategy.Strategy;
import atlantis.production.dynamic.terran.TerranDynamicInfantry;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

import static atlantis.units.AUnitType.*;

public class ProduceMedicsAndFirebats {
    public static boolean medics() {
        if (!A.hasGas(1)) return false;
        if (Count.ofType(AUnitType.Terran_Academy) == 0) return false;

        boolean goingBio = Strategy.get().goingBio();

        int medics = Count.medics();
        if (medics == 0) return produceMedic();

        if (medics >= 2 && !goingBio && (!Have.machineShop() || !A.hasGas(125))) return false;

        int medicsUnfinished = Count.ourUnfinishedOfType(Terran_Medic);
        int marines = Count.marines();
        int infantry = Count.infantry();
        boolean medicsRatioTooLow = medicsUnfinished <= 1 && TerranArmyComposition.medicsToInfantryRatioTooLow();

        if (medics <= 2 && goingBio && marines >= 4) return produceMedic();

        if (medics <= 1 && (medicsUnfinished <= 1 || medics == 0)) return produceMedic();

        if (medicsUnfinished >= 3) return false;

        if (!TerranDecisions.shouldMakeTerranBio()) {
            if (Have.academy() && Count.infantry() >= 4 && medics <= 0) return false;
        }

//        if (!ShouldProduceInfantry.canProduceInfantry(medics)) return false;

        if (TerranDynamicInfantry.needToSaveForFactory()) return false;
        if (!A.canAffordWithReserved(60, 30)) return false;

        if (medicsRatioTooLow) {
            if (medicsUnfinished <= 0 && marines >= 2) {
                return produceMedic();
            }

            // We have medics, but all of them are depleted from energy
            if (medicsUnfinished > 0 && Select.ourOfType(Terran_Medic).havingEnergy(30).isEmpty()) {
                return produceMedic();
            }
        }

        Selection barracks = Select.ourFree(AUnitType.Terran_Barracks);
        if (barracks.isNotEmpty()) {

            // Firebats
            if (!Enemy.terran()) {
                int unfinishedFirebats = Count.inProductionOrInQueue(Terran_Firebat);
                int firebats = Count.firebats();

                if (unfinishedFirebats == 0) {
                    if (
                        marines >= 4
                            && medics >= 3
                            && unfinishedFirebats < minFirebats()
                            && marines >= 4 * firebats
                    ) return produceFirebat();
                }
            }

            // Medics
            if (
                medicsRatioTooLow
                    && TerranArmyComposition.medicsToInfantryRatioTooLow()
                    && Count.inProductionOrInQueue(Terran_Marine) <= 2
            ) return produceMedic();
        }

        return false;
    }

    private static boolean produceFirebat() {
        return ForceProduceUnit.forceProduce(Terran_Firebat);
//        return AddToQueue.maxAtATime(Terran_Firebat, 1) != null;
    }

    private static boolean produceMedic() {
        return ForceProduceUnit.forceProduce(Terran_Medic);
//        return AddToQueue.maxAtATime(Terran_Medic, 2) != null;
    }

    private static int minFirebats() {
        if (Enemy.terran()) return 0;

        int marines = Count.marines();

        if (marines <= 7) return 0;

        if (Enemy.protoss()) {
            return Math.max(4, (marines / 8 + Count.medics() / 5));
        }

        // Zerg
        return Math.max(4, Count.medics() / 4);
    }
}
