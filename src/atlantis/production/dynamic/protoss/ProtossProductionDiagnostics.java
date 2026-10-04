package atlantis.production.dynamic.protoss;

import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.production.dynamic.protoss.units.ProduceDragoon;
import atlantis.production.dynamic.protoss.units.ProduceZealot;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.log.ErrorLog;

/**
 * "We can afford units and there is nowhere to make them" - said out loud, once a minute.
 *
 * <p>B-22 was reported as "we build a zealot and a dragoon and then nothing" and stayed
 * open for a week, because the Protoss production path answers {@code false} through a
 * dozen gates and none of them says which one answered. Measured across the five games
 * in {@code ~/.scbw/games} on 2026-10-05: every one of them produced exactly one zealot
 * and one dragoon (or two zealots) and then no combat unit at all for the rest of the
 * game - 4 to 8 minutes of it - with 640-1368 gas gathered in four of the five, which
 * rules out "no resources" and leaves the gateway capacity question as the only gate
 * that can explain it.</p>
 *
 * <p>So the report is deliberately narrow: it fires only in the state that was reported
 * (enough minerals for a combat unit, at least one gateway, a cybernetics core, nothing
 * produced this call) and it prints the numbers that decide between the remaining
 * explanations - free gateways against gateways in total, the core, the gas, and the two
 * producers' own reasons. It goes through {@link ErrorLog}, so it is rate-limited in a
 * game <i>and</i> in a test world (the throttle works in stub worlds since the clock
 * publication fix), and it lands in the game's {@code bot.log} - which is the file the
 * bug report should have been able to answer from.</p>
 */
public class ProtossProductionDiagnostics {

    /**
     * Enough minerals for any combat unit we would consider; below this the bot is
     * correctly saving, and saying so would only add noise to the log.
     */
    private static final int RICH_ENOUGH_MINERALS = 600;

    public static void reportRichButIdle(boolean producedSomething) {
        if (producedSomething || !looksIdleWithResources()) return;

        ErrorLog.printMaxOncePerMinute(
            "Protoss rich but idle: min=" + A.minerals()
                + " gas=" + A.gas()
                + " supply=" + A.supplyUsed() + "/" + A.supplyTotal()
                + " gateways=" + Count.gateways() + " free=" + Count.freeGateways()
                + " core=" + Have.cyberneticsCore()
                + " army=" + Army.strength()
                + " | commander=" + ProtossDynamicUnitProductionCommander.reason
                + " dragoon=" + ProduceDragoon.reason
                + " zealot=" + ProduceZealot.reason
        );
    }

    /**
     * The reported state and nothing else. A bot that cannot afford units is working as
     * intended, and a bot with no gateway at all has a different problem (the buildings
     * doctrine), so neither is this report's business.
     */
    private static boolean looksIdleWithResources() {
        return A.minerals() >= RICH_ENOUGH_MINERALS
            && Count.gateways() > 0
            && Have.cyberneticsCore()
            && Army.strength() < 500;
    }
}