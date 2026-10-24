package atlantis.battle.cluster;

import atlantis.battle.advance.focus.AFocusPoint;
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

    public static void clearForTests() {
        CACHE.forget("focusPoint");
    }

    public static AFocusPoint focusPoint() {
        try {
            return (AFocusPoint) CACHE.get(
                "focusPoint",
                29,
                () -> {
                    if (A.isUms()) {
                        return null;
                    }

                    AUnit mainBase = Select.main();
                    if (mainBase == null) {
                        return null;
                    }

                    // === Enemies that breached into base ================

                    AUnit enemyInBase = EnemyUnitBreachedBase.get();
                    if (enemyInBase != null && enemyInBase.isAlive()) {
                        return new AFocusPoint(
                            enemyInBase,
                            "EnemyBreachedBase"
                        );
                    }

                    // ====================================================

                    Selection basesWithUnfinished = Select.ourWithUnfinished().bases();
                    if (basesWithUnfinished.count() <= 1 && Missions.isGlobalMissionSparta()) {
                        AChoke mainChoke = Chokes.mainChoke();
                        if (mainChoke != null) {
                            return new AFocusPoint(
                                mainChoke,
                                mainBase,
                                "Choke300"
                            );
                        }
                    }

                    // === Main choke standoff (8 tiles home-side, tested) =

                    AFocusPoint focus = atMainChokeStandoff(mainBase);
                    if (focus != null) {
                        return focus;
                    }

                    // ====================================================

                    if (basesWithUnfinished.count() >= 2) {
                        focus = atAnyBase(mainBase);
                        if (focus != null) {
                            return focus;
                        }
                    }

                    return null;
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

    private static AFocusPoint atAnyBase(AUnit mainBase) {
        if (AtlantisRaceConfig.BASE == null) return null;

        AUnit lastBase = Select.ourWithUnfinishedOfType(AtlantisRaceConfig.BASE)
            .mostDistantTo(mainBase);
        if (lastBase == null) return null;

        // === At natural =========================================

        AChoke naturalChoke = Chokes.natural();
        AUnit natural = Bases.natural();
        if (naturalChoke != null && natural != null) {
            return new AFocusPoint(
                naturalChoke.translateTilesTowards(5, natural),
                natural,
                "NaturalChoke"
            );
        }

        // === Standard ===========================================

        return new AFocusPoint(
            lastBase,
            "LastBase"
        );
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
            APosition plug = atlantis.battle.formation.ChokePlug.plugPointFor(choke, mainBase);
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
