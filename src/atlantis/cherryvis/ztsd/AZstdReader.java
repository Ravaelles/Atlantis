package atlantis.cherryvis.ztsd;

import atlantis.game.A;
import com.github.luben.zstd.ZstdInputStream;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

public class AZstdReader {
    public static String readZstdFile(String fileName) throws Exception {
        try (ZstdInputStream zIn = new ZstdInputStream(new FileInputStream(fileName));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int read;

            while ((read = zIn.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }

            String content = out.toString();

            A.saveToFile("D://clean.json", content, true);

            return content;
        }
    }
}