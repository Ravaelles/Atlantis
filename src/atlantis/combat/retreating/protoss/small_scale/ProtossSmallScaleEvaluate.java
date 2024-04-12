package atlantis.combat.retreating.protoss.small_scale;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ProtossSmallScaleEvaluate {
    protected static boolean meleeOverpoweredInRadius(
        AUnit unit, Selection friends, Selection enemies, double radius
    ) {
        double ourMeleeStrength = ourMeleeStrength(unit, friends, radius);
        double enemyMeleeStrength = enemyMeleeStrength(unit, enemies, radius);

//        if (ourMeleeStrength > 1.1) {
//            System.out.println(A.at() + "STRENGTH = " + ourMeleeStrength + "/" + enemyMeleeStrength);
//        }

        double dEval = ourMeleeStrength - enemyMeleeStrength;
        unit.setLastSmallScaleEval(dEval);

        return dEval < 0;
    }

    protected static double ourMeleeStrength(AUnit unit, Selection friends, double radius) {
        return friends.melee().inRadius(radius, unit).havingAtLeastHp(23).count()
            + (unit.hp() >= 30 ? 1 : 0.3);
    }

    protected static double enemyMeleeStrength(AUnit unit, Selection enemies, double radius) {
//        enemies.melee().inRadius(radius, unit).print(A.now() + " Enemies in radius: " + radius);
        return enemies.melee().inRadius(radius, unit).count() * enemyMeleeMultiplier();
    }

    protected static double enemyMeleeMultiplier() {
        if (Enemy.protoss()) return 1;
        else if (Enemy.terran()) return 0.7;
        return 0.4;
    }
}
