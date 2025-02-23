package src;
import java.util.ArrayList;
import java.util.Collections;

class Vertex {
    private char label; //label is not unique
    private Vertex up, down, left, right, upLeft, upRight, downLeft, downRight;
    private int x, y;
    private boolean isWall;
    private boolean isStart;
    private boolean isEnd;
    private ArrayList<String> style;

    public Vertex(char label) {
        this(label, 0, 0);
    }

    public Vertex(char label, int x, int y) {
        this.label = label;
        this.x = x;
        this.y = y;
    }

    // Getters and Setters
    public char getLabel() { return this.label; }
    public void setLabel(char label) { this.label = label; }

    public Vertex getUp() { return this.up; }
    public void setUp(Vertex up) { this.up = up; }

    public Vertex getDown() { return this.down; }
    public void setDown(Vertex down) { this.down = down; }

    public Vertex getLeft() { return this.left; }
    public void setLeft(Vertex left) { this.left = left; }

    public Vertex getRight() { return this.right; }
    public void setRight(Vertex right) { this.right = right; }

    public Vertex getUpLeft() { return this.upLeft; }
    public void setUpLeft(Vertex upLeft) { this.upLeft = upLeft; }

    public Vertex getUpRight() { return this.upRight; }
    public void setUpRight(Vertex upRight) { this.upRight = upRight; }

    public Vertex getDownLeft() { return this.downLeft; }
    public void setDownLeft(Vertex downLeft) { this.downLeft = downLeft; }

    public Vertex getDownRight() { return this.downRight; }
    public void setDownRight(Vertex downRight) { this.downRight = downRight; }

    public int getX() { return this.x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return this.y; }
    public void setY(int y) { this.y = y; }

    public boolean isWall() { return this.isWall; }
    public void setWall(boolean isWall) {
        this.isWall = isWall;
        if (isWall == true) this.addStyles(Style.BG_BLUE);
        else this.removeStyles(Style.BG_BLUE);
    }

    public boolean isStart() { return this.isStart; }
    public void setStart(boolean isStart) {
        this.isStart = isStart;
        if (isStart == true) this.addStyles(Style.BG_YELLOW, Style.FG_BLACK);
        else this.removeStyles(Style.BG_YELLOW, Style.FG_BLACK);
    }

    public boolean isEnd() { return this.isEnd; }
    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
        if (isEnd == true) this.addStyles(Style.BG_GREEN, Style.FG_BLACK);
        else this.removeStyles(Style.BG_GREEN, Style.FG_BLACK);
    }

    public ArrayList<String> getStyle() { return this.style; }
    public void setStyle(ArrayList<String> style) { this.style = style; }
    public void setStyle(String ...styles) {
        this.style = new ArrayList<>();
        for (String style : styles)
            this.style.add(style);
    }

    public void addStyles(String ...style) {
        if (this.style == null)
            this.style = new ArrayList<>();
        for (String s : style) {
            this.style.add(s);
        }
    }

    public void removeStyles(String ...style) {
        if (this.style == null)
            return;
        for (String s : style) {
            this.style.remove(s);
        }
    }

    public ArrayList<Vertex> getNeighbors() {
        ArrayList<Vertex> neighbors = new ArrayList<>();
        neighbors.add(up);
        neighbors.add(upRight);
        neighbors.add(right);
        neighbors.add(downRight);
        neighbors.add(down);
        neighbors.add(downLeft);
        neighbors.add(left);
        neighbors.add(upLeft);
        return neighbors;
    }

    public ArrayList<Vertex> getNeighborsShuffled() {
        ArrayList<Vertex> result = this.getNeighbors();
        Collections.shuffle(result);
        return result;
    }
}