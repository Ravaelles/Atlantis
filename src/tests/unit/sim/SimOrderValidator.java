package tests.unit.sim;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import tests.fakes.FakeUnit;

import java.util.List;

/**
 * Whether the engine would take an order, decided before it moves sim
 * state. The rules mirror OpenBW {@code can_place_building} (read
 * 2026-10-27, not copied) minus the bits a tile snapshot cannot see:
 *
 * <ul>
 * <li>BUILD: footprint in-bounds, every footprint tile buildable,
 * builder reaches the site (region graph), psi for Protoss (pylon
 * within {@link #PSI_TILES}), refinery only on a geyser tile,
 * bank covers the cost. Creep/occupancy/unk4 have no snapshot
 * equivalent and stay unmodeled (documented gap, not a silent pass:
 * occupancy against sim buildings is checked).</li>
 * <li>MOVE: destination walkable, path from the actor (region graph).
 * Real StarCraft accepts unreachable moves and slides; the sim
 * validates strictly because a builder that cannot arrive is a lost
 * building - the rejection names it instead of hanging the scenario.</li>
 * <li>TRAIN/RESEARCH/UPGRADE: facility alive and completed, bank
 * covers the cost. Queue depth is the driver's business.</li>
 * </ul>
 */
public final class SimOrderValidator {

    /**
     * Pylon psi reach in tiles (engine mask is 256x160px around the
     * pylon; 8 tiles covers it symmetrically and never over-claims
     * a powered site as unpowered).
     */
    static final double PSI_TILES = 8;

    private SimOrderValidator() {
    }

    /**
     * BUILD verdict. Funding is optional: the sim validates placement
     * at request time but deducts at warp start (the builder walks
     * while the bank fills, like the live game), so request validation
     * passes {@code checkBank = false} and warp start passes true.
     *
     * <p>Occupancy is deliberately NOT checked here: the live game
     * permits the request and kills it at warp start (proximity finish),
     * and the bot's own last-resort path bypasses its sweep guard the
     * same way. Vetoing at request time would test this validator
     * instead of the bot's placement ({@code WO-145}). The warp-start
     * occupancy check lives in {@code SimConstructions} and cancels
     * like the engine does.</p>
     */
    public static SimOrder validateBuild(SimGround ground, FakeUnit builder, AUnitType type,
            int tx, int ty, SimBank bank, List<FakeUnit> ourBuildings, boolean checkBank) {
        APosition tile = APosition.create(tx, ty);
        if (!ground.inBounds(tx, ty)
                || !ground.inBounds(tx + type.getTilesWidth() - 1, ty + type.getTilesHeights() - 1)) {
            return SimOrder.rejected(SimOrder.Verb.BUILD, builder, type, tile,
                "footprint off the map");
        }
        if (type.isGasBuilding()) {
            if (!ground.isGeyserTile(tx, ty)) {
                return SimOrder.rejected(SimOrder.Verb.BUILD, builder, type, tile,
                    "refinery without a geyser");
            }
        }
        else if (!ground.footprintBuildable(type, tx, ty)) {
            return SimOrder.rejected(SimOrder.Verb.BUILD, builder, type, tile,
                "footprint not buildable");
        }
        if (!ground.hasPathBetween(builder, tile)) {
            return SimOrder.rejected(SimOrder.Verb.BUILD, builder, type, tile,
                "builder cannot reach the site");
        }
        if (type.isProtoss() && type.ut().requiresPsi() && !coveredByPsi(ourBuildings, tile)) {
            return SimOrder.rejected(SimOrder.Verb.BUILD, builder, type, tile,
                "no psi coverage (no finished Pylon in range)");
        }
        if (checkBank && !bank.canAfford(type.mineralPrice(), type.gasPrice())) {
            return SimOrder.rejected(SimOrder.Verb.BUILD, builder, type, tile,
                "bank cannot cover " + type.mineralPrice() + "/" + type.gasPrice());
        }

        return SimOrder.accepted(SimOrder.Verb.BUILD, builder, type, tile);
    }

    /** MOVE verdict: walkable destination with a path from the actor. */
    public static SimOrder validateMove(SimGround ground, FakeUnit unit, APosition dest) {
        if (dest == null) {
            return SimOrder.rejected(SimOrder.Verb.MOVE, unit, null, null, "null destination");
        }
        if (!ground.isWalkableTile(dest.tx(), dest.ty())) {
            return SimOrder.rejected(SimOrder.Verb.MOVE, unit, null, dest, "destination not walkable");
        }
        if (!ground.hasPathBetween(unit, dest)) {
            return SimOrder.rejected(SimOrder.Verb.MOVE, unit, null, dest, "no path from the actor");
        }

        return SimOrder.accepted(SimOrder.Verb.MOVE, unit, null, dest);
    }

    /** TRAIN verdict: live completed facility plus the bank. */
    public static SimOrder validateTrain(FakeUnit facility, AUnitType type, SimBank bank) {
        if (facility == null || !facility.isAlive()) {
            return SimOrder.rejected(SimOrder.Verb.TRAIN, facility, type, null, "no live facility");
        }
        if (!facility.isCompleted()) {
            return SimOrder.rejected(SimOrder.Verb.TRAIN, facility, type, null,
                "facility still warping");
        }
        if (!bank.canAfford(type.mineralPrice(), type.gasPrice())) {
            return SimOrder.rejected(SimOrder.Verb.TRAIN, facility, type, null,
                "bank cannot cover " + type.mineralPrice() + "/" + type.gasPrice());
        }

        return SimOrder.accepted(SimOrder.Verb.TRAIN, facility, type, null);
    }

    /** RESEARCH/UPGRADE verdict: same shape as TRAIN. */
    public static SimOrder validateResearch(FakeUnit facility, AUnitType ignored, SimBank bank,
            int mineralCost, int gasCost, boolean isUpgrade) {
        SimOrder.Verb verb = isUpgrade ? SimOrder.Verb.UPGRADE : SimOrder.Verb.RESEARCH;
        if (facility == null || !facility.isAlive()) {
            return SimOrder.rejected(verb, facility, null, null, "no live facility");
        }
        if (!facility.isCompleted()) {
            return SimOrder.rejected(verb, facility, null, null, "facility still warping");
        }
        if (!bank.canAfford(mineralCost, gasCost)) {
            return SimOrder.rejected(verb, facility, null, null,
                "bank cannot cover " + mineralCost + "/" + gasCost);
        }

        return SimOrder.accepted(verb, facility, null, null);
    }

    private static boolean coveredByPsi(List<FakeUnit> ourBuildings, APosition tile) {
        for (FakeUnit other : ourBuildings) {
            if (other == null || !other.isAlive() || !other.isCompleted()) continue;
            if (!AUnitType.Protoss_Pylon.equals(other.type())) continue;
            try {
                if (other.position().distTo(tile) <= PSI_TILES) return true;
            } catch (RuntimeException ignored) {
            }
        }

        return false;
    }
}
