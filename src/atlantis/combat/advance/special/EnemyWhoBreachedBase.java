package atlantis.combat.advance.special;

import atlantis.information.enemy.EnemyInfo;
import atlantis.units.AUnit;

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
