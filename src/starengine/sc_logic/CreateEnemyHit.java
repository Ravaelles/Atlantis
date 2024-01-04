package starengine.sc_logic;

import atlantis.game.A;
import tests.unit.FakeUnit;

public class CreateEnemyHit {
    public static void createHit(FakeUnit attacker, FakeUnit target) {
        int damage = attacker.damageAgainst(target);
        target.hp -= damage;

        A.println(attacker + " hits " + target + " for " + damage + " hp (" + target.hp + " left)");

        if (target.hp <= 0) unitIsDead(target);
    }

    private static void unitIsDead(FakeUnit unit) {
        // Do nothing
    }
}
