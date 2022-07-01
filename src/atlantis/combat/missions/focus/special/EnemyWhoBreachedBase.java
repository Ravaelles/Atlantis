package atlantis.combat.missions.focus.special;

import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.information.enemy.EnemyInfo;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class EnemyWhoBreachedBase {

    public static AUnit get() {
        AUnit enemyWhoBreachedBase = EnemyInfo.enemyNearAnyOurBase(-1);
        if (enemyWhoBreachedBase != null && enemyWhoBreachedBase.isAlive()) {

//            if (We.zerg()) {
//                return asZerg(enemyWhoBreachedBase);
//            }

            return enemyWhoBreachedBase;
        }

        return null;
    }

//    private static AUnit asZerg(AUnit enemyWhoBreachedBase) {
//        int sunkens = Count.sunkens();
//
//        if (
//            sunkens == 0
//                || (
//                sunkens > 0 && enemyWhoBreachedBase.enemiesNear()
//                    .ofType(AUnitType.Zerg_Sunken_Colony)
//                    .notEmpty()
//            )
//        ) {
//            return enemyWhoBreachedBase;
//        }
//    }

}
