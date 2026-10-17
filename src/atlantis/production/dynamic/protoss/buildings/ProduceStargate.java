package atlantis.production.dynamic.protoss.buildings;

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

        // NEXT #93 absorption track, final: branches answer Boolean on the
        // board (null = indifferent, try nothing further - exactly one race
        // branch applies). No Decision type left in this file.
        Boolean want;
        if ("Protoss".equals(board.enemyRace)) want = produceAgainstProtoss(board);
        else if ("Terran".equals(board.enemyRace)) want = produceAgainstTerran(board);
        else want = produceAgainstZerg(board);

        if (want != null && want) produceStargate();

        return false;
    }

    private static void produceStargate() {
        AddToQueue.withStandardPriority(Protoss_Stargate);
    }

    private static Boolean produceAgainstTerran(BoardState board) {
        if (!"Terran".equals(board.enemyRace)) return null;

        return false;
    }

    // NEXT #93 absorption track: the Protoss branch reads dragoons and
    // carrier pressure off the board. Same numbers, new home.
    private static Boolean produceAgainstProtoss(BoardState board) {
        if (!"Protoss".equals(board.enemyRace)) return null;
        if (board.dragoonCount <= 12) return false;

        return board.enemyCarrierTech;
    }

    // NEXT #93 absorption track: the Zerg branch absorbs the whole
    // TransitionToCorsairs ladder, read off the board. Same numbers, new
    // home - the battle class stays only while something else calls it.
    private static Boolean produceAgainstZerg(BoardState board) {
        if (!"Zerg".equals(board.enemyRace)) return null;

        if (board.dragoonCount <= 1) return false;
        if (board.armyStrength <= 115 && board.combatUnits <= 5 && board.cannonCount <= 1) {
            return false;
        }
        if (board.supplyUsed <= 40 && board.enemyHydras >= 10) return false;
        if (board.supplyUsed <= 40) return false;

        if (board.supplyUsed >= 70) return true;
        if (board.supplyUsed >= 90 && board.armyStrength >= 95 && board.enemyLurkers <= 2) {
            return true;
        }

        return board.enemyMutalisks > 0;
    }
}
