package atlantis.util.object;

public class SerializedObjects {
    public static final String ROOT = "files/serialized/";

    public static String filePath(String filename) {
        return ROOT + filename;
    }
}
