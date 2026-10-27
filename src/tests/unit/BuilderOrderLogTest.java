package tests.unit;

import atlantis.cherryvis.ACherryVis;
import atlantis.config.env.Env;
import atlantis.production.constructions.Construction;
import atlantis.units.AUnitType;
import atlantis.units.select.BaseSelect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #214 (owner ask): the units log names WHAT the builder is building
 * and WHY, so a stuck builder (site-refresh chase, covered tile) reads off
 * one row instead of needing a WINE_DEBUG hunt.
 * {@code #Build@2:15} becomes {@code #Build/Protoss_Pylon/TravelToSite@2:15}.
 *
 * <p>Change-only on all three parts (command, building, tooltip): a steady
 * builder stays silent. No world harness: the watcher reads one mocked
 * selection, nothing else, so this pins the watcher and nothing around it.</p>
 */
@Tag("infra")
public class BuilderOrderLogTest extends AbstractTestWithUnits {

    private MockedStatic<BaseSelect> baseSelect;

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    @BeforeEach
    public void enableTrace() {
        Env.setCherryVisEnabledForTest(true);
        unitLogger().clearForTests();
        atlantis.cherryvis.simple.ACherryVis_OrderWatcher.clear();
        baseSelect = Mockito.mockStatic(BaseSelect.class);
    }

    @AfterEach
    public void cleanTrace() {
        if (baseSelect != null) baseSelect.close();
        unitLogger().clearForTests();
        atlantis.cherryvis.simple.ACherryVis_OrderWatcher.clear();
        Env.setCherryVisEnabledForTest(false);
    }

    private static atlantis.cherryvis.simple.ACherryVisUnitLogger unitLogger() {
        return ((atlantis.cherryvis.simple.ACherryVisLogger) ACherryVis.logger()).getUnitLogger();
    }

    private void showUnits(FakeUnit... units) {
        List<atlantis.units.AUnit> alive = new ArrayList<>(java.util.Arrays.asList(units));
        baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenAnswer(invocation -> alive);
    }

    private static String rows() {
        return atlantis.cherryvis.simple.ACherryVis_UnitLogs.buildUnitLogs(unitLogger());
    }

    @Test
    public void builderRowNamesBuildingAndBehavior() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);
        Construction construction = new Construction(AUnitType.Protoss_Pylon);
        probe.setConstruction(construction);
        probe.setTooltip("TravelToSite", false);
        showUnits(probe);

        atlantis.cherryvis.simple.ACherryVis_OrderWatcher.update();

        String rows = rows();
        assertTrue(rows.contains("/Pylon/TravelToSite@"),
            "the row names the building and the behavior: " + rows);
    }

    @Test
    public void steadyBuilderStaysSilent() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);
        probe.setConstruction(new Construction(AUnitType.Protoss_Pylon));
        probe.setTooltip("TravelToSite", false);
        showUnits(probe);

        atlantis.cherryvis.simple.ACherryVis_OrderWatcher.update();
        int afterFirst = rows().length();

        atlantis.cherryvis.simple.ACherryVis_OrderWatcher.update();

        assertEquals(afterFirst, rows().length(),
            "same command, building and tooltip - no second row");
    }

    @Test
    public void plainOrderKeepsOldShape() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);
        showUnits(probe);

        atlantis.cherryvis.simple.ACherryVis_OrderWatcher.update();

        String rows = rows();
        assertTrue(rows.contains("#"), "the engine order still rows: " + rows);
        assertTrue(!rows.contains("/Pylon") && !rows.contains("/TravelToSite"),
            "no construction, no tooltip - no suffixes: " + rows);
    }
}
