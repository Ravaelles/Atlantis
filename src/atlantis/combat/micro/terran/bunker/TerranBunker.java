package atlantis.combat.micro.terran.bunker;

import atlantis.combat.missions.Missions;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.requests.AntiLandBuildingCommander;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class TerranBunker extends AntiLandBuildingCommander {
    @Override
    public AUnitType type() {
        return AUnitType.Terran_Bunker;
    }

    @Override
    public int expected() {
        if (EnemyInfo.isDoingEarlyGamePush()) {
            return Enemy.zerg() ? 2 : 1;
        }

        return 1;
    }

    @Override
    public boolean shouldBuildNew() {
        if (!Have.barracks()) return false;

        return Count.bases() <= 1
            ? ShouldBuildBunkerIfOneBase.shouldBuild()
            : ShouldBuildBunkerIfManyBases.shouldBuild();
    }

    protected static int existingOrInProduction() {
        return Count.existingOrInProductionOrInQueue(AUnitType.Terran_Bunker);
    }

    @Override
    public boolean handleBuildNew() {
        if (
            GamePhase.isEarlyGame()
                && OurStrategy.get().isRushOrCheese()
                && EnemyUnits.discovered().combatUnits().atMost(Enemy.zerg() ? 10 : 5)
        ) return false;

        if (Count.bases() >= 2 && existingOrInProduction() < Count.bases()) {
            if (handleNaturalBunker()) return true;
        }

        return super.handleBuildNew();
    }

    // =========================================================

    private boolean handleNaturalBunker() {
        if (Count.bases() < 2) return false;

        AChoke naturalChoke = Chokes.natural();
        AUnit naturalBase = Select.ourBases().second();
        if (naturalBase != null && naturalChoke != null) {
            if (Count.existingOrPlannedBuildingsNear(type(), 6, naturalBase) == 0) {
                return reinforcePosition(naturalBase.translateTilesTowards(5, naturalChoke), false);
            }
        }

        return false;
    }

    private boolean handleMissionContain() {
        if (!Missions.isGlobalMissionContain()) return false;

        APosition focusPoint = Missions.globalMission().focusPoint();
        if (focusPoint == null) return false;

        return reinforcePosition(focusPoint, true);
    }

    private boolean reinforcePosition(HasPosition position, boolean checkReservedMinerals) {
        if (!Have.existingOrPlannedOrInQueue(type(), position, 12)) {
//            if (checkReservedMinerals ? AGame.canAffordWithReserved(84, 0) : AGame.canAfford(70, 0)) {
            if (checkReservedMinerals ? AGame.canAffordWithReserved(84, 0) : Count.ourCombatUnits() >= 2) {
                AddToQueue.withTopPriority(type(), position);
                return true;
            }
        }

        return false;
    }

    // =========================================================

//    public TerranBunker getInstance() {
//        if (instance == null) {
//            return (TerranBunker) (instance = new TerranBunker());
//        }
//
//        return (TerranBunker) instance;
//    }

}