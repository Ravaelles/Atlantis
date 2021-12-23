package jps.main.java.jps;

/**
 * @author Kevin
 */
public abstract class Node {
    public int x;
    public int y;

    public boolean walkable = true;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setWalkable(boolean walkable) { this.walkable = walkable; }
    public boolean isWalkable() { return walkable; }
}
