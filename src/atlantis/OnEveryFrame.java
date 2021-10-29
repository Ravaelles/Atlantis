package atlantis;

import atlantis.enemy.AEnemyUnits;
import atlantis.units.select.Select;

public class OnEveryFrame {

//    private static CappedList<Integer> frames = new CappedList<>(4);

    public static void update() {
//        for (AUnit unit : Select.ourCombatUnits().list()) {
//            if (unit.isUnderAttack(2) && unit.hpPercent() < 48) {
//                GameSpeed.changeSpeedTo(30);
//            }
//        }

//        AEnemyUnits.printEnemyFoggedUnits();
//        System.out.println("ENEMY BASE = " + AEnemyUnits.enemyBase());

        Select.printCache();
    }

}
