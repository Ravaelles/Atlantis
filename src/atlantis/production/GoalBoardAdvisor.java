package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.decisions.board.GoalBoard;
import atlantis.decisions.goals.protoss.ProtossOpening;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.BaseUnderAttack;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.We;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

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
        Goal top = GoalBoard.top(state, ProtossOpening.goals(), GoalBoardFeeder.inFlightIds());

        StringBuilder active = new StringBuilder();
        for (Goal goal : ranked) {
            if (active.length() > 0) active.append(",");
            active.append(goal.id());
        }

        // JSON-only since 2026-10-15 (owner: no red spam in the game
        // log): the opinion lands in EVENTS.jsonl under BOARD. Funded
        // rides along since the wallet: which wants the bank covers.
        StringBuilder funded = new StringBuilder();
        for (String id : GoalBoardFeeder.fundedIds(state, ranked)) {
            if (funded.length() > 0) funded.append(",");
            funded.append(id);
        }
        EventLog.key(LogKey.BOARD)
            .param("top", top == null ? "-" : top.id())
            .param("active", active.toString())
            .param("funded", funded.toString())
            .param("minerals", state.minerals)
            .param("supplyUsed", state.supplyUsed)
            .param("supplyTotal", state.supplyTotal)
            .param("workers", state.workerCount)
            .log(A.now(), A.minSec());

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
            .gatewaysTraining(Math.max(0, Count.gateways() - Count.freeGateways()))
            .army(Count.ourCombatUnits())
            .core(Have.cyberneticsCore())
            .coreAwaitingSupply(coreAwaitingSupply())
            .assimilator(Have.assimilator())
            .forge(Have.forge())
            .cannons(Count.cannons())
            .observers(Count.observers())
            .hiddenUnits(EnemyInfo.goesOrHasHiddenUnits())
            .underAttack(BaseUnderAttack.check())
            .seconds(A.seconds())
            .enemyRace(enemyRaceName())
            .dragoons(Count.dragoons())
            .dragoonsUnfinished(Count.dragoonsWithUnfinished())
            .armyStrength(atlantis.information.generic.Army.strength())
            .enemyUnits(
                atlantis.information.enemy.EnemyUnits.hydras(),
                atlantis.information.enemy.EnemyUnits.lurkers(),
                atlantis.information.enemy.EnemyUnits.mutas(),
                enemyCarrierTech())
            .sparta(atlantis.battle.Missions.isGlobalMissionSparta())
            .wallPieces(wallPiecesAtNatural())
            .armyStrengthWithoutCB(atlantis.information.generic.Army.strengthWithoutCB())
            .enemyHasRanged(atlantis.information.enemy.EnemyInfo.hasRanged())
            .enemyGoingHidden(atlantis.information.strategy.Strategy.get().isGoingHiddenUnits())
            .gatewaysUnfinished(Count.gatewaysWithUnfinished())
            .forgeExpand(isForgeExpand())
            .build();
    }

    /**
     * Our opening is Forge Expand (owner 2026-10-18: the natural wall-in
     * only makes sense on FE). Null-safe: the snapshot must survive a
     * half-initialised game, same as {@code Strategy.current()}.
     */
    private static boolean isForgeExpand() {
        atlantis.information.strategy.AStrategy strategy =
            atlantis.information.strategy.Strategy.current();
        return strategy != null && strategy.nameContains("Forge FE");
    }

    /**
     * Wall pieces (Pylon/Gateway/Cannon, standing or unfinished) near the
     * natural choke center. 99 when no natural choke exists (the goal reads
     * it as satisfied). Radius covers anchors plus one footprint - pieces
     * belong to the wall by proximity, exactness is the planner's job.
     */
    private static int wallPiecesAtNatural() {
        atlantis.map.choke.AChoke choke = atlantis.map.choke.Chokes.natural();
        if (choke == null || choke.center() == null) return 99;
        atlantis.map.position.APosition center = choke.center();
        return Count.ourWithUnfinished(AUnitType.Protoss_Pylon, 12, center)
            + Count.ourWithUnfinished(AUnitType.Protoss_Gateway, 12, center)
            + Count.ourWithUnfinished(AUnitType.Protoss_Photon_Cannon, 12, center);
    }

    /**
     * Enemy-race posture (NEXT #93 absorption track): the matchup branch of
     * every producer reads this instead of asking {@code Enemy} mid-chain.
     * First consumer: the Stargate Terran veto.
     */
    private static String enemyRaceName() {
        if (atlantis.game.player.Enemy.terran()) return "Terran";
        if (atlantis.game.player.Enemy.zerg()) return "Zerg";
        if (atlantis.game.player.Enemy.protoss()) return "Protoss";
        return "Unknown";
    }

    /**
     * Carrier pressure the Stargate branch answers to: discovered carriers
     * or a Fleet Beacon going up. Collapsed here so consumers read one
     * posture bit, never two unit queries.
     */
    private static boolean enemyCarrierTech() {
        atlantis.units.select.Selection enemies =
            atlantis.information.enemy.EnemyUnits.discovered();
        return enemies.carriers().notEmpty()
            || enemies.ofType(AUnitType.Protoss_Fleet_Beacon).notEmpty();
    }

    /**
     * The worker Core-veto posture (NEXT #93 absorption track): a Core
     * construction is requested but unstarted while its order is already
     * supply-unlocked. Verbatim the old trio's only load-bearing branch.
     */
    private static boolean coreAwaitingSupply() {
        if (ConstructionRequests.countNotStartedOfType(AUnitType.Protoss_Cybernetics_Core) == 0) {
            return false;
        }
        ProductionOrder order = Queue.get().notFinished().ofType(AUnitType.Protoss_Cybernetics_Core).first();
        return order != null && order.supplyRequirementFulfilled(0);
    }
}
