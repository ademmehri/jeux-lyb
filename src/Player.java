package src;

import java.util.ArrayList;
import java.util.Arrays;

import src.Exceptions.InvalidMoveException;

public class Player {
    private Vertex position;
    private int score;
    private ArrayList<String> style;

    // getters and sertters
    public Vertex getPosition() { return this.position; }
    public void setPosition(Vertex position) { this.position = position; }
    public int getScore() { return this.score; }
    public void setScore(int score) { this.score = score; }
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

    public Player(Vertex position, String ...styles) {
        this.position = position;
        this.style = new ArrayList<>(Arrays.asList(styles));
        this.score = 100;
    }

    public void move(char move) throws InvalidMoveException {
        Vertex to;
        if      (move == '1') to = this.position.getDownLeft();
        else if (move == '2') to = this.position.getDown();
        else if (move == '3') to = this.position.getDownRight();
        else if (move == '4') to = this.position.getLeft();
        else if (move == '6') to = this.position.getRight();
        else if (move == '7') to = this.position.getUpLeft();
        else if (move == '8') to = this.position.getUp();
        else if (move == '9') to = this.position.getUpRight();
        else throw new InvalidMoveException("Invalid move, use the numbers 1, 2, 3, 4, 6, 7, 8, 9 to move.");

        if (to == null) throw new InvalidMoveException("Invalid move, you can't go into the void.");
        if (to.isWall()) throw new InvalidMoveException("Invalid move, you can't go through walls.");
        this.position = to;
    }

    public void increaseScore(int amount) {
        this.score += amount;
    }
    public void decreaseScore(int amount) {
        this.score -= amount;
    }
}
