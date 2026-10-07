package atlantis.production.dynamic.protoss.units;

import atlantis.information.enemy.EnemyInfo;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Protoss_Gateway;

/**
 * Which gateway a Protoss unit should be trained from.
 *
 * <p>Preference is a free gateway nearest to the enemy, as before. What changed on
 * 2026-10-05 is the fallback: when the bot has gateways but none of them counts as
 * "free", this used to return {@code null} and every producer bailed on
 * {@code freeGateways == 0} - which is what five real games did (BUGS.md B-22: one
 * zealot and one dragoon, then nothing for the rest of the game, in 5 games out of 5).
 *
 * <p>The question {@code free()} answers is "is this building idle", and idle is a
 * <i>movement</i> notion; whether a producer can take a train order is a different one,
 * and the engine is the only authority on it. So the fallback asks a gateway anyway: if
 * the engine refuses, the order costs one refused call every 7 frames and changes
 * nothing, and if the gateway was busy for a reason that does not actually stop
 * production - a finished order the engine still counts, a queued action - the bot
 * starts producing again, which is the whole difference between a game with two combat
 * units and a game with an army.</p>
 *
 * <p>No further filter on the fallback: a Protoss gateway cannot morph and cannot be
 * lifted (those are the Zerg producer's problems), and "is it usable right now" is the
 * engine's question, asked rather than guessed.</p>
 */
public class GatewayClosestToEnemy {

    public static AUnit get() {
        APosition enemyPosition = EnemyInfo.enemyLocationOrGuess();

        AUnit free = enemyPosition == null
            ? Select.ourFree(Protoss_Gateway).random()
            : Select.ourFree(Protoss_Gateway).groundNearestTo(enemyPosition);

        if (free != null) return free;

        // Fallback: a Gateway that is completed and not already training. The
        // plain `ourOfType` fallback used to be enough only by accident -
        // Select filters completeness, but nothing checked whether the Gateway
        // was already busy, and train() on a busy Gateway is what the engine
        // rejected with ArrayIndexOutOfBoundsException (the "OrderSink.train
        // failed" spam that blocked the owner's Cybernetics Core).
        return Select.ourOneNotTrainingUnits(Protoss_Gateway);
    }
}