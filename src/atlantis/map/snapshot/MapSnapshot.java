package atlantis.map.snapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A full static copy of one StarCraft map for the StarEngine map
 * library (NEXT #191): walkable + buildable tiles, region shapes,
 * choke points, resources, start locations and preplaced buildings.
 *
 * <p>Zero dependencies, Java 8: {@link #toJson} is hand-built, {@link
 * #parse} is a minimal recursive-descent reader for exactly the shape
 * {@code toJson} writes (objects, arrays, strings, ints, no escapes
 * beyond \\ and \"). The same class travels both ways - the in-game
 * dumper writes it, the sim loader reads it - so the schema cannot
 * drift between writer and reader.</p>
 *
 * <p>Tiles: one char per tile, row-major. '.' is walkable and
 * buildable, 'w' is walkable only, 'x' is neither. Regions: BWEM area
 * ids as dumped ({@code regionOf} holds one id per tile, -1 when the
 * tile belongs to no area); {@code regions} carries each id once with
 * its centroid and neighbour ids. Chokes carry center tiles, the raw
 * width number, end tiles and the two region ids they join.</p>
 */
public final class MapSnapshot {

    public String map = "";
    public int w;
    public int h;
    public List<String> tiles = new ArrayList<>();
    public List<Integer> regionOf = new ArrayList<>();
    public List<Region> regions = new ArrayList<>();
    public List<Choke> chokes = new ArrayList<>();
    public List<Res> resources = new ArrayList<>();
    public List<Pos> starts = new ArrayList<>();
    public List<Unit> units = new ArrayList<>();

    public static final class Region {
        public int id;
        public int cx;
        public int cy;
        public List<Integer> neighbors = new ArrayList<>();
    }

    public static final class Choke {
        public int x;
        public int y;
        public int w;
        public int r1 = -1;
        public int r2 = -1;
        public int x1;
        public int y1;
        public int x2;
        public int y2;
    }

    public static final class Res {
        /** "M" for minerals, "G" for geyser. */
        public String t = "";
        public int x;
        public int y;
        public int amount;
    }

    public static final class Pos {
        public int x;
        public int y;
    }

    public static final class Unit {
        public String t = "";
        public int x;
        public int y;
    }

    // =========================================================

    public String toJson() {
        StringBuilder out = new StringBuilder(1024);
        out.append("{\"map\":\"").append(escape(map)).append("\",\"w\":").append(w)
            .append(",\"h\":").append(h).append(",\"tiles\":[");
        for (int i = 0; i < tiles.size(); i++) {
            if (i > 0) out.append(',');
            out.append('"').append(tiles.get(i)).append('"');
        }
        out.append("],\"regionOf\":[");
        for (int i = 0; i < regionOf.size(); i++) {
            if (i > 0) out.append(',');
            out.append(regionOf.get(i));
        }
        out.append("],\"regions\":[");
        for (int i = 0; i < regions.size(); i++) {
            if (i > 0) out.append(',');
            Region r = regions.get(i);
            out.append("{\"id\":").append(r.id)
                .append(",\"cx\":").append(r.cx).append(",\"cy\":").append(r.cy)
                .append(",\"neighbors\":[");
            for (int j = 0; j < r.neighbors.size(); j++) {
                if (j > 0) out.append(',');
                out.append(r.neighbors.get(j));
            }
            out.append("]}");
        }
        out.append("],\"chokes\":[");
        for (int i = 0; i < chokes.size(); i++) {
            if (i > 0) out.append(',');
            Choke c = chokes.get(i);
            out.append("{\"x\":").append(c.x).append(",\"y\":").append(c.y)
                .append(",\"w\":").append(c.w).append(",\"r1\":").append(c.r1)
                .append(",\"r2\":").append(c.r2).append(",\"x1\":").append(c.x1)
                .append(",\"y1\":").append(c.y1).append(",\"x2\":").append(c.x2)
                .append(",\"y2\":").append(c.y2).append('}');
        }
        out.append("],\"resources\":[");
        for (int i = 0; i < resources.size(); i++) {
            if (i > 0) out.append(',');
            Res r = resources.get(i);
            out.append("{\"t\":\"").append(escape(r.t)).append("\",\"x\":").append(r.x)
                .append(",\"y\":").append(r.y).append(",\"amount\":").append(r.amount).append('}');
        }
        out.append("],\"starts\":[");
        for (int i = 0; i < starts.size(); i++) {
            if (i > 0) out.append(',');
            Pos p = starts.get(i);
            out.append("{\"x\":").append(p.x).append(",\"y\":").append(p.y).append('}');
        }
        out.append("],\"units\":[");
        for (int i = 0; i < units.size(); i++) {
            if (i > 0) out.append(',');
            Unit u = units.get(i);
            out.append("{\"t\":\"").append(escape(u.t)).append("\",\"x\":").append(u.x)
                .append(",\"y\":").append(u.y).append('}');
        }
        out.append("]}");

        return out.toString();
    }

    private static String escape(String value) {
        if (value == null) return "";
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') out.append('\\');
            out.append(c);
        }

        return out.toString();
    }

    // =========================================================

    /** Reads exactly what {@link #toJson} writes; throws on anything else. */
    @SuppressWarnings("unchecked")
    public static MapSnapshot parse(String json) {
        Object root = new Parser(json).parseValue();
        if (!(root instanceof Map)) throw new IllegalArgumentException("snapshot root is not an object");

        Map<String, Object> obj = (Map<String, Object>) root;
        MapSnapshot snap = new MapSnapshot();
        snap.map = str(obj, "map");
        snap.w = num(obj, "w");
        snap.h = num(obj, "h");
        for (Object row : list(obj, "tiles")) snap.tiles.add((String) row);
        for (Object id : list(obj, "regionOf")) snap.regionOf.add(((Number) id).intValue());
        for (Object ro : list(obj, "regions")) {
            Map<String, Object> rm = (Map<String, Object>) ro;
            Region r = new Region();
            r.id = num(rm, "id");
            r.cx = num(rm, "cx");
            r.cy = num(rm, "cy");
            for (Object n : list(rm, "neighbors")) r.neighbors.add(((Number) n).intValue());
            snap.regions.add(r);
        }
        for (Object co : list(obj, "chokes")) {
            Map<String, Object> cm = (Map<String, Object>) co;
            Choke c = new Choke();
            c.x = num(cm, "x");
            c.y = num(cm, "y");
            c.w = num(cm, "w");
            c.r1 = num(cm, "r1");
            c.r2 = num(cm, "r2");
            c.x1 = num(cm, "x1");
            c.y1 = num(cm, "y1");
            c.x2 = num(cm, "x2");
            c.y2 = num(cm, "y2");
            snap.chokes.add(c);
        }
        for (Object ro : list(obj, "resources")) {
            Map<String, Object> rm = (Map<String, Object>) ro;
            Res r = new Res();
            r.t = str(rm, "t");
            r.x = num(rm, "x");
            r.y = num(rm, "y");
            r.amount = num(rm, "amount");
            snap.resources.add(r);
        }
        for (Object po : list(obj, "starts")) {
            Map<String, Object> pm = (Map<String, Object>) po;
            Pos p = new Pos();
            p.x = num(pm, "x");
            p.y = num(pm, "y");
            snap.starts.add(p);
        }
        for (Object uo : list(obj, "units")) {
            Map<String, Object> um = (Map<String, Object>) uo;
            Unit u = new Unit();
            u.t = str(um, "t");
            u.x = num(um, "x");
            u.y = num(um, "y");
            snap.units.add(u);
        }

        return snap;
    }

    private static String str(Map<String, Object> obj, String key) {
        Object value = obj.get(key);
        if (value == null) return "";

        return value.toString();
    }

    private static int num(Map<String, Object> obj, String key) {
        Object value = obj.get(key);
        if (value instanceof Number) return ((Number) value).intValue();

        throw new IllegalArgumentException("snapshot field '" + key + "' is not a number");
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(Map<String, Object> obj, String key) {
        Object value = obj.get(key);
        if (value == null) return new ArrayList<>();
        if (value instanceof List) return (List<Object>) value;

        throw new IllegalArgumentException("snapshot field '" + key + "' is not a list");
    }

    /** Minimal JSON reader: objects, arrays, strings, integers. No floats, no literals. */
    static final class Parser {
        private final String text;
        private int pos;

        Parser(String text) {
            this.text = text;
        }

        Object parseValue() {
            skipWs();
            if (pos >= text.length()) throw new IllegalArgumentException("unexpected end of snapshot");
            char c = text.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == '-' || (c >= '0' && c <= '9')) return parseNumber();

            throw new IllegalArgumentException("unexpected char '" + c + "' at " + pos);
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> obj = new LinkedHashMap<>();
            pos++;
            skipWs();
            if (peek() == '}') {
                pos++;

                return obj;
            }
            while (true) {
                skipWs();
                String key = parseString();
                skipWs();
                expect(':');
                Object value = parseValue();
                obj.put(key, value);
                skipWs();
                char c = peek();
                if (c == ',') {
                    pos++;
                    continue;
                }
                if (c == '}') {
                    pos++;

                    return obj;
                }
                throw new IllegalArgumentException("expected ',' or '}' at " + pos);
            }
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++;
            skipWs();
            if (peek() == ']') {
                pos++;

                return list;
            }
            while (true) {
                list.add(parseValue());
                skipWs();
                char c = peek();
                if (c == ',') {
                    pos++;
                    continue;
                }
                if (c == ']') {
                    pos++;

                    return list;
                }
                throw new IllegalArgumentException("expected ',' or ']' at " + pos);
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder out = new StringBuilder();
            while (pos < text.length()) {
                char c = text.charAt(pos++);
                if (c == '"') return out.toString();
                if (c == '\\') {
                    if (pos >= text.length()) break;
                    out.append(text.charAt(pos++));
                    continue;
                }
                out.append(c);
            }
            throw new IllegalArgumentException("unterminated string");
        }

        private Integer parseNumber() {
            int start = pos;
            if (peek() == '-') pos++;
            while (pos < text.length() && Character.isDigit(text.charAt(pos))) pos++;
            try {
                return Integer.valueOf(text.substring(start, pos));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("bad number at " + start);
            }
        }

        private void expect(char c) {
            skipWs();
            if (pos >= text.length() || text.charAt(pos) != c) {
                throw new IllegalArgumentException("expected '" + c + "' at " + pos);
            }
            pos++;
        }

        private char peek() {
            skipWs();
            if (pos >= text.length()) throw new IllegalArgumentException("unexpected end of snapshot");

            return text.charAt(pos);
        }

        private void skipWs() {
            while (pos < text.length()) {
                char c = text.charAt(pos);
                if (c != ' ' && c != '\t' && c != '\n' && c != '\r') return;
                pos++;
            }
        }
    }
}
