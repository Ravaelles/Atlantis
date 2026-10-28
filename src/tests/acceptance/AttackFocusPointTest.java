package tests.acceptance;

import atlantis.combat.focus.AttackFocusPoint;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #194: the attack side owns its named home, mirroring the defend
 * side - the anchor only follows. Known enemy base first, then the
 * known main, else exploration, else nothing (the null path the old
 * inline chain already had, pinned here instead of re-derived).
 */
public class AttackFocusPointTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void knownEnemyBaseIsTheTarget() {
        FakeUnit foeNexus = fake(AUnitType.Protoss_Nexus, 100, 100);
        foeNexus.setEnemy();

        try (MockedStatic<EnemyUnits> enemies = Mockito.mockStatic(EnemyUnits.class)) {
            enemies.when(EnemyUnits::enemyBase).thenReturn(foeNexus);

            world(8, fakeOurs(
                fake(AUnitType.Protoss_Nexus, 10, 10),
                fake(AUnitType.Protoss_Probe, 9, 10)
            ), fakeEnemies(foeNexus), () -> {
                if (atlantis.game.A.now() != 2) return;

                HasPosition focus = AttackFocusPoint.focusPoint();

                assertEquals(foeNexus.position(), focus,
                    "a known enemy base is marched, not searched around");
            });
        }
    }

    @Test
    public void knownEnemyMainIsNext() {
        APosition enemyMain = APosition.create(100, 100);

        try (MockedStatic<EnemyUnits> enemies = Mockito.mockStatic(EnemyUnits.class)) {
            enemies.when(EnemyUnits::enemyBase).thenReturn(null);
            enemies.when(EnemyUnits::enemyMainBase).thenReturn(enemyMain);

            world(8, fakeOurs(
                fake(AUnitType.Protoss_Nexus, 10, 10),
                fake(AUnitType.Protoss_Probe, 9, 10)
            ), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;

                assertEquals(enemyMain, AttackFocusPoint.focusPoint(),
                    "no discovered base, but the main is known - march there");
            });
        }
    }

    @Test
    public void nothingKnownIsNoFocus() {
        try (MockedStatic<EnemyUnits> enemies = Mockito.mockStatic(EnemyUnits.class)) {
            enemies.when(EnemyUnits::enemyBase).thenReturn(null);
            enemies.when(EnemyUnits::enemyMainBase).thenReturn(null);

            world(8, fakeOurs(
                fake(AUnitType.Protoss_Probe, 9, 10)
            ), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;

                assertNull(AttackFocusPoint.focusPoint(),
                    "no enemy, no base, nowhere to march from - null, as before");
            });
        }
    }

    @Test
    public void decisionTwinsTargetAndReasonOnce() {
        FakeUnit foeNexus = fake(AUnitType.Protoss_Nexus, 100, 100);
        foeNexus.setEnemy();

        try (MockedStatic<EnemyUnits> enemies = Mockito.mockStatic(EnemyUnits.class)) {
            enemies.when(EnemyUnits::enemyBase).thenReturn(foeNexus);
            enemies.when(EnemyUnits::enemyMainBase).thenReturn(foeNexus.position());

            world(8, fakeOurs(
                fake(AUnitType.Protoss_Nexus, 10, 10),
                fake(AUnitType.Protoss_Probe, 9, 10)
            ), fakeEnemies(foeNexus), () -> {
                if (atlantis.game.A.now() < 2) return;

                AttackFocusPoint.focusPoint();
            });
        }

        String rows = atlantis.util.log.EventLog.toJsonLines();
        int first = rows.indexOf("\"key\":\"ATTACK_DECISION\"");
        assertTrue(first >= 0, "the march is twinned with its reason: " + rows);
        assertTrue(rows.indexOf("\"key\":\"ATTACK_DECISION\"", first + 1) < 0,
            "the same decision re-asked is not a new row");
        assertTrue(rows.contains("\"reason\":\"ENEMY_BASE_SEEN\""),
            "the row names why, was: " + rows);
    }
}
