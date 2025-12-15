package atlantis.cherryvis.simple;

public class ACherryVis_GlobalLogs {
    public static String build() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < ACherryVis_GlobalLog.all.size(); i++) {
            ACherryVis_GlobalLog log = ACherryVis_GlobalLog.all.get(i);
            result.append(log.toJson());

            if (i < ACherryVis_GlobalLog.all.size() - 1) {
                result.append(",");
            }
        }

        return result.toString();
    }
}
