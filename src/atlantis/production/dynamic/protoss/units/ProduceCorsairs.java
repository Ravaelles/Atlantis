package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.protoss.corsair.CorsairHuntOverlords;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.production.AbstractDynamicUnits.buildToHave;
import static atlantis.production.AbstractDynamicUnits.trainNowIfHaveWhatsRequired;

public class ProduceCorsairs {
    private static int produced = 0;

    public static boolean corsairs() {
        if (Have.notEvenPlanned(AUnitType.Protoss_Stargate)) return false;
        if (!A.hasMinerals(200)) return false;

        if (Enemy.zerg()) {
            if (againstZerg()) return produce();
            return false;
        }

        if (produceFirstCorsair()) {
            return buildToHave(AUnitType.Protoss_Corsair, 1) && increaseProduced();
        }

        if (Enemy.terran()) {
            if (EnemyUnits.count(AUnitType.Terran_Wraith) >= 1 && produceAgainstWraiths()) return true;
        }

        return false;
    }

    private static boolean produce() {
        if (ProtossBuildingToProduce.produce(AUnitType.Protoss_Corsair)) {
            return increaseProduced();
        }

        return false;
    }

    private static boolean againstZerg() {
        if (produceFirstCorsairAgainstZerg()) return true;

        return produceAgainstMutalisks() || produceAgainstOverlords();
    }

    private static boolean produceAgainstOverlords() {
        int corsairs = Count.ourWithUnfinished(AUnitType.Protoss_Corsair);
        int expected = A.supplyUsed() / 36;

        return corsairs < expected;
    }

    private static boolean produceAgainstMutalisks() {
        if (!EnemyInfo.goesZergAirUnits()) {
            return false;
        }

        return buildToHave(AUnitType.Protoss_Corsair, (int) (EnemyUnits.count(AUnitType.Zerg_Mutalisk) / 2) + 1)
            && increaseProduced();
    }

    private static boolean produceAgainstWraiths() {
        return buildToHave(AUnitType.Protoss_Corsair, (int) (EnemyUnits.count(AUnitType.Terran_Wraith) / 4) + 1)
            && increaseProduced();
    }

    private static boolean produceFirstCorsairAgainstZerg() {
        return produced <= 1
            && Count.corsairs() == 0
            && A.supplyUsed() >= Enemy.zergElse(40, 80);
    }

    private static boolean produceFirstCorsair() {
        return produced <= 1
            && Count.corsairs() == 0
            && A.supplyUsed() >= Enemy.zergElse(40, 80);
    }

    private static boolean increaseProduced() {
        produced++;
        return true;
    }
}
