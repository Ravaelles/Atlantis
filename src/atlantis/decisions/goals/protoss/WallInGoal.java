package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.units.AUnitType;

/**
 * Wall-in at the natural: Pylon + Gateway + cannons on choke anchors.
 *
 * <p>
 * Rush-window defense after the core is up, Zerg only (other matchups wall
 * later - a later goal, not a wider condition). Owner 2026-10-18: Forge
 * Expand openings only - on any other opening the pieces park a builder
 * on an anchor the Pylon field never reaches (measured: [49,107],
 * hasPower=false, 8 re-requests, pump strangled to bank 1894).
 * {@link #WANTED_PIECES} wall pieces stand near the natural choke; active
 * while fewer than that, the 99-sentinel (no natural choke on this map) reads as satisfied, because a
 * wall nobody can serve is noise, not a want. Not an emergency: a wall is
 * planned work, and emergencies break commitment. Price is the Pylon -
 * the wall starts with power.
 * </p>
 */
public final class WallInGoal implements Goal {

    public static final String ID = "wall-in";

    public static final int WANTED_PIECES = 3;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean active(BoardState state) {
        return "Zerg".equals(state.enemyRace)
            && state.hasCyberneticsCore
            && state.ourForgeExpand
            && state.wallPiecesAtNatural < WANTED_PIECES;
    }

    @Override
    public int rank() {
        return GoalRanks.WALL_IN;
    }

    @Override
    public int priceMinerals() {
        return AUnitType.Protoss_Pylon.mineralPrice();
    }

    @Override
    public int priceGas() {
        return AUnitType.Protoss_Pylon.gasPrice();
    }
}
