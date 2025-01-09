package atlantis.util.object.not_needed;

public class SerializedObjects {
    public static final String ROOT = "files/serialized/";

    public static String filePath(String filename) {
        return ROOT + filename;
    }
}
