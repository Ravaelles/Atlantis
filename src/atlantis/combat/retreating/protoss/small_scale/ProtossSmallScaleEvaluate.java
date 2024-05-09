package atlantis.combat.retreating.protoss.small_scale;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ProtossSmallScaleEvaluate {
    public static final double RADIUS_LG = 3.5;
    public static final double RADIUS_SM = 1.6;

    public static boolean isOverpoweredByEnemyMelee(AUnit unit, Selection friends, Selection enemies) {
        if (ProtossMeleeVsMelee.beBraveIn1v1(unit, enemies)) return false;

        return meleeOverpoweredInRadius(unit, friends, enemies, RADIUS_SM)
            && meleeOverpoweredInRadius(unit, friends, enemies, RADIUS_LG);
    }

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
            + (unit.hp() >= 23 ? 1.05 : 0.55);
    }

    protected static double enemyMeleeStrength(AUnit unit, Selection enemies, double radius) {
//        enemies.melee().inRadius(radius, unit).print(A.now() + " AliveEnemies in radius: " + radius);
        double rawEval = enemies.melee().inRadius(radius, unit).count()
            + (unit.hp() >= 23 ? 1 : 0.5);

        return rawEval * enemyMeleeMultiplier();

    }

    protected static double enemyMeleeMultiplier() {
        if (Enemy.protoss()) return 1;
        else if (Enemy.terran()) return 0.7;
        return 0.4;
    }
}
