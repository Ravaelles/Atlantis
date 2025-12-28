package atlantis.cherryvis.simple;

import java.util.List;

public class ACherryVis_GlobalStates {
    public static String build() {
        StringBuilder result = new StringBuilder();

        List<ACherryVis_GlobalState> all = ACherryVis_GlobalState.all;
        for (int i = 0; i < all.size(); i++) {
            ACherryVis_GlobalState log = all.get(i);
            result.append(log.toJson());

            if (i < all.size() - 1) {
                result.append(",");
            }
        }

        return result.toString();
    }
}
