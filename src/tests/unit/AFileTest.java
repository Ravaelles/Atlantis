package tests.unit;

import atlantis.util.AFile;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@Tag("infra")
public class AFileTest {

    private static Path write(Path dir, String name, String content) throws IOException {
        Path file = dir.resolve(name);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));

        return file;
    }

    @Test
    public void missingFileIsReportedNotFatal(@TempDir Path dir) {
        // Was System.exit(-1) inside this method: a leaf helper killed the JVM,
        // so no caller could react and no test could ever cover it.
        Path missing = dir.resolve("nope.txt");

        UncheckedIOException thrown = assertThrows(UncheckedIOException.class,
            () -> AFile.loadFile(missing.toString(), 2, ";"));

        assertTrue(thrown.getMessage().contains(missing.toString()),
            "the message must name the file: " + thrown.getMessage());
        assertInstanceOf(IOException.class, thrown.getCause(), "the original cause must survive");
    }

    @Test
    public void loadsRowsAndSkipsComments(@TempDir Path dir) throws IOException {
        Path file = write(dir, "orders.txt",
            "// a comment line\n"
                + "Probe;66\n"
                + "Zealot;70\n");

        String[][] rows = AFile.loadFile(file.toString(), 2, ";");

        assertEquals(2, rows.length);
        assertArrayEquals(new String[]{"Probe", "66"}, rows[0]);
        assertArrayEquals(new String[]{"Zealot", "70"}, rows[1]);
    }

    @Test
    public void defaultDelimiterIsSemicolon(@TempDir Path dir) throws IOException {
        Path file = write(dir, "orders.txt", "a;b\n");

        String[][] rows = AFile.loadFile(file.toString(), 2, null);

        assertArrayEquals(new String[]{"a", "b"}, rows[0]);
    }

    @Test
    public void savesAppendsAndMeasures(@TempDir Path dir) {
        String path = dir.resolve("out.txt").toString();

        assertFalse(AFile.fileExists(path), "file must not exist before the first write");
        AFile.saveToFile(path, "first\n", true);
        AFile.appendToFile(path, "second\n");

        assertTrue(AFile.fileExists(path));
        assertTrue(AFile.fileSize(path) > 0);
        assertEquals(Arrays.asList("first", "second"), AFile.readTextFileToList(path));
    }

    @Test
    public void removingAMissingFileIsANoOp(@TempDir Path dir) {
        AFile.removeFile(dir.resolve("never-written.txt").toString());

        assertFalse(AFile.fileExists(dir.resolve("never-written.txt").toString()));
    }
}
