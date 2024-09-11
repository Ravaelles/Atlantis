package atlantis.map.wall;

public class Structure {
    int tx;
    int ty;
    int width;
    int height;

    public Structure(int tx, int ty, int width, int height) {
        this.tx = tx;
        this.ty = ty;
        this.width = width;
        this.height = height;
    }

    public Structure(int width, int height) {
        this.tx = -1;
        this.ty = -1;
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int tx() {
        return tx;
    }

    public void setTx(int tx) {
        this.tx = tx;
    }

    public int ty() {
        return ty;
    }

    public void setTy(int ty) {
        this.ty = ty;
    }
}