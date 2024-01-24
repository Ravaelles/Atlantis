package atlantis.information.decisions.terran;

import atlantis.decions.Decision;
import atlantis.information.decisions.Decisions;
import atlantis.information.decisions.FocusOnProducingUnits;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.*;

public class TerranDecisions extends Decisions {
    public static Decision DONT_PRODUCE_TANKS_AT_ALL = new Decision(false);

    public static void reset() {
        // Reset all static variables to false by iterating over this class fields
//        for (java.lang.reflect.Field field : Terranclass.getDeclaredFields()) {
//            if (field.getType() == boolean.class) {
//                try {
//                    field.set(null, false);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }

//        ClassUtil.changeStaticFieldsInClassTo(TerranDecisions.class, false);
    }

    public static boolean haveFactories() {
        return cache.get(
            "haveFactories",
            91,
            () -> {
                return OurStrategy.get().goingBio();
            }
        );
    }

    public static boolean shouldMakeTerranBio() {
        return cache.get(
            "shouldMakeTerranBio",
            95,
            () -> ShouldMakeTerranBio.should()
        );
    }

    public static boolean produceVultures() {
//        return true;
        return cache.get(
            "produceVultures",
            99,
            () -> {
                int vultures = Count.vultures();

                if (FocusOnProducingUnits.isFocusedOn(Terran_Vulture)) {
                    if (vultures < 4) {
                        return true;
                    }
                }

//                if (true) return true;

//                if (vultures < 12 && (A.hasMinerals(800) && !A.hasGas(200))) {
//                    return true;
//                }

                if (vultures == 0 && EnemyUnits.count(Zerg_Zergling) >= 7) return true;

                if (vultures <= 2 && EnemyUnits.count(Protoss_Zealot) >= 6) return false;

                if (
                    GamePhase.isEarlyGame()
                        && vultures <= 3
                        && EnemyUnits.discovered().ofType(Protoss_Zealot).atLeast(5)
                ) return false;

                return (maxFocusOnTanks() && vultures >= 1)
                    || (Enemy.terran() && vultures >= 1)
                    || (vultures >= 2 && Count.tanks() < 2)
                    || vultures >= 15;
            }
//                () -> Count.vultures() >= 1
//                () -> maxFocusOnTanks() || (shouldBuildBio() && Count.vultures() <= 1)
        );
    }

    public static boolean maxFocusOnTanks() {
        return cache.get(
            "maxFocusOnTanks",
            91,
            () ->
//                    GamePhase.isEarlyGame()
                (
                    EnemyInfo.startedWithCombatBuilding
                        || EnemyUnits.discovered().combatBuildings(true).atLeast(2)
                )
//                        && Have.factory() && Have.machineShop()
        );
    }

    public static boolean weHaveBunkerAndDontHaveToDefendAnyLonger() {
        if (Enemy.zerg()) {
            if (GamePhase.isEarlyGame()) {
                if (EnemyUnits.discovered().countOfType(AUnitType.Zerg_Zergling) >= 9) {
                    return Count.ourCombatUnits() >= 13;
                }

                if (Count.medics() <= 3) return false;
            }
        }

        return Count.ourCombatUnits() >= 8;
    }
}
