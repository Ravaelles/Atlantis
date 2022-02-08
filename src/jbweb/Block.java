package jbweb;

import bwapi.*;

import java.util.ArrayList;
import java.util.List;

public class Block {
    private int w = 0;
    private int h = 0;
    private final TilePosition tile;
    private List<TilePosition> smallTiles = new ArrayList<>();
    private List<TilePosition> mediumTiles = new ArrayList<>();
    private List<TilePosition> largeTiles = new ArrayList<>();
    private final boolean proxy;
    private final boolean defensive;

    public Block() {
        tile = null;
        proxy = false;
        defensive = false;
    }

    public Block(TilePosition _tile, List<Piece> _pieces, boolean _proxy, boolean _defensive) {
        tile = _tile;
        proxy = _proxy;
        defensive = _defensive;

        // Arrange pieces
        int rowHeight = 0;
        int rowWidth = 0;
        TilePosition here = tile;
        for (Piece p : _pieces) {
            if (p == Piece.Small) {
                smallTiles.add(here);
                here = new TilePosition(here.x + 2, here.y);
                rowWidth += 2;
                rowHeight = Math.max(rowHeight, 2);
            }
            if (p == Piece.Medium) {
                mediumTiles.add(here);
                here = new TilePosition(here.x + 3, here.y);
                rowWidth += 3;
                rowHeight = Math.max(rowHeight, 2);
            }
            if (p == Piece.Large) {
                if (JBWEB.game.self().getRace() == Race.Zerg && !JBWEB.game.canBuildHere(here, UnitType.Zerg_Hatchery)) {
                    continue;
                }
                largeTiles.add(here);
                here = new TilePosition(here.x + 4, here.y);
                rowWidth += 4;
                rowHeight = Math.max(rowHeight, 3);
            }
            if (p == Piece.Addon) {
                TilePosition insertTile = new TilePosition(here.x, here.y + 1);
                smallTiles.add(insertTile);
                here = new TilePosition(here.x + 2, here.y);
                rowWidth += 2;
                rowHeight = Math.max(rowHeight, 2);
            }
            if (p == Piece.Row) {
                w = Math.max(w, rowWidth);
                h += rowHeight;
                rowWidth = 0;
                rowHeight = 0;
                here = new TilePosition(here.x, here.y + h);
            }
        }

        // In case there is no row piece
        w = Math.max(w, rowWidth);
        h += rowHeight;
    }

    /// Returns the top left TilePosition of this Block.
    public TilePosition getTilePosition() {
        return tile;
    }

    /// Returns the set of TilePositions that belong to 2x2 (small) buildings.
    public List<TilePosition> getSmallTiles() {
        return smallTiles;
    }

    /// Returns the set of TilePositions that belong to 3x2 (medium) buildings.
    public List<TilePosition> getMediumTiles() {
        return mediumTiles;
    }

    /// Returns the set of TilePositions that belong to 4x3 (large) buildings.
    public List<TilePosition> getLargeTiles() {
        return largeTiles;
    }

    /// Inserts a 2x2 (small) building at this location.
    public void insertSmall(TilePosition here) {
        smallTiles.add(here);
    }

    /// Inserts a 3x2 (medium) building at this location.
    public void insertMedium(TilePosition here) {
        mediumTiles.add(here);
    }

    /// Inserts a 4x3 (large) building at this location.
    public void insertLarge(TilePosition here) {
        largeTiles.add(here);
    }

    /// Returns the width of the Block in TilePositions.
    public int width() {
        return w;
    }

    /// Returns the height of the Block in TilePositions.
    public int height() {
        return h;
    }

    /// Returns true if this Block was generated for proxy usage.
    public boolean isProxy() {
        return proxy;
    }

    /// Returns true if this Block was generated for defensive usage.
    public boolean isDefensive() {
        return defensive;
    }

    /// Draws all the features of the Block.
    public void draw() {
        Color color = JBWEB.game.self().getColor();
        Text textColor = color.id == 185 ? Text.DarkGreen : JBWEB.game.self().getTextColor();

        // Draw boxes around each feature
        for (TilePosition tile : smallTiles) {
            Position p1 = new Position(tile.toPosition().x + 65, tile.toPosition().x + 65);
            Position p2 = new Position(tile.toPosition().x + 52, tile.toPosition().x + 52);
            JBWEB.game.drawBoxMap(new Position(tile), p1, color);
            JBWEB.game.drawTextMap(p2, "%cB", textColor);
        }
        for (TilePosition tile : mediumTiles) {
            Position p1 = new Position(tile.toPosition().x + 97, tile.toPosition().x + 65);
            Position p2 = new Position(tile.toPosition().x + 84, tile.toPosition().x + 52);
            JBWEB.game.drawBoxMap(new Position(tile), p1, color);
            JBWEB.game.drawTextMap(p2, "%cB", textColor);
        }
        for (TilePosition tile : largeTiles) {
            Position p1 = new Position(tile.toPosition().x + 129, tile.toPosition().x + 97);
            Position p2 = new Position(tile.toPosition().x + 116, tile.toPosition().x + 84);
            JBWEB.game.drawBoxMap(new Position(tile), p1, color);
            JBWEB.game.drawTextMap(p2, "%cB", textColor);
        }
    }
}
