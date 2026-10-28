package atlantis.combat.twins;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.EnemyArmyStrength;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

import java.util.ArrayList;

/**
 * Strength decomposition at contact transitions (NEXT #2 follow-up: the
 * live strength doubles when a contact is lost, e.g. 182 to 364 - either
 * the visible set was double-counted or the fogged set drops units).
 *
 * <p>Rows only when foe-known flips between samples (plus the first
 * sample): ours total, enemy total, and the enemy split into visible
 * vs remembered-fogged heads and HP. If visible+fogged exceeds the
 * total the dedup is broken; if the fogged part vanishes the memory
 * is. Read once, then decide whether this probe stays or goes.</p>
 */
public final class StrengthWatch {

    private static Boolean lastKnown = null;

    private StrengthWatch() {
    }

    public static void sample(boolean foeKnown) {
        boolean transition = lastKnown == null || lastKnown != foeKnown;
        lastKnown = foeKnown;
        if (!transition) return;

        Selection visible = Select.enemy().notDeadMan().combatUnits();
        Selection fogged = Select.from(
            new ArrayList<>(EnemyUnits.rawUnitsDiscovered()), "fogged").notDeadMan().combatUnits();
        Selection both = EnemyUnits.discoveredIncludingAddressless().combatUnits();

        EventLog.key(LogKey.STRENGTH_WATCH)
            .param("foeKnown", foeKnown)
            .param("ours", totalOf(Select.ourCombatUnits()))
            .param("enemy", totalOf(both))
            .param("visN", visible.count())
            .param("visHp", visible.totalHp())
            .param("fogN", fogged.count())
            .param("fogHp", fogged.totalHp())
            .log(A.now(), A.minSec());
    }

    private static int totalOf(Selection units) {
        return Math.max(1,
            units.totalHp()
                + units.melee().count() * 10
                + units.ranged().count() * 30
                - units.combatBuildings(true).count() * 50
                - units.bases().count() * 100);
    }

    public static void clearForTests() {
        lastKnown = null;
    }
}
