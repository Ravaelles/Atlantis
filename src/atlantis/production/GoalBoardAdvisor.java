package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.decisions.board.GoalBoard;
import atlantis.decisions.goals.protoss.ProtossOpening;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.units.BaseUnderAttack;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.We;

import java.util.Collections;
import java.util.List;

/**
 * Goal-board advisor (NEXT #91): computes the board's opinion every ~60
 * game-seconds and logs it next to what legacy production actually does.
 * Zero behavior change - pure observability. Divergence lines
 * ("board says X, legacy did Y") are the data the worker-pump driver
 * commit will be measured against.
 *
 * <p>v1 limits, stated plainly: in-flight set is empty (commitment proves
 * in unit tests, not here); Have.* counts planned-as-standing; the ladder
 * is matchup-blind (legacy keeps all matchup gates).</p>
 */
public class GoalBoardAdvisor extends Commander {

    @Override
    public boolean applies() {
        return We.protoss() && A.everyNthGameFrame(1500);
    }

    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[0];
    }

    @Override
    protected boolean handle() {
        BoardState state = snapshot();
        List<Goal> ranked = GoalBoard.ranked(state, ProtossOpening.goals());
        Goal top = GoalBoard.top(state, ProtossOpening.goals(), Collections.<String>emptySet());

        StringBuilder active = new StringBuilder();
        for (Goal goal : ranked) {
            if (active.length() > 0) active.append(",");
            active.append(goal.id());
        }

        atlantis.util.log.ErrorLog.printMaxOncePerMinute(
            "BOARD top=" + (top == null ? "-" : top.id())
                + " active=[" + active + "]"
                + " min=" + state.minerals
                + " sup=" + state.supplyUsed + "/" + state.supplyTotal
                + " workers=" + state.workerCount);

        return false;
    }

    /**
     * The single seam where engine reads become board data. Every field the
     * goals need is read here, once - the board package itself stays
     * engine-free (ArchUnit-clean by construction).
     */
    public static BoardState snapshot() {
        return BoardState.builder()
            .minerals(A.minerals())
            .gas(A.gas())
            .supply(A.supplyUsed(), A.supplyTotal())
            .workers(Count.workers())
            .bases(Count.bases())
            .gateways(Count.gateways())
            .army(Count.ourCombatUnits())
            .core(Have.cyberneticsCore())
            .assimilator(Have.assimilator())
            .forge(Have.forge())
            .cannons(Count.cannons())
            .observers(Count.observers())
            .hiddenUnits(EnemyInfo.goesOrHasHiddenUnits())
            .underAttack(BaseUnderAttack.check())
            .seconds(A.seconds())
            .build();
    }
}
