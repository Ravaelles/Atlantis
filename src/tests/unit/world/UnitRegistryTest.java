package tests.unit.world;

import atlantis.core.world.UnitRegistry;
import atlantis.core.world.Worlds;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Stage E: unit identity lives in an explicit registry, not in statics.
 */
public class UnitRegistryTest {

    @Test
    void registryKeepsIdentityById() {
        UnitRegistry registry = new UnitRegistry();
        FakeUnit marine = new FakeUnit(AUnitType.Terran_Marine, 20, 20);

        registry.register(marine);

        assertSame(marine, registry.entity(marine.id()));
        assertEquals(1, registry.size());
    }

    @Test
    void forgetEntirelyRemovesTheUnit() {
        UnitRegistry registry = new UnitRegistry();
        FakeUnit marine = new FakeUnit(AUnitType.Terran_Marine, 20, 20);
        registry.register(marine);

        registry.forgetEntirely(marine);

        assertNull(registry.entity(marine.id()));
        assertEquals(0, registry.size());
    }

    @Test
    void registriesAreIndependent() {
        FakeUnit marine = new FakeUnit(AUnitType.Terran_Marine, 20, 20);

        UnitRegistry first = new UnitRegistry();
        UnitRegistry second = new UnitRegistry();
        first.register(marine);

        assertSame(marine, first.entity(marine.id()));
        assertNull(second.entity(marine.id()));
    }

    @Test
    void sharedHolderResetsCleanly() {
        Worlds.units().register(new FakeUnit(AUnitType.Terran_Marine, 20, 20));

        Worlds.reset();

        assertEquals(0, Worlds.units().size());
    }
}
