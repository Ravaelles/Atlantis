package tests.unit;

import atlantis.Atlantis;
import atlantis.config.AtlantisConfigChanger;
import atlantis.config.env.Env;
import atlantis.game.AGame;
import atlantis.game.race.EnemyRace;
import atlantis.information.tech.ATech;
import atlantis.map.base.AllBaseLocations;
import atlantis.map.choke.AllChokes;
import atlantis.game.player.Enemy;
import atlantis.util.cache.Cache;
import bwapi.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.fakes.FakeBaseLocations;
import tests.fakes.FakeChokes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MockEverything {
    private final AbstractTestWithUnits test;

    public MockEverything(AbstractTestWithUnits test) {
        this.test = test;
    }

    /**
     * Terran, because that is what {@code Main.ourRace()} returns and what almost
     * every test builds. Before this, the whole suite pretended to be Protoss:
     * {@code AtlantisRaceConfig} said BASE = Protoss_Nexus, WORKER = Protoss_Probe,
     * BARRACKS = Protoss_Gateway, DEFENSIVE_BUILDING_* = Photoss_Cannon while the
     * tests were creating Terran_Marine and Terran_Barracks, so every branch
     * guarded by that config (and every {@code We.terran()}) was evaluated with
     * the wrong race. A test that needs another race overrides {@code initRace()}.
     */
    public static Race defaultEnemyRaceForTests() {
        return Race.Protoss;
    }

    public static Race defaultRaceForTests() {
        return Race.Terran;
    }

    public void mockEverything() {
        Cache.nukeAllCaches();

        mockAtlantisConfig();
        mockGameObject();
        mockAGameObject();
        mockBaseLocations();
        mockChokes();
        mockOtherStaticClasses();
    }

    private void mockAtlantisConfig() {
        // Has to follow the race the test asked for, not a hard-coded one: the
        // config is what defines BASE / WORKER / BARRACKS / DEFENSIVE_BUILDING_*,
        // so a Protoss config silently reinterprets every Terran unit a test
        // creates.
        switch (test.initRace()) {
            case Protoss:
                AtlantisConfigChanger.useConfigForProtoss();
                break;
            case Zerg:
                AtlantisConfigChanger.useConfigForZerg();
                break;
            case Terran:
            default:
                AtlantisConfigChanger.useConfigForTerran();
                break;
        }
    }

    private void mockGameObject() {
        Game game = test.game;

        if ((game = Atlantis.game()) == null) {
            game = Mockito.mock(Game.class);
            Atlantis.getInstance().setGame(game);
        }

        // Map dimensions
        when(game.mapWidth()).thenReturn(202);
        when(game.mapHeight()).thenReturn(203);

        // Walkability
        when(game.isWalkable(any(WalkPosition.class))).thenReturn(true);
    }

    private void mockAGameObject() {
        if (test.aGame == null) test.aGame = Mockito.mockStatic(AGame.class);

        test.currentSupplyUsed = test.options == null ? 0 : test.options.getIntOr("supplyUsed", 0);
        test.currentSupplyTotal = test.options == null ? 4 : test.options.getIntOr("supplyTotal", 4);

        test.aGame.when(AGame::supplyUsed).thenAnswer(invocation -> test.currentSupplyUsed());
        test.aGame.when(AGame::supplyTotal).thenAnswer(invocation -> test.currentSupplyTotal());
        test.aGame.when(AGame::supplyFree).thenAnswer(invocation -> test.currentSupplyFree());

        test.aGame.when(AGame::minerals).thenAnswer(invocation -> test.currentMinerals());
        test.aGame.when(AGame::gas).thenAnswer(invocation -> test.currentGas());

        if (test.enemyRace == null) test.enemyRace = Mockito.mockStatic(EnemyRace.class);
        Race enemy = test.initEnemyRace();
        test.enemyRace.when(EnemyRace::isEnemyProtoss).thenReturn(enemy.equals(Race.Protoss));
        test.enemyRace.when(EnemyRace::isEnemyTerran).thenReturn(enemy.equals(Race.Terran));
        test.enemyRace.when(EnemyRace::isEnemyZerg).thenReturn(enemy.equals(Race.Zerg));
    }

    /**
     * You have to define static mocks as public field of this class, so they can be automatically reset on test end.
     */
    private void mockOtherStaticClasses() {
//        if (test.env == null) test.env = Mockito.mockStatic(Env.class);
//        test.env.when(Env::isTesting).thenReturn(true);

        if (test.aTech == null) test.aTech = Mockito.mockStatic(ATech.class);
        test.aTech.when(() -> ATech.isResearched(TechType.Lockdown)).thenReturn(true);
        test.aTech.when(() -> ATech.isResearched(null)).thenReturn(false);
        test.aTech.when(() -> ATech.getUpgradeLevel(any())).thenReturn(0);

        // Same race as EnemyRace above - the two mocks used to disagree, because
        // this one was hard-coded to "enemy is Protoss" while a test could change
        // the other one. Enemy.* is what production code actually calls.
        Race enemyRace = test.initEnemyRace();
        if (test.enemy == null) test.enemy = Mockito.mockStatic(Enemy.class);
        test.enemy.when(() -> Enemy.terran()).thenReturn(enemyRace.equals(Race.Terran));
        test.enemy.when(() -> Enemy.protoss()).thenReturn(enemyRace.equals(Race.Protoss));
        test.enemy.when(() -> Enemy.zerg()).thenReturn(enemyRace.equals(Race.Zerg));
    }

    private void mockBaseLocations() {
        if (test.allBaseLocations == null) test.allBaseLocations = Mockito.mockStatic(AllBaseLocations.class);
        test.allBaseLocations.when(AllBaseLocations::get).thenReturn(FakeBaseLocations.get());
    }

    private void mockChokes() {
        if (test.allChokes == null) test.allChokes = Mockito.mockStatic(AllChokes.class);
        test.allChokes.when(AllChokes::get).thenReturn(FakeChokes.get());
    }
}
