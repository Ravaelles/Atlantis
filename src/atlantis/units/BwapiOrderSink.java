package atlantis.units;

import atlantis.util.log.ErrorLog;
import bwapi.Position;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwapi.UpgradeType;

/**
 * Stage D (see _AI/REVIEW.md §16): {@link OrderSink}wired to the live engine.
 *
 * <p>This is the only production class allowed to invoke order methods on a
 * raw {@code bwapi.Unit}. Every method guards the engine call: an engine
 * exception is logged (throttled) and reported as {@code false} instead of
 * propagating — a failed order must never kill the bot.</p>
 */
public final class BwapiOrderSink implements OrderSink {

    private static final BwapiOrderSink INSTANCE = new BwapiOrderSink();

    private BwapiOrderSink() {
    }

    public static BwapiOrderSink instance() {
        return INSTANCE;
    }

    @Override
    public boolean attackUnit(AUnit actor, AUnit target) {
        return issue("attackUnit", actor, () -> actor.u().attack(target.u()));
    }

    @Override
    public boolean train(AUnit actor, UnitType type) {
        return issue("train", actor, () -> actor.u().train(type));
    }

    @Override
    public boolean morph(AUnit actor, UnitType type) {
        return issue("morph", actor, () -> actor.u().morph(type));
    }

    @Override
    public boolean build(AUnit actor, UnitType type, TilePosition position) {
        return issue("build", actor, () -> actor.u().build(type, position));
    }

    @Override
    public boolean buildAddon(AUnit actor, UnitType type) {
        return issue("buildAddon", actor, () -> actor.u().buildAddon(type));
    }

    @Override
    public boolean upgrade(AUnit actor, UpgradeType type) {
        return issue("upgrade", actor, () -> actor.u().upgrade(type));
    }

    @Override
    public boolean research(AUnit actor, TechType type) {
        return issue("research", actor, () -> actor.u().research(type));
    }

    @Override
    public boolean move(AUnit actor, Position position) {
        return issue("move", actor, () -> actor.u().move(position));
    }

    @Override
    public boolean patrol(AUnit actor, Position position) {
        return issue("patrol", actor, () -> actor.u().patrol(position));
    }

    @Override
    public boolean holdPosition(AUnit actor) {
        return issue("holdPosition", actor, () -> actor.u().holdPosition());
    }

    @Override
    public boolean stop(AUnit actor) {
        return issue("stop", actor, () -> actor.u().stop());
    }

    @Override
    public boolean follow(AUnit actor, AUnit target) {
        return issue("follow", actor, () -> actor.u().follow(target.u()));
    }

    @Override
    public boolean gather(AUnit actor, AUnit target) {
        return issue("gather", actor, () -> actor.u().gather(target.u()));
    }

    @Override
    public boolean repair(AUnit actor, AUnit target) {
        return issue("repair", actor, () -> actor.u().repair(target.u()));
    }

    @Override
    public boolean burrow(AUnit actor) {
        return issue("burrow", actor, () -> actor.u().burrow());
    }

    @Override
    public boolean unburrow(AUnit actor) {
        return issue("unburrow", actor, () -> actor.u().unburrow());
    }

    @Override
    public boolean cloak(AUnit actor) {
        return issue("cloak", actor, () -> actor.u().cloak());
    }

    @Override
    public boolean decloak(AUnit actor) {
        return issue("decloak", actor, () -> actor.u().decloak());
    }

    @Override
    public boolean siege(AUnit actor) {
        return issue("siege", actor, () -> actor.u().siege());
    }

    @Override
    public boolean unsiege(AUnit actor) {
        return issue("unsiege", actor, () -> actor.u().unsiege());
    }

    @Override
    public boolean lift(AUnit actor) {
        return issue("lift", actor, () -> actor.u().lift());
    }

    @Override
    public boolean land(AUnit actor, TilePosition position) {
        return issue("land", actor, () -> actor.u().land(position));
    }

    @Override
    public boolean load(AUnit actor, AUnit target) {
        return issue("load", actor, () -> actor.u().load(target.u()));
    }

    @Override
    public boolean unload(AUnit actor, AUnit target) {
        return issue("unload", actor, () -> actor.u().unload(target.u()));
    }

    @Override
    public boolean unloadAll(AUnit actor) {
        return issue("unloadAll", actor, () -> actor.u().unloadAll());
    }

    @Override
    public boolean unloadAllAt(AUnit actor, Position position) {
        return issue("unloadAllAt", actor, () -> actor.u().unloadAll(position));
    }

    @Override
    public boolean rightClick(AUnit actor, AUnit target) {
        return issue("rightClick", actor, () -> actor.u().rightClick(target.u()));
    }

    @Override
    public boolean haltConstruction(AUnit actor) {
        return issue("haltConstruction", actor, () -> actor.u().haltConstruction());
    }

    @Override
    public boolean cancelConstruction(AUnit actor) {
        return issue("cancelConstruction", actor, () -> actor.u().cancelConstruction());
    }

    @Override
    public boolean cancelAddon(AUnit actor) {
        return issue("cancelAddon", actor, () -> actor.u().cancelAddon());
    }

    @Override
    public boolean cancelTrain(AUnit actor) {
        return issue("cancelTrain", actor, () -> actor.u().cancelTrain());
    }

    @Override
    public boolean cancelTrainSlot(AUnit actor, int slot) {
        return issue("cancelTrainSlot", actor, () -> actor.u().cancelTrain(slot));
    }

    @Override
    public boolean cancelMorph(AUnit actor) {
        return issue("cancelMorph", actor, () -> actor.u().cancelMorph());
    }

    @Override
    public boolean cancelResearch(AUnit actor) {
        return issue("cancelResearch", actor, () -> actor.u().cancelResearch());
    }

    @Override
    public boolean cancelUpgrade(AUnit actor) {
        return issue("cancelUpgrade", actor, () -> actor.u().cancelUpgrade());
    }

    @Override
    public boolean useTech(AUnit actor, TechType tech) {
        return issue("useTech", actor, () -> actor.u().useTech(tech));
    }

    @Override
    public boolean useTechAt(AUnit actor, TechType tech, Position position) {
        return issue("useTechAt", actor, () -> actor.u().useTech(tech, position));
    }

    @Override
    public boolean useTechOn(AUnit actor, TechType tech, AUnit target) {
        return issue("useTechOn", actor, () -> actor.u().useTech(tech, target.u()));
    }

    private interface EngineCall {
        boolean call();
    }

    private boolean issue(String operation, AUnit actor, EngineCall call) {
        try {
            return call.call();
        } catch (Exception e) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(
                "OrderSink." + operation + " failed for " + actor + ": " + e.getMessage()
            );
            return false;
        }
    }
}
