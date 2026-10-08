package tests.unit;

import atlantis.util.AFile;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code AFile.loadFile} must not eat the last character of a value that ends
 * in
 * the delimiter.
 *
 * <p>
 * The bug this pins, measured 2026-10-08: {@code ENV} contains
 * {@code BWAPI_DATA_PATH=/sc-ai/Atlantis/bwapi-data/}, and
 * {@code String.split("=")}
 * drops the trailing empty field, so the path arrived as
 * {@code /sc-ai/Atlantis/bwapi-data} <em>without</em> the workaround being
 * obvious: the loader then built
 * {@code /sc-ai/Atlantis/bwapi-dataread/build_orders/...}
 * and the bot played a whole game with no build order.
 * </p>
 */
public class AFileLoadFileTest {

    private static File writeTemp(String name, String content) throws Exception {
        File file = File.createTempFile(name, ".txt");
        file.deleteOnExit();
        try (Writer out = new FileWriter(file)) {
            out.write(content);
        }
        return file;
    }

    @Test
    public void aValueEndingInTheDelimiterKeepsItsLastCharacter() throws Exception {
        File env = writeTemp("env", "BWAPI_DATA_PATH=/sc-ai/Atlantis/bwapi-data/\nLOCAL=true\n");

        String[][] parsed = AFile.loadFile(env.getAbsolutePath(), 2, "=");

        String value = null;
        for (String[] row : parsed) {
            if (row.length > 1 && row[0].trim().equals("BWAPI_DATA_PATH"))
                value = row[1];
        }

        assertEquals("/sc-ai/Atlantis/bwapi-data/", value,
                "the trailing slash is part of the path; losing it points the loader at a"
                        + " directory that does not exist");
    }

    @Test
    public void ordinaryValuesAreUnchanged() throws Exception {
        File env = writeTemp("env2", "LOCAL=true\nGAME_LAUNCHER=OPENBW\n");

        String[][] parsed = AFile.loadFile(env.getAbsolutePath(), 2, "=");

        assertEquals(2, parsed.length);
        assertEquals("true", parsed[0][1]);
        assertEquals("OPENBW", parsed[1][1]);
    }

    @Test
    public void aValueWithoutTrailingDelimiterIsNotModified() throws Exception {
        File env = writeTemp("env3", "NAME=Atlantis\n");

        String[][] parsed = AFile.loadFile(env.getAbsolutePath(), 2, "=");

        assertEquals("Atlantis", parsed[0][1]);
        assertTrue(parsed[0][1].length() == "Atlantis".length());
    }

    @Test
    public void aRealBuildOrderRowStillParses() throws Exception {
        // The other user of this loader: build orders are `9 - Gateway - x2`
        // (delimiter ";", then the " - " fallback). Guarding the ENV fix must not
        // disturb them.
        File orders = writeTemp("orders", "9 - Gateway\n12 - Pylon\n");

        String[][] parsed = AFile.loadFile(orders.getAbsolutePath(), 2, ";");

        assertEquals(2, parsed.length);
        assertEquals("9", parsed[0][0].trim());
        assertEquals("Gateway", parsed[0][1].trim());
    }
}
