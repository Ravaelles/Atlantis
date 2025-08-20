package cherryvis.ztsd;

import atlantis.util.log.ErrorLog;
import com.github.luben.zstd.Zstd;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AZstdWriter {
    public static void writeZstdFile(String fileName, String content) {
        byte[] input = content.getBytes(StandardCharsets.UTF_8);

        // Zstd recommended compression level: 3–6 (fast)
        int level = 5;

        byte[] compressed = Zstd.compress(input, level);

        try (FileOutputStream out = new FileOutputStream(fileName)) {
            out.write(compressed);
        } catch (IOException e) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("writeZstdFile exception: " + e.getMessage());
        }
    }
}