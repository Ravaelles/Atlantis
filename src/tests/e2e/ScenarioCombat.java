package tests.acceptance.e2e;

import atlantis.map.bullets.BulletDamageAgainst;
import atlantis.units.AUnit;
import tests.fakes.FakeBullet;
import tests.fakes.FakeUnit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic combat resolution for scenario tests: whoever has a live
 * target in reach and a cooled-down weapon deals one round of damage.
 *
 * <p>Two rules, both replicating what the game does without any bot:</p>
 * <ul>
 *   <li>mobile units strike the target their side assigned them (Atlantis
 *       managers for ours via {@code attackUnit}, the zombie brain for
 *       theirs) - so this class measures <b>decisions</b>, not damage
 *       modelling;</li>
 *   <li>our combat <b>buildings</b> auto-acquire the nearest enemy in reach,
 *       exactly like the engine does (no orders needed in a real game, and
 *       no manager drives them in the stub world).</li>
 * </ul>
 *
 * <p>Damage of one round is {@code BulletDamageAgainst} (the bot's own
 * arithmetic over engine data), applied shields-first. Cooldowns are the
 * engine weapon cooldowns, tracked per unit id in this class so manager
 * cooldown state is never touched. Deliberate simplifications, all
 * conservative for a defense test: no armor, no upgrades, no splash, no
 * high-ground or building-size modifiers. If Atlantis holds under these,
 * it holds with shields modelled properly; the numbers would only move in
 * our favour.</p>
 *
 * <p>When the OpenBW runner (see {@code _AI/IDEA-E2E-TESTS.md}) can host
 * Atlantis, this class retires: the engine resolves everything and the
 * scenarios keep only their forces, timing and assertions.</p>
 */
public class ScenarioCombat {

    private final Map<Integer, Integer> lastStrikeAt = new HashMap<>();

    /**
     * Resolve one frame of strikes for every unit on both sides.
     */
    public void onFrame(int frame, List<FakeUnit> ours, List<FakeUnit> enemies) {
        strikeForSide(frame, ours, enemies);
        strikeForSide(frame, enemies, ours);
    }

    private void strikeForSide(int frame, List<FakeUnit> attackers, List<FakeUnit> targets) {
        for (FakeUnit attacker : attackers) {
            if (!attacker.isAlive()) continue;

            // Ground scenarios only (4pool/9pool defense have no air units on
            // either side); an air weapon never fires here.
            if (!attacker.type().hasGroundWeapon()) continue;

            FakeUnit target = pickTarget(attacker, targets);
            if (target == null) continue;

            if (!inReach(attacker, target)) continue;

            int cooldown = attacker.type().groundWeapon().damageCooldown();
            int lastStrike = lastStrikeAt.getOrDefault(attacker.id(), -99999);
            if (frame - lastStrike < cooldown) continue;

            lastStrikeAt.put(attacker.id(), frame);
            dealOneRound(attacker, target);
        }
    }

    /**
     * Mobile units shoot what they were told to; our combat buildings shoot
     * what the game would shoot for them: the nearest reachable enemy.
     */
    private FakeUnit pickTarget(FakeUnit attacker, List<FakeUnit> targets) {
        if (attacker.isABuilding()) {
            return nearestAliveInReach(attacker, targets);
        }

        AUnit assigned = attacker.target();
        if (assigned instanceof FakeUnit) {
            FakeUnit target = (FakeUnit) assigned;
            if (target.isAlive()) return target;
        }

        return null;
    }

    private FakeUnit nearestAliveInReach(FakeUnit attacker, List<FakeUnit> targets) {
        FakeUnit nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (FakeUnit target : targets) {
            if (!target.isAlive()) continue;

            double dist = attacker.distTo(target);
            if (dist <= reachOf(attacker) && dist < nearestDist) {
                nearestDist = dist;
                nearest = target;
            }
        }

        return nearest;
    }

    private boolean inReach(FakeUnit attacker, FakeUnit target) {
        return attacker.distTo(target) <= reachOf(attacker);
    }

    private double reachOf(FakeUnit attacker) {
        return attacker.groundWeaponRange() + 0.5;
    }

    private void dealOneRound(FakeUnit attacker, FakeUnit target) {
        FakeBullet bullet = FakeBullet.fromPosition(attacker.position, attacker, target);
        int damage = BulletDamageAgainst.forBullet(bullet);

        // Engine semantics, matching AUnit.hp() = hitPoints() + shields(): hp
        // is the total pool and always drops by the full damage; shields drop
        // in parallel while any last, as an indicator for shield-specific
        // rules - not as a second pool. Absorbing into shields without
        // touching hp would count every shield point twice.
        target.setHp(target.hp() - damage);
        target.setShields(Math.max(0, target.shields() - damage));
    }
}
