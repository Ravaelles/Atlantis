package bweb;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Block {
    public enum Piece { Small, Medium, Large, Addon, Row, Space }
    public enum BlockType { None, Start, Production, Proxy, Defensive, Supply }

    private int w = 0, h = 0;
    private BWAPI.TilePosition tile;
    private BWAPI.Position center;
    private Set<BWAPI.TilePosition> smallTiles = new HashSet<>();
    private Set<BWAPI.TilePosition> mediumTiles = new HashSet<>();
    private Set<BWAPI.TilePosition> largeTiles = new HashSet<>();
    private BlockType type = BlockType.None;

    public Block() {}

    public Block(BWAPI.TilePosition _tile, Map<BWAPI.TilePosition, Piece> _pieces, int _w, int _h, BlockType _type) {
        tile = _tile;
        type = _type;
        w = _w;
        h = _h;
        center = new BWAPI.Position(tile.x + w / 2, tile.y + h / w);
        for (Map.Entry<BWAPI.TilePosition, Piece> entry : _pieces.entrySet()) {
            BWAPI.TilePosition placement = entry.getKey();
            Piece piece = entry.getValue();
            if (piece == Piece.Small || piece == Piece.Addon) smallTiles.add(placement);
            if (piece == Piece.Medium) mediumTiles.add(placement);
            if (piece == Piece.Large) largeTiles.add(placement);
        }
    }

    public BWAPI.TilePosition getTilePosition() { return tile; }
    public BWAPI.Position getCenter() { return center; }
    public Set<BWAPI.TilePosition> getSmallTiles() { return smallTiles; }
    public Set<BWAPI.TilePosition> getMediumTiles() { return mediumTiles; }
    public Set<BWAPI.TilePosition> getLargeTiles() { return largeTiles; }
    public Set<BWAPI.TilePosition> getPlacements(BWAPI.UnitType type) {
        if (type.tileWidth() == 4) return largeTiles;
        if (type.tileWidth() == 3) return mediumTiles;
        return smallTiles;
    }
    public boolean isProxy() { return type == BlockType.Proxy; }
    public boolean isDefensive() { return type == BlockType.Defensive; }
    public int width() { return w; }
    public int height() { return h; }
    public void insertSmall(BWAPI.TilePosition here) { smallTiles.add(here); }
    public void insertMedium(BWAPI.TilePosition here) { mediumTiles.add(here); }
    public void insertLarge(BWAPI.TilePosition here) { largeTiles.add(here); }
    public void draw() {
        // Drawing logic would go here, using a Java graphics API or stub
    }
} 