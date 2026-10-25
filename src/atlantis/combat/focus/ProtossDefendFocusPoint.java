package atlantis.combat.focus;

import atlantis.battle.advance.focus.AFocusPoint;
import atlantis.combat.cluster.ClusterRally;
import atlantis.battle.cluster.Missions;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.map.base.Bases;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

/**
 * Where the Protoss army stands when it defends. Resurrected from the old
 * {@code combat.missions.defend.MissionDefendFocusPoint} (deleted in the
 * combat cutover) because it worked: breach answers first, then the
 * choke, then the base - never the mineral line. Only the Protoss
 * branches survived the port: the Zerg sunken ring and the Terran
 * bunker branches died with their races (owner direction - a dedicated
 * version per race instead of one chain of race-ifs).
 *
 * <p>One deliberate change versus the old version, from a tested loss
 * vs Protoss (owner 2026-10-26): the main-choke stand sits 8 tiles
 * toward the main instead of on the choke center. Holding at the base
 * leaves no room to retreat once the fight is lost, and a 5+ army
 * retreating into the base blocks itself - the standoff keeps the line
 * facing out with room behind it. Everything else (breach target,
 * sparta choke, expansion siting, decision order, 29-frame cache) is
 * verbatim old behavior.</p>
 */
public final class ProtossDefendFocusPoint {

    private ProtossDefendFocusPoint() {
    }

    private static final Cache<AFocusPoint> CACHE = new Cache<>();

    /**
     * The focus re-derives about twice per second; preserved verbatim
     * cadence, tune only with a measured cadence (CONVENTIONS §31).
     */
    private static final int FOCUS_TTL_FRAMES = 29;

    public static void clearForTests() {
        CACHE.forget("focusPoint");
    }

    public static AFocusPoint focusPoint() {
        try {
            return (AFocusPoint) CACHE.get(
                "focusPoint",
                FOCUS_TTL_FRAMES,
                () -> {
                    if (A.isUms()) {
                        return null;
                    }

                    AUnit mainBase = Select.main();
                    if (mainBase == null) {
                        return null;
                    }

                    // === Inputs: what qualifies ===================
                    // The priority between them lives in
                    // DefendedPlace.defendGround (NEXT #194) - this
                    // only resolves the candidates, then builds the
                    // focus the answer names (the HOW stays here).

                    AUnit enemyInBase = EnemyUnitBreachedBase.get();
                    if (enemyInBase != null && !enemyInBase.isAlive()) enemyInBase = null;

                    AChoke mainChoke = Chokes.mainChoke();
                    Selection unfinishedBases = Select.ourWithUnfinished().bases();
                    boolean sparta = unfinishedBases.count() <= 1
                        && Missions.isGlobalMissionSparta();

                    // The main stand resolves here so the HOW below
                    // reuses it: the choke answers the mission only
                    // when there is room to stand on it.
                    AFocusPoint mainStand = atMainChokeStandoff(mainBase);

                    boolean multiBase = unfinishedBases.count() >= 2;
                    AChoke naturalChoke = multiBase ? Chokes.natural() : null;
                    AUnit natural = multiBase ? Bases.natural() : null;
                    AUnit lastBase = null;
                    if (multiBase && AtlantisRaceConfig.BASE != null) {
                        lastBase = Select.ourWithUnfinishedOfType(AtlantisRaceConfig.BASE)
                            .mostDistantTo(mainBase);
                    }

                    DefendedPlace.Answer answer = DefendedPlace.defendGround(
                        enemyInBase,
                        sparta ? mainChoke : null,
                        mainStand != null ? mainChoke : null,
                        naturalChoke != null && natural != null ? naturalChoke : null,
                        lastBase);
                    if (answer == null) return null;

                    switch (answer.kind) {
                        case BREACH:
                            return new AFocusPoint(
                                enemyInBase,
                                "EnemyBreachedBase"
                            );
                        case SPARTA_CHOKE:
                            return new AFocusPoint(
                                mainChoke,
                                mainBase,
                                "Choke300"
                            );
                        case MAIN_CHOKE:
                            return mainStand;
                        case NATURAL_CHOKE:
                            return new AFocusPoint(
                                naturalChoke.translateTilesTowards(5, natural),
                                natural,
                                "NaturalChoke"
                            );
                        case BASE:
                            return new AFocusPoint(
                                lastBase,
                                "LastBase"
                            );
                        default:
                            return null;
                    }
                }
            );
        }
        catch (RuntimeException ignored) {
            return null;
        }
    }

    // =========================================================

    /**
     * True when the known enemy cannot shoot over a mouth plug: combat
     * units exist and none is ranged. Workers-only and empty reads keep
     * the old stand - the plug answers an army, not a scout.
     */
    private static boolean enemyMeleeOnly() {
        try {
            atlantis.units.select.Selection foes = Select.enemy().combatUnits();
            return foes != null && foes.notEmpty() && foes.ranged().empty();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    /**
     * The Protoss defend stand: 8 tiles from the main choke center toward
     * the main (owner 2026-10-26, tested loss vs Protoss). Null when there
     * is no choke or no room to stand - the caller falls back to the base,
     * same as the old code's null path.
     */
    private static AFocusPoint atMainChokeStandoff(AUnit mainBase) {
        if (mainBase == null || mainBase.position() == null) return null;

        // PurpleWave guard: against melee-only enemies the line waits AT
        // the mouth (melee plugs, dragoons second row), not 8 tiles back
        // - the standoff depth stays for ranged enemies, which shoot over
        // a mouth plug. NEXT #170's depth rule survives for that case.
        if (enemyMeleeOnly()) {
            AChoke choke = Chokes.mainChoke();
            APosition plug = atlantis.combat.formation.ChokePlug.plugPointFor(choke, mainBase);
            if (plug != null) {
                return new AFocusPoint(
                    plug,
                    mainBase,
                    "MainChoke"
                );
            }
        }

        HasPosition standoff = ClusterRally.chokeStandoffPoint();
        if (standoff == null) return null;

        return new AFocusPoint(
            standoff,
            mainBase,
            "MainChoke"
        );
    }
}
