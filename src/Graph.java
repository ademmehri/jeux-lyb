package src;
import java.util.ArrayList;

public class Graph {
    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        UP_LEFT,
        UP_RIGHT,
        DOWN_LEFT,
        DOWN_RIGHT
    }

    private ArrayList<Vertex> vertices;
    
    public Graph() {
        this.vertices = new ArrayList<>();
    }

    public ArrayList<Vertex> getVertices() {
        return this.vertices;
    }

    public void addVertex(Vertex newVertex) {
        for (Vertex vertex : this.vertices) {
            if (vertex.getX() == newVertex.getX() && vertex.getY() == newVertex.getY()) {
                throw new IllegalArgumentException("A vertex already exists at this position.");
            } else if (vertex.getX() == newVertex.getX() && vertex.getY() == newVertex.getY() - 1) {
                vertex.setDown(newVertex);
                newVertex.setUp(vertex);
            } else if (vertex.getX() == newVertex.getX() && vertex.getY() == newVertex.getY() + 1) {
                vertex.setUp(newVertex);
                newVertex.setDown(vertex);
            } else if (vertex.getX() == newVertex.getX() - 1 && vertex.getY() == newVertex.getY()) {
                vertex.setRight(newVertex);
                newVertex.setLeft(vertex);
            } else if (vertex.getX() == newVertex.getX() + 1 && vertex.getY() == newVertex.getY()) {
                vertex.setLeft(newVertex);
                newVertex.setRight(vertex);
            } else if (vertex.getX() == newVertex.getX() - 1 && vertex.getY() == newVertex.getY() - 1) {
                vertex.setDownRight(newVertex);
                newVertex.setUpLeft(vertex);
            } else if (vertex.getX() == newVertex.getX() + 1 && vertex.getY() == newVertex.getY() - 1) {
                vertex.setDownLeft(newVertex);
                newVertex.setUpRight(vertex);
            } else if (vertex.getX() == newVertex.getX() - 1 && vertex.getY() == newVertex.getY() + 1) {
                vertex.setUpRight(newVertex);
                newVertex.setDownLeft(vertex);
            } else if (vertex.getX() == newVertex.getX() + 1 && vertex.getY() == newVertex.getY() + 1) {
                vertex.setUpLeft(newVertex);
                newVertex.setDownRight(vertex);
            }
        }
        this.vertices.add(newVertex);
    }

    public void addVertexAt(char label, int x, int y) {
        this.addVertex(new Vertex(label, x, y));
    }

    public void attachVertexTo(Vertex what, Vertex to, Graph.Direction direction) {
        // the what vertex's position is not important, it will change
        int x = to.getX();
        int y = to.getY();
        if      (direction == Graph.Direction.UP)         { y--; }
        else if (direction == Graph.Direction.DOWN)       { y++; }
        else if (direction == Graph.Direction.LEFT)       { x--; }
        else if (direction == Graph.Direction.RIGHT)      { x++; }
        else if (direction == Graph.Direction.UP_LEFT)    { x--; y--; }
        else if (direction == Graph.Direction.UP_RIGHT)   { x++; y--; }
        else if (direction == Graph.Direction.DOWN_LEFT)  { x--; y++; }
        else if (direction == Graph.Direction.DOWN_RIGHT) { x++; y++; }
        what.setX(x);
        what.setY(y);
        this.addVertex(what);
    }

    public void attachVertexTo(char label, Vertex to, Graph.Direction direction) {
        this.attachVertexTo(new Vertex(label), to, direction);
    }

    public Vertex getVertexAt(int x, int y) {
        for (Vertex vertex : this.vertices) {
            if (vertex.getX() == x && vertex.getY() == y) return vertex;
        }
        return null;
    }

    public int getColumnsCount() {
        int max = 0;
        for (Vertex vertex : this.vertices) {
            if (vertex.getX() > max) max = vertex.getX();
        }
        return max + 1;
    }

    public int getRowsCount() {
        int max = 0;
        for (Vertex vertex : this.vertices) {
            if (vertex.getY() > max) max = vertex.getY();
        }
        return max + 1;
    }

    // used for rendering
    public char[][] toMatrix() {
        int rows = this.getRowsCount();
        int columns = this.getColumnsCount();
        char[][] matrix = new char[rows][columns];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                Vertex vertex = this.getVertexAt(x, y);
                if (vertex == null)        matrix[y][x] = '\0';
                else if (vertex.isWall())  matrix[y][x] = '#';
                else if (vertex.isStart()) matrix[y][x] = '$';
                else if (vertex.isEnd())   matrix[y][x] = '£';
                else                       matrix[y][x] = vertex.getLabel();
            }
        }
        return matrix;
    }
}