package atlantis.production.dynamic.protoss.buildings;

import atlantis.decisions.Decision;
import atlantis.decisions.board.BoardState;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.production.GoalBoardAdvisor;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

import static atlantis.units.AUnitType.Protoss_Fleet_Beacon;
import static atlantis.units.AUnitType.Protoss_Stargate;

public class ProduceStargate {
    public static boolean produce() {
        if (Count.ofTypeWithUnfinished(Protoss_Stargate) > 0) return false;
        if (ConstructionRequests.isBeingBuilt(Protoss_Stargate)) return false;

        BoardState board = GoalBoardAdvisor.snapshot();

        Decision decision = A.whenEnemyProtossTerranZerg(
            () -> produceAgainstProtoss(board),
            () -> produceAgainstTerran(board),
            () -> produceAgainstZerg(board)
        );

        if (decision.isTrue()) produceStargate();

        return false;
    }

    private static void produceStargate() {
        AddToQueue.withStandardPriority(Protoss_Stargate);
    }

    // NEXT #93 absorption track: the Terran branch reads the board's enemy
    // race instead of asking Enemy mid-chain. Protoss/Zerg branches keep
    // their Decision shape until their own slices land.
    private static Decision produceAgainstTerran(BoardState board) {
        if (!"Terran".equals(board.enemyRace)) return Decision.INDIFFERENT;

        return Decision.FORBIDDEN;
    }

    // NEXT #93 absorption track: the Protoss branch reads dragoons and
    // carrier pressure off the board. Same numbers, new home.
    private static Decision produceAgainstProtoss(BoardState board) {
        if (!"Protoss".equals(board.enemyRace)) return Decision.INDIFFERENT;
        if (board.dragoonCount <= 12) return Decision.FORBIDDEN;

        return Decision.fromBoolean(board.enemyCarrierTech);
    }

    // NEXT #93 absorption track: the Zerg branch absorbs the whole
    // TransitionToCorsairs ladder, read off the board. Same numbers, new
    // home - the battle class stays only while something else calls it.
    private static Decision produceAgainstZerg(BoardState board) {
        if (!"Zerg".equals(board.enemyRace)) return Decision.INDIFFERENT;

        if (board.dragoonCount <= 1) return Decision.FORBIDDEN;
        if (board.armyStrength <= 115 && board.combatUnits <= 5 && board.cannonCount <= 1) {
            return Decision.FORBIDDEN;
        }
        if (board.supplyUsed <= 40 && board.enemyHydras >= 10) return Decision.FORBIDDEN;
        if (board.supplyUsed <= 40) return Decision.FORBIDDEN;

        if (board.supplyUsed >= 70) return Decision.ALLOWED;
        if (board.supplyUsed >= 90 && board.armyStrength >= 95 && board.enemyLurkers <= 2) {
            return Decision.ALLOWED;
        }

        return Decision.fromBoolean(board.enemyMutalisks > 0);
    }
}
