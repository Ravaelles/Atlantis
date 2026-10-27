package tests.unit;

import atlantis.Atlantis;
import atlantis.config.AtlantisConfigChanger;
import atlantis.game.AGame;
import atlantis.game.player.Enemy;
import atlantis.game.race.EnemyRace;
import atlantis.map.base.AllBaseLocations;
import atlantis.map.choke.AllChokes;
import atlantis.util.cache.Cache;
import bwapi.Game;
import bwapi.Race;
import bwapi.WalkPosition;
import main.Main;
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

        mockOurRace();
        mockAtlantisConfig();
        mockGameObject();
        mockAGameObject();
        mockBaseLocations();
        mockChokes();
        mockOtherStaticClasses();
    }

    /**
     * Answers the race question the bot actually asks - {@code Main.ourRace()} -
     * with whatever {@code initRace()} declared, so {@code We.protoss()/terran()/zerg()}
     * agree with the config below instead of both disagreeing with the test.
     *
     * <p>The source is mocked, not {@code We}: {@code RaceWiringTest} is a standalone
     * infra class that reads the real {@code Main} to pin the owner's ruling, and it
     * must keep seeing the truth. {@code We.*()} is not cached for the binary
     * questions, so one answer per call is enough; the cached {@code We.race()} is
     * dropped by {@code Cache.nukeAllCaches()} above and rebuilt from these.</p>
     */
    private void mockOurRace() {
        if (AbstractTestWithUnits.main == null) AbstractTestWithUnits.main = Mockito.mockStatic(Main.class);
        AbstractTestWithUnits.main.when(Main::ourRace).thenAnswer(invocation -> test.initRace().name());
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
        if (AbstractTestWithUnits.aGame == null) AbstractTestWithUnits.aGame = Mockito.mockStatic(AGame.class);

        // The frame-clock questions (AGame::now, everyNthGameFrame,
        // notNthGameFrame) are stubbed once, here - not re-stubbed by
        // useFakeTime every frame. Every when(...) is a Mockito registration,
        // and a static mock scans its whole stubbing list on every intercepted
        // call, so per-frame re-stubbing made every A.now() call cost grow with
        // the frame number - the quadratic cost behind BUGS.md B-24. The
        // answers read the published clock at call time, so one registration
        // per test is enough.
        AbstractTestWithUnits.stubAGameClock(AbstractTestWithUnits.aGame);

        test.currentSupplyUsed = test.options == null ? 0 : test.options.getIntOr("supplyUsed", 0);
        test.currentSupplyTotal = test.options == null ? 4 : test.options.getIntOr("supplyTotal", 4);

        AbstractTestWithUnits.aGame.when(AGame::supplyUsed).thenAnswer(invocation -> test.currentSupplyUsed());
        AbstractTestWithUnits.aGame.when(AGame::supplyTotal).thenAnswer(invocation -> test.currentSupplyTotal());
        AbstractTestWithUnits.aGame.when(AGame::supplyFree).thenAnswer(invocation -> test.currentSupplyFree());

        AbstractTestWithUnits.aGame.when(AGame::minerals).thenAnswer(invocation -> test.currentMinerals());
        AbstractTestWithUnits.aGame.when(AGame::gas).thenAnswer(invocation -> test.currentGas());

        // Answered on every call, not stubbed once in setUp(): the enemy race is
        // a property of the world, like the supply, and a test declares it where
        // it declares the units - inside the test method, before world(...). A
        // test that fills the enemy side with drones, lurkers and creep colonies
        // and does not say so gets every Enemy.zerg() branch answering for a
        // Protoss, which is how ATargetingTest's Zerg scenarios ended up
        // unreachable.
        if (AbstractTestWithUnits.enemyRace == null) AbstractTestWithUnits.enemyRace = Mockito.mockStatic(EnemyRace.class);
        AbstractTestWithUnits.enemyRace.when(EnemyRace::isEnemyProtoss).thenAnswer(invocation -> test.currentEnemyRace().equals(Race.Protoss));
        AbstractTestWithUnits.enemyRace.when(EnemyRace::isEnemyTerran).thenAnswer(invocation -> test.currentEnemyRace().equals(Race.Terran));
        AbstractTestWithUnits.enemyRace.when(EnemyRace::isEnemyZerg).thenAnswer(invocation -> test.currentEnemyRace().equals(Race.Zerg));
    }

    /**
     * You have to define static mocks as public field of this class, so they can be automatically reset on test end.
     */
    private void mockOtherStaticClasses() {
//        if (test.env == null) test.env = Mockito.mockStatic(Env.class);
//        test.env.when(Env::isTesting).thenReturn(true);

        // ATech used to be mocked here, the whole class at once, with three
        // stubbed methods. Every other method then answered null or 0 - including
        // getCurrentlyResearching() and costOf(...) - so the tests were running
        // against a facade that does nothing. The two questions the stub world
        // genuinely cannot answer are behind ATech.Source now
        // (tests.fakes.FakeResearch), and the rest of the class runs for real.

        // Same race as EnemyRace above - the two mocks used to disagree, because
        // this one was hard-coded to "enemy is Protoss" while a test could change
        // the other one. Enemy.* is what production code actually calls.
        if (AbstractTestWithUnits.enemy == null) AbstractTestWithUnits.enemy = Mockito.mockStatic(Enemy.class);
        AbstractTestWithUnits.enemy.when(() -> Enemy.terran()).thenAnswer(invocation -> test.currentEnemyRace().equals(Race.Terran));
        AbstractTestWithUnits.enemy.when(() -> Enemy.protoss()).thenAnswer(invocation -> test.currentEnemyRace().equals(Race.Protoss));
        AbstractTestWithUnits.enemy.when(() -> Enemy.zerg()).thenAnswer(invocation -> test.currentEnemyRace().equals(Race.Zerg));
    }

    private void mockBaseLocations() {
        if (AbstractTestWithUnits.allBaseLocations == null) AbstractTestWithUnits.allBaseLocations = Mockito.mockStatic(AllBaseLocations.class);
        AbstractTestWithUnits.allBaseLocations.when(AllBaseLocations::get).thenReturn(FakeBaseLocations.get());
    }

    private void mockChokes() {
        if (AbstractTestWithUnits.allChokes == null) AbstractTestWithUnits.allChokes = Mockito.mockStatic(AllChokes.class);
        AbstractTestWithUnits.allChokes.when(AllChokes::get).thenReturn(FakeChokes.get());
    }
}
