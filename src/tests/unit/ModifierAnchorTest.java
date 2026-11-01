package tests.unit;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.CatalogueSiteAdapter;
import atlantis.production.constructions.position.modifier.ModifierAnchor;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.placement.TargetPlacement;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Migration A4b (owner #146): legacy order modifiers resolve to catalogue
 * neighbourhood anchors instead of degrading to anywhere. Pure lookup, no
 * search: unresolvable modifiers answer null and the caller keeps its
 * legacy path. Stub-world pins name the contract, not map physics.
 */
@Tag("production")
public class ModifierAnchorTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void clean() {
        EventLog.clearForTests();
        atlantis.combat.BattleCommander.clearForTests();
    }

    @AfterEach
    public void cleanAfter() {
        EventLog.clearForTests();
        atlantis.combat.BattleCommander.clearForTests();
    }

    @Test
    public void mainModifierAnchorsAtMain() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            HasPosition anchor = ModifierAnchor.resolve(ModifierAnchor.MAIN);

            assertTrue(anchor != null, "MAIN names our main base");
            assertEquals(Select.main().position(), anchor.position(),
                "the anchor IS the main, not a copy near it");
        });
    }

    @Test
    public void unknownAndBunkerModifiersResolveToNull() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20)
        ), fakeEnemies(), () -> {
            assertNull(ModifierAnchor.resolve("NO_SUCH_MODIFIER"),
                "unknown names keep the legacy path");
            assertNull(ModifierAnchor.resolve(null),
                "no modifier is not a modifier");
        });
    }

    @Test
    public void naturalMirrorsBaseLocations() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20)
        ), fakeEnemies(), () -> {
            assertEquals(atlantis.map.base.BaseLocations.natural(),
                ModifierAnchor.resolve(ModifierAnchor.NATURAL),
                "NATURAL is whatever the base index names - wiring, not geography");
        });
    }

    @Test
    public void modifierOrderConstrainsToNeighbourhood() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            order.setModifier(ModifierAnchor.MAIN);
            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setProductionOrder(order);

            TargetPlacement constraint =
                CatalogueSiteAdapter.constraintFor(order, construction);

            assertEquals(TargetPlacement.Mode.NEIGHBOURHOOD, constraint.mode(),
                "a modifier order constrains to a neighbourhood, never anywhere");
            APosition main = APosition.create(Select.main().position());
            assertEquals(main.tx(), constraint.tileX());
            assertEquals(main.ty(), constraint.tileY());
        });
    }

    @Test
    public void unresolvableModifierFallsThroughToNearTo() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20)
        ), fakeEnemies(), () -> {
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            order.setModifier("NO_SUCH_MODIFIER");
            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setProductionOrder(order);
            construction.setNearTo(APosition.create(30, 30));

            TargetPlacement constraint =
                CatalogueSiteAdapter.constraintFor(order, construction);

            assertEquals(TargetPlacement.Mode.NEIGHBOURHOOD, constraint.mode());
            assertEquals(30, constraint.tileX());
            assertEquals(30, constraint.tileY());
        });
    }

    @Test
    public void explicitNearToBeatsTheGenericModifier() {
        // Legacy precedence (A4g): the spiral read nearTo first and the
        // modifier only when no near was set - an explicitly placed site
        // beats the generic neighbourhood class.
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20)
        ), fakeEnemies(), () -> {
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            order.setModifier(ModifierAnchor.MAIN);
            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setProductionOrder(order);
            construction.setNearTo(APosition.create(30, 30));

            TargetPlacement constraint =
                CatalogueSiteAdapter.constraintFor(order, construction);

            assertEquals(TargetPlacement.Mode.NEIGHBOURHOOD, constraint.mode());
            assertEquals(30, constraint.tileX());
            assertEquals(30, constraint.tileY());
        });
    }
}
