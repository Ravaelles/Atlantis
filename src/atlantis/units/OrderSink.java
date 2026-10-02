package atlantis.units;

import bwapi.Position;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwapi.UpgradeType;

/**
 * Stage D (see _AI/REVIEW.md §16): the single door through which all engine
 * orders pass.
 *
 * <p>Every {@code train/move/attack/...} call in production code must go
 * through this port. {@link BwapiOrderSink} implements it against the live
 * engine; tests plug in a recording fake via
 * {@link AUnit#setOrderSink(OrderSink)}. The interface deliberately mirrors
 * the engine's order vocabulary (one door, many operations); richer
 * intention objects are Stage E material.</p>
 *
 * <p>Implementations must never throw out of these methods and must never
 * terminate the JVM: a failed issuance is reported as {@code false}.</p>
 */
public interface OrderSink {

    boolean attackUnit(AUnit actor, AUnit target);

    boolean train(AUnit actor, UnitType type);

    boolean morph(AUnit actor, UnitType type);

    boolean build(AUnit actor, UnitType type, TilePosition position);

    boolean buildAddon(AUnit actor, UnitType type);

    boolean upgrade(AUnit actor, UpgradeType type);

    boolean research(AUnit actor, TechType type);

    boolean move(AUnit actor, Position position);

    boolean patrol(AUnit actor, Position position);

    boolean holdPosition(AUnit actor);

    boolean stop(AUnit actor);

    boolean follow(AUnit actor, AUnit target);

    boolean gather(AUnit actor, AUnit target);

    boolean repair(AUnit actor, AUnit target);

    boolean burrow(AUnit actor);

    boolean unburrow(AUnit actor);

    boolean cloak(AUnit actor);

    boolean decloak(AUnit actor);

    boolean siege(AUnit actor);

    boolean unsiege(AUnit actor);

    boolean lift(AUnit actor);

    boolean land(AUnit actor, TilePosition position);

    boolean load(AUnit actor, AUnit target);

    boolean unload(AUnit actor, AUnit target);

    boolean unloadAll(AUnit actor);

    boolean unloadAllAt(AUnit actor, Position position);

    boolean rightClick(AUnit actor, AUnit target);

    boolean haltConstruction(AUnit actor);

    boolean cancelConstruction(AUnit actor);

    boolean cancelAddon(AUnit actor);

    boolean cancelTrain(AUnit actor);

    boolean cancelTrainSlot(AUnit actor, int slot);

    boolean cancelMorph(AUnit actor);

    boolean cancelResearch(AUnit actor);

    boolean cancelUpgrade(AUnit actor);

    boolean useTech(AUnit actor, TechType tech);

    boolean useTechAt(AUnit actor, TechType tech, Position position);

    boolean useTechOn(AUnit actor, TechType tech, AUnit target);
}
