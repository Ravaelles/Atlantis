package atlantis.production.dynamic.reinforce.terran.turrets;

import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.Bases;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class TerranSecureBaseWithTurrets {
    public static void secureAllBases() {
        secureMain();
        secureNatural();
        for (AUnit base : Select.ourBasesWithUnfinished().list()) {
            secureExpansion(base);
        }
    }

    private static boolean secureNatural() {
        AUnit natural = Bases.natural();
        if (natural == null) return false;

        return securePosition(natural, Count.basesWithUnfinished() >= 2 ? 1 : 0);
    }

    private static boolean secureMain() {
        return securePosition(Select.main(), Count.basesWithUnfinished() <= 1 ? 1 : 0);
    }

    private static boolean secureExpansion(HasPosition base) {
        return securePosition(base, 0);
    }

    private static boolean securePosition(HasPosition securePosition, int bonus) {
        if (securePosition == null) return false;

        int existing = Count.existingOrPlannedBuildingsNear(type(), 20, securePosition);
        int expected = expectedCount(securePosition, 1);

        if (existing < expected) return false;

        for (int i = 0; i < expected - existing; i++) {
            TerranMissileTurret.get().requestOne(securePosition);
        }
        return true;
    }

    private static int expectedCount(HasPosition securePosition, int bonus) {
        double fromEnemies = 0;
        Selection enemies = EnemyUnits.discovered().air();

        if (Enemy.zerg()) {
            fromEnemies = enemies.ofType(AUnitType.Zerg_Mutalisk).count();
        }

        if (Enemy.zerg()) fromEnemies = expectedCountFromEnemiesVsZ(enemies);
        if (Enemy.protoss()) fromEnemies = expectedCountFromEnemiesVsP(enemies);
        if (Enemy.terran()) fromEnemies = expectedCountFromEnemiesVsT(enemies);

        return (int) Math.round(bonus + fromEnemies);
    }

    private static double expectedCountFromEnemiesVsZ(Selection enemies) {
        return enemies.ofType(AUnitType.Zerg_Mutalisk).count() * 0.34;
    }

    private static double expectedCountFromEnemiesVsT(Selection enemies) {
        return enemies.ofType(AUnitType.Terran_Wraith).count() * 0.2;
    }

    private static double expectedCountFromEnemiesVsP(Selection enemies) {
        return enemies.ofType(AUnitType.Protoss_Carrier).count() * 1.1 + enemies.ofType(AUnitType.Protoss_Scout).count() * 0.2;
    }

    private static AUnitType type() {
        return AUnitType.Terran_Missile_Turret;
    }
}
