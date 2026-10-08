package tests.architecture;

import atlantis.keyboard.KeyRelay;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every class on the run classpath must be Java 8 bytecode (class-file major
 * version 52).
 *
 * <p>
 * Why this is a test and not a comment: the compiled output directory
 * ({@code out/production/Atlantis}) is shared by the IDE, which launches
 * {@code main.Main} from it, and by the test scripts. The Wine bot JVM is Java
 * 8
 * (CONVENTIONS §16) and the IDE's supervisor JVM is Java 8 too, while the
 * machine's default {@code javac} is not - so any compile that forgets
 * {@code --release 8} writes Java 17 classes into the directory the Java 8
 * runtime loads from.
 * </p>
 *
 * <p>
 * The failure it produced (measured 2026-10-08) was not a compile error but a
 * dead keyboard: JNativeHook's dispatch thread loaded
 * {@code atlantis/keyboard/KeyRelay} and threw
 * </p>
 *
 * <pre>
 * UnsupportedClassVersionError: atlantis/keyboard/KeyRelay has been compiled by a
 * more recent version of the Java Runtime (class file version 61.0), this version
 * of the Java Runtime only recognizes class file versions up to 52.0
 * </pre>
 *
 * <p>
 * which killed the key thread silently - the bot kept playing, only every
 * shortcut was dead. A build that cannot load its own classes on the runtime it
 * targets is a build failure, and this test says so at once instead of leaving
 * it
 * to a game run to discover.
 * </p>
 */
public class Java8BytecodeTest {

    /** Class-file major version 52 == Java 8. */
    private static final int JAVA_8_MAJOR = 52;

    /**
     * The classes whose class-file version decides whether the keyboard and the
     * launcher work under Wine. They are the ones JNativeHook's dispatch thread
     * touches on the Java 8 runtimes, so they are the ones this test reads.
     */
    private static final Class<?>[] MUST_BE_JAVA_8 = {
            KeyRelay.class,
            atlantis.keyboard.AKeyboard.class,
            atlantis.Atlantis.class,
            main.Main.class,
    };

    @Test
    public void classesTheJava8RuntimesLoadAreJava8Bytecode() throws IOException {
        List<String> tooNew = new ArrayList<>();

        for (Class<?> type : MUST_BE_JAVA_8) {
            int major = classFileMajorVersionOf(type);
            if (major != JAVA_8_MAJOR) {
                tooNew.add(type.getName() + " (major " + major + ", expected " + JAVA_8_MAJOR + ")");
            }
        }

        assertTrue(tooNew.isEmpty(),
                "these classes are not Java 8 bytecode, so the Wine/IDE Java 8 JVMs cannot load them "
                        + "(a compile that forgot --release 8 is the usual cause): " + tooNew);
    }

    /**
     * Reads bytes 6-7 of the class file, which are the major version, without
     * loading the class into this (possibly newer) JVM.
     */
    private static int classFileMajorVersionOf(Class<?> type) throws IOException {
        String resource = "/" + type.getName().replace('.', '/') + ".class";
        try (InputStream in = type.getResourceAsStream(resource)) {
            assertTrue(in != null, "class file not found on the classpath: " + resource);
            try (DataInputStream data = new DataInputStream(in)) {
                assertEquals(0xCAFEBABE, data.readInt(), "not a class file: " + resource);
                data.readUnsignedShort(); // minor version
                return data.readUnsignedShort(); // major version
            }
        }
    }
}
