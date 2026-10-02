package tests.fakes;

import atlantis.units.AUnit;
import atlantis.units.OrderSink;
import bwapi.Position;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwapi.UpgradeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage D: recording {@link OrderSink} for tests. Captures every order that
 * production code routes through {@code AUnit.orderSink()}, so tests can
 * assert on issued orders without a running game.
 */
public class FakeOrderSink implements OrderSink {

    public static class Record {
        public final String operation;
        public final int actorId;
        public final String detail;

        public Record(String operation, int actorId, String detail) {
            this.operation = operation;
            this.actorId = actorId;
            this.detail = detail;
        }

        @Override
        public String toString() {
            return operation + " by " + actorId + (detail.isEmpty() ? "" : " " + detail);
        }
    }

    private final List<Record> records = new ArrayList<>();
    private boolean resultToReturn = true;

    public List<Record> orders() {
        return records;
    }

    public void clear() {
        records.clear();
    }

    public void resultToReturn(boolean resultToReturn) {
        this.resultToReturn = resultToReturn;
    }

    private boolean record(String operation, AUnit actor, String detail) {
        records.add(new Record(operation, actor.id(), detail));
        return resultToReturn;
    }

    private boolean record(String operation, AUnit actor) {
        return record(operation, actor, "");
    }

    @Override
    public boolean attackUnit(AUnit actor, AUnit target) {
        return record("attackUnit", actor, String.valueOf(target.id()));
    }

    @Override
    public boolean train(AUnit actor, UnitType type) {
        return record("train", actor, String.valueOf(type));
    }

    @Override
    public boolean morph(AUnit actor, UnitType type) {
        return record("morph", actor, String.valueOf(type));
    }

    @Override
    public boolean build(AUnit actor, UnitType type, TilePosition position) {
        return record("build", actor, type + " @ " + position);
    }

    @Override
    public boolean buildAddon(AUnit actor, UnitType type) {
        return record("buildAddon", actor, String.valueOf(type));
    }

    @Override
    public boolean upgrade(AUnit actor, UpgradeType type) {
        return record("upgrade", actor, String.valueOf(type));
    }

    @Override
    public boolean research(AUnit actor, TechType type) {
        return record("research", actor, String.valueOf(type));
    }

    @Override
    public boolean move(AUnit actor, Position position) {
        return record("move", actor, String.valueOf(position));
    }

    @Override
    public boolean patrol(AUnit actor, Position position) {
        return record("patrol", actor, String.valueOf(position));
    }

    @Override
    public boolean holdPosition(AUnit actor) {
        return record("holdPosition", actor);
    }

    @Override
    public boolean stop(AUnit actor) {
        return record("stop", actor);
    }

    @Override
    public boolean follow(AUnit actor, AUnit target) {
        return record("follow", actor, String.valueOf(target.id()));
    }

    @Override
    public boolean gather(AUnit actor, AUnit target) {
        return record("gather", actor, String.valueOf(target.id()));
    }

    @Override
    public boolean repair(AUnit actor, AUnit target) {
        return record("repair", actor, String.valueOf(target.id()));
    }

    @Override
    public boolean burrow(AUnit actor) {
        return record("burrow", actor);
    }

    @Override
    public boolean unburrow(AUnit actor) {
        return record("unburrow", actor);
    }

    @Override
    public boolean cloak(AUnit actor) {
        return record("cloak", actor);
    }

    @Override
    public boolean decloak(AUnit actor) {
        return record("decloak", actor);
    }

    @Override
    public boolean siege(AUnit actor) {
        return record("siege", actor);
    }

    @Override
    public boolean unsiege(AUnit actor) {
        return record("unsiege", actor);
    }

    @Override
    public boolean lift(AUnit actor) {
        return record("lift", actor);
    }

    @Override
    public boolean land(AUnit actor, TilePosition position) {
        return record("land", actor, String.valueOf(position));
    }

    @Override
    public boolean load(AUnit actor, AUnit target) {
        return record("load", actor, String.valueOf(target.id()));
    }

    @Override
    public boolean unload(AUnit actor, AUnit target) {
        return record("unload", actor, String.valueOf(target.id()));
    }

    @Override
    public boolean unloadAll(AUnit actor) {
        return record("unloadAll", actor);
    }

    @Override
    public boolean unloadAllAt(AUnit actor, Position position) {
        return record("unloadAllAt", actor, String.valueOf(position));
    }

    @Override
    public boolean rightClick(AUnit actor, AUnit target) {
        return record("rightClick", actor, String.valueOf(target.id()));
    }

    @Override
    public boolean haltConstruction(AUnit actor) {
        return record("haltConstruction", actor);
    }

    @Override
    public boolean cancelConstruction(AUnit actor) {
        return record("cancelConstruction", actor);
    }

    @Override
    public boolean cancelAddon(AUnit actor) {
        return record("cancelAddon", actor);
    }

    @Override
    public boolean cancelTrain(AUnit actor) {
        return record("cancelTrain", actor);
    }

    @Override
    public boolean cancelTrainSlot(AUnit actor, int slot) {
        return record("cancelTrainSlot", actor, String.valueOf(slot));
    }

    @Override
    public boolean cancelMorph(AUnit actor) {
        return record("cancelMorph", actor);
    }

    @Override
    public boolean cancelResearch(AUnit actor) {
        return record("cancelResearch", actor);
    }

    @Override
    public boolean cancelUpgrade(AUnit actor) {
        return record("cancelUpgrade", actor);
    }

    @Override
    public boolean useTech(AUnit actor, TechType tech) {
        return record("useTech", actor, String.valueOf(tech));
    }

    @Override
    public boolean useTechAt(AUnit actor, TechType tech, Position position) {
        return record("useTechAt", actor, tech + " @ " + position);
    }

    @Override
    public boolean useTechOn(AUnit actor, TechType tech, AUnit target) {
        return record("useTechOn", actor, tech + " @ " + target.id());
    }
}
