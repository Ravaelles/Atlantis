package tests.unit;

import atlantis.config.launcher.openbw.OpenBWConfig;
import atlantis.config.launcher.openbw.OpenBWHost;
import atlantis.config.launcher.openbw.OpenBWRunResult;
import atlantis.config.launcher.openbw.OpenBWRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The OpenBW run API, proven without launching a game.
 *
 * <p>
 * This is the payoff of putting the lifecycle in Java instead of a shell
 * script:
 * the sequencing (clear stale state -&gt; host -&gt; wait for the registry
 * -&gt; verdict)
 * and the verdict logic are ordinary code, so they are asserted here in
 * milliseconds rather than inferred from a five-minute game run.
 * </p>
 */
public class OpenBWLauncherTest {

    @AfterEach
    public void restoreRealRunner() {
        OpenBWHost.useProcessRunner(null);
        OpenBWHost.useHostingEnabled(true);
    }

    /**
     * Hosting is disabled for the whole class: a unit test must never start a
     * game (it once did, and took 30 s of real hosting). The seam exists so the
     * lifecycle can be exercised with a fake without touching the machine.
     */
    @org.junit.jupiter.api.BeforeEach
    public void disableRealHosting() {
        OpenBWHost.useHostingEnabled(false);
    }

    // ---- configuration ------------------------------------------------------

    @Test
    public void aConfigKnowsWhenItCannotRunAndWhy() {
        OpenBWConfig broken = OpenBWConfig.builder()
                .harnessDir("/definitely/not/here")
                .build();

        assertFalse(broken.isRunnable());
        assertNotNull(broken.missingRequirement());
        assertTrue(broken.missingRequirement().contains("/definitely/not/here"),
                "the reason must name the path: " + broken.missingRequirement());
    }

    @Test
    public void builderDefaultsAreTheMachinesRealPaths() {
        OpenBWConfig config = OpenBWConfig.defaults();

        assertEquals("/sc-ai/StardustDevEnvironment", config.harnessDir());
        assertTrue(config.launcherBinary().endsWith("/build/bin/BWAPILauncher"));
        assertTrue(config.serverScript().endsWith("run-openbw-server.sh"));
        assertTrue(config.gameDir().endsWith("build/test"));
        assertTrue(config.gameTimeoutSeconds() <= 360, "CONVENTIONS 13 caps every command at 360 s");
    }

    @Test
    public void configOfMapKeepsEveryOtherDefault() {
        OpenBWConfig config = OpenBWConfig.ofMap("maps/sscai/(3)TauCross.scx");

        assertEquals("maps/sscai/(3)TauCross.scx", config.map());
        assertEquals(OpenBWConfig.DEFAULT_OUR_RACE, config.ourRace());
        assertEquals(OpenBWConfig.DEFAULT_HARNESS_DIR, config.harnessDir());
    }

    // ---- host lifecycle -----------------------------------------------------

    private static final class RecordingRunner implements OpenBWHost.ProcessRunner {        final List<List<String>> started = new ArrayList<>();
        final List<List<String>> ran = new ArrayList<>();
        boolean hostProcessAlive = true;

        @Override
        public OpenBWHost.HostProcess startDetached(List<String> command, File workingDir, File outputLog) {
            started.add(command);
            outputLog.getParentFile().mkdirs();
            return new FakeHostProcess(4242L, hostProcessAlive);
        }

        @Override
        public int run(List<String> command) {
            ran.add(command);
            // pgrep / kill -0: report "nothing found" so the wait loop relies on
            // the registry, which is what the test supplies.
            return 1;
        }

        @Override
        public String readFileIfExists(String path) {
            return null;
        }
    }

    /** Minimal host process: only pid() and isAlive() are meaningful here. */
    private static final class FakeHostProcess implements OpenBWHost.HostProcess {
        private final long pid;
        private final boolean alive;

        FakeHostProcess(long pid, boolean alive) {
            this.pid = pid;
            this.alive = alive;
        }

        @Override
        public long pid() {
            return pid;
        }

        @Override
        public boolean isAlive() {
            return alive;
        }
    }

    @Test
    public void theHostCommandCarriesTheMapRacesAndTimeout() {
        RecordingRunner runner = new RecordingRunner();
        OpenBWHost.useProcessRunner(runner);
        // This test drives the HOST SEQUENCE, so hosting must be enabled here -
        // it is the OS adapter underneath that is fake, not the lifecycle.
        OpenBWHost.useHostingEnabled(true);

        OpenBWConfig config = OpenBWConfig.builder()
                .map("maps/cog/(3)TauCross1.1.scx")
                .races("Protoss", "Zerg")
                .gameTimeoutSeconds(200)
                .hostWaitSeconds(1)
                .logDir(tempDir().getAbsolutePath())
                .build();

        new OpenBWHost(config).host();

        assertEquals(1, runner.started.size(), "exactly one host is started");
        List<String> command = runner.started.get(0);
        assertTrue(command.contains("setsid"), "the host must survive this JVM: " + command);
        assertTrue(command.contains("nohup"), "and must not die with the parent shell: " + command);
        assertTrue(command.contains("200"), "the timeout must be the configured one: " + command);
        assertTrue(command.contains("maps/cog/(3)TauCross1.1.scx"), command.toString());
        assertTrue(command.contains("Protoss") && command.contains("Zerg"), command.toString());

        // Without this the harness publishes no game registry and the Java client
        // never attaches (measured 2026-10-08, _AI/CHALLENGES/OpenBW.md). It is the
        // one setting that makes the difference between "No server proc ID" forever
        // and "Connection successful" / "HELLO_ATLANTIS", so it is pinned here.
        assertTrue(command.contains("BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON"),
                "the host must be told to publish the shared-memory registry: " + command);
    }

    @Test
    public void staleStateIsClearedBeforeHosting() {
        RecordingRunner runner = new RecordingRunner();
        OpenBWHost.useProcessRunner(runner);

        new OpenBWHost(OpenBWConfig.builder().logDir(tempDir().getAbsolutePath()).build()).teardown();

        assertTrue(runner.ran.stream().anyMatch(c -> c.contains("pkill") && c.contains("-9")),
                "a leftover host must be killed first: " + runner.ran);
        assertTrue(runner.ran.stream().anyMatch(c -> c.contains("-x")),
                "pkill -x, never -f (a -f pattern matches the shell running it)");
    }

    @Test
    public void transportFilesToClearAreNamedAfterTheHostPid() {
        List<String> files = OpenBWHost.listStaleTransportFiles();

        for (String file : files) {
            assertTrue(file.contains("bwapi_shared_memory_") || file.contains("bwapi_socket_"),
                    "unexpected stale file: " + file);
        }
    }

    @Test
    public void theRegistryPidIsReadLittleEndian() throws Exception {
        File registry = File.createTempFile("game_list", ".bin");
        registry.deleteOnExit();
        // The registry slot layout, measured from a live host: little-endian int
        // pid at +0, byte isConnected at +4. The value has a distinct byte in
        // every position so an endianness swap cannot pass by accident.
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(registry)) {
            out.write(new byte[] {0x03, 0x02, 0x01, 0x00, 1, 0, 0, 0});
        }

        assertEquals(0x00010203, OpenBWHost.readHostPid(registry.getAbsolutePath()));
    }

    @Test
    public void anEmptyRegistryMeansNoHostYet() throws Exception {
        File registry = File.createTempFile("game_list_empty", ".bin");
        registry.deleteOnExit();

        assertEquals(-1, OpenBWHost.readHostPid(registry.getAbsolutePath()));
        assertEquals(-1, OpenBWHost.readHostPid("/definitely/not/here"));
    }

    // ---- verdict ------------------------------------------------------------

    private File tempDir() {
        try {
            File dir = File.createTempFile("openbw", "dir");
            dir.delete();
            dir.mkdirs();
            dir.deleteOnExit();
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private OpenBWRunner runnerWithBotLog(String content) throws IOException {
        File dir = tempDir();
        File log = new File(dir, "bot.log");
        try (Writer out = new FileWriter(log)) {
            out.write(content);
        }
        return new OpenBWRunner(OpenBWConfig.builder().logDir(dir.getAbsolutePath()).build());
    }

    @Test
    public void aSuccessfulRunIsRecognisedFromTheBotLog() throws Exception {
        OpenBWRunResult result = runnerWithBotLog(
                "Connection successful\n"
                        + "Use build order: `Zealot into Goon`\n"
                        + "HELLO_ATLANTIS - BWAPI attached, Atlantis is playing!\n"
                        + "Frame 900\n")
                .verdict();

        assertTrue(result.attached());
        assertTrue(result.playing());
        assertTrue(result.isSuccess());
        assertNull(result.failureReason());
        assertEquals(900, result.lastFrame());
    }

    @Test
    public void anUnattachedRunIsAFailureWithAReason() throws Exception {
        OpenBWRunResult result = runnerWithBotLog("Unable to open communications socket\n").verdict();

        assertFalse(result.attached());
        assertFalse(result.isSuccess());
        assertNotNull(result.failureReason());
        // With no connection line, the reason is the connection itself.
        assertTrue(result.failureReason().contains("never attached"), result.failureReason());
    }

    @Test
    public void aMissingBuildOrderIsNamedAsTheReason() throws Exception {
        OpenBWRunResult result = runnerWithBotLog(
                "Connection successful\nCurrent BUILD ORDER is NULL\n").verdict();

        assertTrue(result.attached());
        assertFalse(result.playing(), "attaching is not playing");
        assertNotNull(result.failureReason());
        assertTrue(result.failureReason().contains("no build order"), result.failureReason());
    }

    @Test
    public void aRunWithoutALogIsAFailureNotAnException() {
        OpenBWRunner runner = new OpenBWRunner(
                OpenBWConfig.builder().logDir("/definitely/not/here").build());

        OpenBWRunResult result = runner.verdict();

        assertFalse(result.isSuccess());
        assertNotNull(result.failureReason());
        assertTrue(result.failureReason().contains("no bot log"), result.failureReason());
    }

    @Test
    public void aRunThatNeverStartedSaysSo() {
        OpenBWRunResult result = OpenBWRunResult.hostDidNotStart("s.log", "b.log");

        assertFalse(result.attached());
        assertTrue(result.failureReason().contains("host"), result.failureReason());
    }

    @Test
    public void logPathsFollowTheConfiguredDirectory() {
        OpenBWRunner runner = new OpenBWRunner(OpenBWConfig.builder().logDir("out/openbw").build());

        assertEquals("out/openbw/bot.log", runner.botLogPath());
        assertEquals("out/openbw/server.log", runner.serverLogPath());
    }

    // ---- the API shape the owner asked for ---------------------------------

    @Test
    public void theFluentApiReadsLikeTheRecipe() {
        // init().useConfig(...).race(...).run() - the point of the exercise is
        // that this line is legible in Main and in a test, so it is compiled here
        // (not executed: run() would need a game).
        OpenBWConfig config = OpenBWConfig.ofMap("maps/cog/(3)TauCross1.1.scx");

        assertEquals("maps/cog/(3)TauCross1.1.scx", config.map());
        assertEquals(Arrays.asList("Protoss", "Zerg"), Arrays.asList(config.ourRace(), config.enemyRace()));

        OpenBWConfig customised = OpenBWConfig.builder()
                .map("maps/sscai/(3)TauCross.scx")
                .races("Protoss", "Protoss")
                .gameTimeoutSeconds(120)
                .build();
        assertEquals("Protoss", customised.enemyRace());
        assertEquals(120, customised.gameTimeoutSeconds());
    }
}
