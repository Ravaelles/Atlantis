package atlantis.config.launcher.openbw;

import atlantis.util.log.ErrorLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Starts a headless OpenBW game and waits until the client can attach.
 *
 * <p>
 * The whole lifecycle lives here - <b>one command owns the host and the
 * client</b>
 * (CONVENTIONS 15). The host must outlive this JVM's start-up, so it is
 * detached
 * ({@code setsid nohup}) and the run is bounded by the configured timeout
 * (CONVENTIONS 13). Stale transport state is cleared <b>before</b> hosting,
 * never
 * after: a dead host leaves its PID in the game registry and the next client
 * adopts it, which looks like "the client never attaches".
 * </p>
 *
 * <p>
 * Every OS interaction is behind {@link ProcessRunner}, so the sequencing
 * (clear -&gt; host -&gt; wait -&gt; attach -&gt; tear down) is unit-testable
 * with a fake
 * and no game. That is the point of this class: the recipe that used to live
 * only
 * in a shell script is now typed, asserted, and callable from {@code Main} or a
 * test.
 * </p>
 */
public final class OpenBWHost {

    /**
     * A started process, reduced to what this class needs. Deliberately not
     * {@code java.lang.ProcessHandle}: that type is Java 9+, and this project
     * targets Java 8 (CONVENTIONS 16) - the release-8 jar build fails on it,
     * which is exactly the trap that compile exists to catch.
     */
    public interface HostProcess {
        long pid();

        boolean isAlive();
    }

    /**
     * The single door to the operating system; a test installs a recording fake.
     */
    public interface ProcessRunner {
        /** Runs a detached command, returning a handle to it. */
        HostProcess startDetached(List<String> command, File workingDir, File outputLog) throws IOException;

        /** Runs a command to completion and returns its exit code. */
        int run(List<String> command);

        String readFileIfExists(String path);
    }

    /** Test seam: swap the OS adapter. */
    private static ProcessRunner runner = new SystemProcessRunner();

    /**
     * Test seam: when false, {@link #host()} refuses to start a game. Without
     * this, a unit test drove a real 30-second host before the fake could be
     * installed - a test must never launch a game (CONVENTIONS 12).
     */
    private static boolean hostingEnabled = true;

    public static void useProcessRunner(ProcessRunner custom) {
        runner = custom != null ? custom : new SystemProcessRunner();
    }

    public static void useHostingEnabled(boolean enabled) {
        hostingEnabled = enabled;
    }

    private final OpenBWConfig config;

    public OpenBWHost(OpenBWConfig config) {
        this.config = config != null ? config : OpenBWConfig.defaults();
    }

    /**
     * Clears stale state, hosts the game in the background and waits until the
     * registry names a live host. Returns the hosted PID, or -1 when the host
     * never came up (the caller then fails the run rather than attaching to
     * nothing).
     */
    public int host() {
        if (!hostingEnabled) {
            ErrorLog.printPlusToFile("OpenBW: hosting disabled (test seam); not starting a game");
            return -1;
        }

        String missing = config.missingRequirement();
        if (missing != null) {
            ErrorLog.printPlusToFile("OpenBW cannot start: " + missing);
            return -1;
        }

        teardown();
        File logDir = new File(config.logDir());
        if (!logDir.isDirectory() && !logDir.mkdirs()) {
            ErrorLog.printPlusToFile("OpenBW: cannot create log dir " + logDir.getAbsolutePath());
            return -1;
        }

        File serverLog = new File(logDir, "server.log");
        HostProcess handle = startHostDetached(serverLog);
        if (handle == null)
            return -1;

        int pid = waitForHost(handle);
        if (pid < 0) {
            ErrorLog.printPlusToFile("OpenBW: host published no game registry entry within "
                    + config.hostWaitSeconds() + " s; server log tail:\n" + tail(serverLog));
        }
        return pid;
    }

    private HostProcess startHostDetached(File serverLog) {
        List<String> command = new ArrayList<>();
        command.add("setsid");
        command.add("nohup");
        command.add("env");
        // THE setting that makes the client attach (measured 2026-10-08, see
        // _AI/CHALLENGES/OpenBW.md): the harness's BWAPI creates its shared-memory
        // game registry - the table the Java client reads the server PID from -
        // only when `Server::serverEnabled` is true, i.e. when
        // LoadConfigStringUCase("config", "shared_memory", "ON") == "ON". That
        // resolves from BWAPI_CONFIG_<SECTION>__<KEY> before any bwapi.ini is
        // read, and StardustDevEnvironment has no bwapi.ini, so without this the
        // host serves a socket but publishes no registry and the client loops on
        // "No server proc ID". scripts/run-openbw-e2e.sh sets the same variable.
        command.add("BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON");
        command.add("timeout");
        command.add(String.valueOf(config.gameTimeoutSeconds()));
        command.add("bash");
        command.add(config.serverScript());
        command.add(config.map());
        command.add(config.ourRace());
        command.add(config.enemyRace());

        try {
            return runner.startDetached(command, new File(config.gameDir()), serverLog);
        } catch (IOException e) {
            ErrorLog.printPlusToFile("OpenBW: could not start the host: " + e);
            return null;
        }
    }

    /**
     * Waits for the game registry to name a live host. It is the first thing the
     * client reads and the host publishes it up front, so this is the right
     * signal - waiting for the <em>socket</em> would deadlock, because the host
     * creates it only once a client knocks.
     */
    private int waitForHost(HostProcess handle) {
        String registry = "/dev/shm/bwapi_shared_memory_game_list";
        long deadline = System.currentTimeMillis() + config.hostWaitSeconds() * 1000L;

        while (System.currentTimeMillis() < deadline) {
            int pid = readHostPid(registry);
            if (pid > 0 && isAlive(pid))
                return pid;

            if (handle != null && !handle.isAlive() && !anotherHostIsRunning())
                return -1;
            sleepQuietly(500);
        }
        return -1;
    }

    /** The registry slot layout: int pid at +0, byte isConnected at +4. */
    public static int readHostPid(String registryPath) {
        byte[] bytes = readBytes(registryPath, 24);
        if (bytes == null || bytes.length < 8)
            return -1;

        int pid = (bytes[0] & 0xff) | ((bytes[1] & 0xff) << 8)
                | ((bytes[2] & 0xff) << 16) | ((bytes[3] & 0xff) << 24);
        return pid;
    }

    private static byte[] readBytes(String path, int count) {
        try {
            byte[] all = Files.readAllBytes(Paths.get(path));
            if (all.length == 0)
                return null;
            int length = Math.min(count, all.length);
            byte[] slice = new byte[length];
            System.arraycopy(all, 0, slice, 0, length);
            return slice;
        } catch (IOException e) {
            return null;
        }
    }

    private boolean isAlive(int pid) {
        return runner.run(java.util.Arrays.asList("kill", "-0", String.valueOf(pid))) == 0;
    }

    private boolean anotherHostIsRunning() {
        return runner.run(java.util.Arrays.asList("pgrep", "-x", "BWAPILauncher")) == 0;
    }

    /**
     * Removes everything a previous run could leave behind. Order matters:
     * killing the host first, then clearing the transports it owned, and never
     * the other way round.
     */
    public void teardown() {
        runner.run(java.util.Arrays.asList("pkill", "-9", "-x", "BWAPILauncher"));
        for (String file : listStaleTransportFiles()) {
            new File(file).delete();
        }
    }

    /** The two namespaces, both named after the hosting PID. */
    public static List<String> listStaleTransportFiles() {
        List<String> files = new ArrayList<>();
        addMatching(files, new File("/dev/shm"), "bwapi_shared_memory_");
        addMatching(files, new File("/tmp"), "bwapi_socket_");
        return files;
    }

    private static void addMatching(List<String> files, File dir, String prefix) {
        String[] names = dir.list();
        if (names == null)
            return;
        for (String name : names) {
            if (name.startsWith(prefix))
                files.add(new File(dir, name).getAbsolutePath());
        }
    }

    private String tail(File log) {
        try {
            List<String> lines = Files.readAllLines(log.toPath());
            int from = Math.max(0, lines.size() - 15);
            StringBuilder builder = new StringBuilder();
            for (int i = from; i < lines.size(); i++)
                builder.append(lines.get(i)).append('\n');
            return builder.toString();
        } catch (IOException e) {
            return "(no server log)";
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** The real adapter: everything the class needs from the OS. */
    static final class SystemProcessRunner implements ProcessRunner {

        @Override
        public HostProcess startDetached(List<String> command, File workingDir, File outputLog) throws IOException {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(workingDir);
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.to(outputLog));
            final Process process = builder.start();

            return new HostProcess() {
                @Override
                public long pid() {
                    return processPid(process);
                }

                @Override
                public boolean isAlive() {
                    return process.isAlive();
                }
            };
        }

        /**
         * The OS pid of a started process, or -1. Reflection because
         * {@code Process.pid()} is Java 9+ and this project targets Java 8; on 8
         * the field is private, but liveness is what matters and {@code isAlive()}
         * covers it.
         */
        private static long processPid(Process process) {
            try {
                return (long) (Long) Process.class.getMethod("pid").invoke(process);
            } catch (Exception e) {
                return -1;
            }
        }

        @Override
        public int run(List<String> command) {
            try {
                Process process = new ProcessBuilder(command).start();
                return process.waitFor();
            } catch (IOException e) {
                return -1;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            }
        }

        @Override
        public String readFileIfExists(String path) {
            try {
                return new String(Files.readAllBytes(Paths.get(path)));
            } catch (IOException e) {
                return null;
            }
        }
    }
}
