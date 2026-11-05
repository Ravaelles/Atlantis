package tests.unit;

import atlantis.map.base.BaseLocations;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;

/**
 * A transient "no natural" answer must never poison the cache (measured
 * live: "Natural base can not be determined" every minute on TauCross -
 * the map-analysis call ran before the bases were listed, the null stuck
 * forever, and every natural-anchored decision degraded silently).
 * First call with no bases answers null; once the bases exist the same
 * call answers the natural. No error is asserted here - silence is the
 * contract for the transient case.
 */
@Tag("placement")
public class NaturalBaseResolvesTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void nullAnswerIsNotCachedForever() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 7, 44);
        world(1, fakeOurs(main), fakeEnemies(), () -> {
            DefineNaturalBase.resetForNewGame();

            // No bases listed yet (map analysis before BWEM): null, and
            // it must not stick.
            AbstractTestWithUnits.allBaseLocations.when(
                atlantis.map.base.AllBaseLocations::get)
                .thenReturn(Collections.emptyList());
            assertNull(DefineNaturalBase.naturalIfMainIsAt(main),
                "no bases listed, no natural");

            // Bases arrive: the same query heals instead of replaying
            // the cached miss.
            AbstractTestWithUnits.allBaseLocations.when(
                atlantis.map.base.AllBaseLocations::get)
                .thenReturn(tests.fakes.FakeBaseLocations.get());
            assertNotNull(DefineNaturalBase.naturalIfMainIsAt(main),
                "the natural resolves once bases are listed: "
                    + tests.fakes.FakeBaseLocations.get().size() + " known");
        });
    }

    @Test
    public void stubWorldHasANatural() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 7, 44);
        world(1, fakeOurs(main), fakeEnemies(), () -> {
            assertNotNull(BaseLocations.natural(),
                "the stub world must name a natural - base siters anchor on it");
        });
    }
}
