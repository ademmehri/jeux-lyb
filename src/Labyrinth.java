package src;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Stack;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import src.Exceptions.NoWordsCombinationException;

public class Labyrinth {
    // the terminal width can not hold more than 26 vertices
    // the terminal height can not hold more than 10 vertices
    private int maxColumns;
    private int maxRows; // it can hold up to 11, but then the last row will cause rerendering issues
    private int scalar;
    private ArrayList<String> dictionary;
    private Graph graph;
    private Vertex start;
    private Vertex end;
    // private ArrayList<ArrayList<Vertex>> distinctPaths;
    private ArrayList<
        Pair<
            ArrayList<Vertex>,
            ArrayList<String>
        >
    > distinctPaths; // the first element is the path, the second is the words that fit the path

    // getters and setters
    public int getMaxColumns() { return this.maxColumns; }
    public int getMaxRows() { return this.maxRows; }
    public int getScalar() { return this.scalar; }
    public ArrayList<String> getDictionary() { return this.dictionary; }
    public Graph getGraph() { return this.graph; }
    public Vertex getStart() { return this.start; }
    public Vertex getEnd() { return this.end; }
    public ArrayList<Pair<ArrayList<Vertex>, ArrayList<String>>> getDistinctPaths() { return this.distinctPaths; }


    public Labyrinth(String filePath, Game.Difficulty difficulty) throws IOException, InterruptedException, ExecutionException {
        this.scalar = difficulty == Game.Difficulty.EASY ? 2 : 1;
        this.maxColumns = 26 / this.scalar;
        this.maxRows = 10 / this.scalar;
        this.dictionary = Labyrinth.readDictionary(filePath);
        Collections.shuffle(this.dictionary);
        // if the difficulty is hard, the player should find the words flipped
        if (difficulty == Game.Difficulty.HARD) this.flipDictionary();

        ArrayList<ArrayList<Vertex>> emptyDistinctPaths;
        long timeout = 1000; // 1000 ms = 1 seconds
        int minPathsCount = 2;
        while (true) { // keep creating new graphs until at least `minPathsCount` path(s) is found in under `timeout` seconds
            try {
                this.graph = this.createEmpty();
                emptyDistinctPaths = TimeoutExecuter.execute(this::searchEmptyDistinctPaths, timeout); // search for paths in `timeout` seconds
                if (emptyDistinctPaths.size() < minPathsCount) continue; // the number of paths should be at least `minPathsCount`
                this.distinctPaths = this.associateWordsToPaths(emptyDistinctPaths);
                break;
            }
            catch (TimeoutException e) { /* Timeout, will create a new labyrinth */ }
            catch (NoWordsCombinationException e) { /* No words combination found, will create a new labyrinth */ }
        }
        this.fill();
    }
// Cette méthode génère un labyrinthe vide, en créant un graphe avec des sommets et des arêtes. Les positions de départ et d'arrivée sont choisies aléatoirement, et le labyrinthe est construit à l'aide d'une pile de vertices, tout en s'assurant que le labyrinthe reste connecté et sans murs trop nombreux.
    public Graph createEmpty() {
        Graph result = new Graph();
        Random random = new Random();
        Stack<Vertex> verticesStack = new Stack<>();

        double wallPercentage = 15.0; // 15% chance of being a wall
        double minDistance = 7; // the minimum euclidean distance between the start and the end vertices
        double distance;
        // int maxVertices = this.maxColumns * this.maxRows; // may be used to limit the number of vertices
        // note: if the number of vertices is limited, the end vertex may not be reached

        do { // make sure that the end vertex and the start vertex are far enough using the euclidean distance
            this.start = new Vertex('$', random.nextInt(this.maxColumns), random.nextInt(this.maxRows));
            this.end   = new Vertex('@', random.nextInt(this.maxColumns), random.nextInt(this.maxRows));
            distance = Math.sqrt(Math.pow(this.end.getX() - this.start.getX(), 2) + Math.pow(this.end.getY() - this.start.getY(), 2));
        } while (distance < minDistance); // the distance should be at least `minDistance`

        this.start.setStart(true);
        this.end.setEnd(true);
        result.addVertex(this.start);
        result.addVertex(this.end);
        verticesStack.push(this.start);

        while (!verticesStack.isEmpty() /* && maxVertices-- != 0 */) {
            Vertex currentVertex = verticesStack.getLast();
            ArrayList<Graph.Direction> allowedDirections = this.getAllowedDirections(currentVertex);
            if (allowedDirections.size() == 0) {
                verticesStack.pop();
                continue;
            }
            Graph.Direction direction = allowedDirections.get(random.nextInt(allowedDirections.size())); // choose a random direction
            Vertex newVertex = new Vertex('\0'); // empty vertex represented by a '\0'
            boolean isWall = random.nextInt(100) < wallPercentage;
            newVertex.setWall(isWall);
            result.attachVertexTo(newVertex, currentVertex, direction);
            if (!isWall) verticesStack.add(newVertex);
        }
        verticesStack.push(this.end);
        return result;
    }
//Une fois le graphe généré, cette méthode remplit les vertices du graphe avec des lettres du dictionnaire et ajoute des murs où nécessaire.
    public void fill() {
        for (Pair<ArrayList<Vertex>, ArrayList<String>> pair : this.distinctPaths) {
            ArrayList<Vertex> path = pair.getKey();
            ArrayList<String> words = pair.getValue();

            String wordConcatinated = String.join("", words);

            for (int i = 0; i < path.size(); i++) {
                path.get(i).setLabel(wordConcatinated.charAt(i));
            }
        }
        Random random = new Random();
        double wallPercentage = 50.0; // 50% chance of being a wall
        for (Vertex v : this.graph.getVertices()) {
            if (v.getLabel() == '\0') {
                boolean isWall = random.nextInt(100) < wallPercentage;
                if (isWall) v.setWall(isWall);
                else v.setLabel((char) (random.nextInt(0, 26) + 'a'));
            }
        }
    }
    //Cette méthode détermine les directions possibles pour chaque sommet du graphe, en vérifiant quelles arêtes sont déjà présentes.
    public ArrayList<Graph.Direction> getAllowedDirections(Vertex currentVertex) {
        ArrayList<Graph.Direction> allowedDirections = new ArrayList<>();
        if (currentVertex.getX() > 0 && currentVertex.getLeft() == null)
            allowedDirections.add(Graph.Direction.LEFT);
        if (currentVertex.getX() < this.maxColumns - 1 && currentVertex.getRight() == null)
            allowedDirections.add(Graph.Direction.RIGHT);
        if (currentVertex.getY() > 0 && currentVertex.getUp() == null)
            allowedDirections.add(Graph.Direction.UP);
        if (currentVertex.getY() < this.maxRows - 1 && currentVertex.getDown() == null)
            allowedDirections.add(Graph.Direction.DOWN);
        if (currentVertex.getX() > 0 && currentVertex.getY() > 0 && currentVertex.getUpLeft() == null)
            allowedDirections.add(Graph.Direction.UP_LEFT);
        if (currentVertex.getX() < this.maxColumns - 1 && currentVertex.getY() > 0 && currentVertex.getUpRight() == null)
            allowedDirections.add(Graph.Direction.UP_RIGHT);
        if (currentVertex.getX() > 0 && currentVertex.getY() < this.maxRows - 1 && currentVertex.getDownLeft() == null)
            allowedDirections.add(Graph.Direction.DOWN_LEFT);
        if (currentVertex.getX() < this.maxColumns - 1 && currentVertex.getY() < this.maxRows - 1 && currentVertex.getDownRight() == null)
            allowedDirections.add(Graph.Direction.DOWN_RIGHT);

        return allowedDirections;
    }
    // Lit un fichier pour obtenir une liste de mots, qui seront ensuite utilisés pour créer des mots dans le labyrinthe.

    public static ArrayList<String> readDictionary(String filePath) throws IOException {
        ArrayList<String> words = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
        String word;
        while ((word = bufferedReader.readLine()) != null) {
            words.add(word);
        }
        bufferedReader.close();
        return words;
    }

    public void addVertexAt(char label, int x, int y) {
        if (x < 0 || x >= this.maxColumns || y < 0 || y >= this.maxRows)
            throw new IllegalArgumentException("The vertex can not be added outside the terminal.");
        this.graph.addVertexAt(label, x, y);
    }

    public void attachVertexTo(char label, Vertex to, Graph.Direction direction) {
        int x = to.getX();
        int y = to.getY();
        if (x == this.maxColumns - 1 && direction == Graph.Direction.RIGHT ||
            y == this.maxRows && direction == Graph.Direction.DOWN ||
            x == 0 && direction == Graph.Direction.LEFT ||
            y == 0 && direction == Graph.Direction.UP
        ) {
            throw new IllegalArgumentException("The vertex can not be added outside the terminal.");
        }
        this.graph.attachVertexTo(label, to, direction);
    }
    // Si la difficulté du jeu est "HARD", cette méthode inverse les mots du dictionnaire pour ajouter de la difficulté.
    public void flipDictionary() {
        for (int i = 0; i < this.dictionary.size(); i++)
            this.dictionary.set(i, new StringBuilder(this.dictionary.get(i)).reverse().toString());
    }
//Recherche les chemins distincts à travers le graphe à partir du sommet de départ, en utilisant une approche de recherche en profondeur (DFS). Elle s'assure que le chemin ne contient pas de murs et trouve les chemins valides menant au sommet d'arrivée.
    public ArrayList<ArrayList<Vertex>> searchEmptyDistinctPaths() {
        ArrayList<ArrayList<Vertex>> distinctPaths = new ArrayList<>();
        ArrayList<Vertex> currentPath = new ArrayList<>();
        HashSet<Vertex> marked = new HashSet<>(); // this will hold the vertices that are already in a path
        Stack<Pair<Vertex, Vertex>> stack = new Stack<>(); // the first vertex is the parent, the second is the current, i am storing the parent to correctly backtrack the path

        for (Vertex neighbor : this.start.getNeighborsShuffled()) {
            if (neighbor != null && !neighbor.isWall() && !neighbor.isEnd()) {
                stack.push(new Pair<>(null, neighbor));
            }
        }

        int initialStartingVerticesCount = stack.size();

        while (!stack.isEmpty()) {
            Pair<Vertex, Vertex> currentPair = stack.pop();
            Vertex currentParent = currentPair.getKey();
            Vertex currentVertex = currentPair.getValue();

            if (marked.contains(currentVertex)) { // this could happen when a path is found, and the initial neighbors of the start are to be processed, but they are already in the first path
                continue;
            }

            while (currentPath.size() != 0 && currentPath.getLast() != currentParent) {
                currentPath.removeLast();
            }

            boolean endIsNeighbor = currentVertex.getNeighbors().contains(this.end);
            if (endIsNeighbor) {
                currentPath.add(currentVertex);
                distinctPaths.add(new ArrayList<>(currentPath));
                marked.addAll(currentPath);
                currentPath.clear();
                stack.setSize(--initialStartingVerticesCount); // clear the stack (except the first added ones, the neighbors of the start)
            } else {
                boolean hasExplorableNeighbors = currentVertex.getNeighbors().stream().anyMatch(v -> v != null && !v.isWall() && !v.isStart() && !currentPath.contains(v) && !marked.contains(v));
                if (!hasExplorableNeighbors) {
                    continue;
                }
                currentPath.add(currentVertex);
                for (Vertex neighbor : currentVertex.getNeighborsShuffled()) {
                    if (neighbor != null && !neighbor.isWall() && !neighbor.isStart() && !currentPath.contains(neighbor) && !marked.contains(neighbor)) {
                        stack.push(new Pair<Vertex, Vertex>(currentVertex, neighbor));
                    }
                }
            }
        }
        return distinctPaths;
    }
//Cherche une combinaison de mots du dictionnaire qui s'adaptent à un chemin spécifique du graphe, en fonction de la longueur du chemin
    public ArrayList<String> getFittingWords(ArrayList<String> dictionary, int pathLength) {
        for (int i = 0; i < dictionary.size(); i++) {
            String word = dictionary.remove(i);
            pathLength -= word.length();
            if (pathLength == 0) {
                ArrayList<String> result = new ArrayList<>();
                result.add(word);
                return result;
            } else if (pathLength < 0) {
                dictionary.add(i, word);
                pathLength += word.length();
                continue;
            } else if (pathLength > 0) {
                ArrayList<String> result = this.getFittingWords(dictionary, pathLength);
                if (result == null) {
                    dictionary.add(i, word);
                    pathLength += word.length();
                } else {
                    result.add(0, word);
                    return result;
                }
            }
        }
        return null; // no combination of words found for the current branch
    }
//Associe les mots trouvés à des chemins dans le graphe, en s'assurant que les mots choisis conviennent à la longueur du chemin.
    public ArrayList<Pair<ArrayList<Vertex>, ArrayList<String>>> associateWordsToPaths(ArrayList<ArrayList<Vertex>> emptyDistinctPaths) throws NoWordsCombinationException {
        ArrayList<Pair<ArrayList<Vertex>, ArrayList<String>>> result = new ArrayList<>();
        ArrayList<String> tempDictionary = new ArrayList<>(this.dictionary); // copy the dictionary to avoid modifying the original one
        for (ArrayList<Vertex> path : emptyDistinctPaths) {
            ArrayList<String> fittingWords = this.getFittingWords(tempDictionary, path.size());
            if (fittingWords == null)
                throw new NoWordsCombinationException("no combination of words with length " + path.size() + " found!");
            result.add(new Pair<>(path, fittingWords));
        }
        return result;
    }
    //Recherche tous les chemins valides qui vont du point de départ au point d'arrivée, en respectant les règles du jeu.
    public ArrayList<ArrayList<Vertex>> getAllValidPaths() {
        ArrayList<ArrayList<Vertex>> paths = new ArrayList<>();
        ArrayList<Vertex> currentPath = new ArrayList<>();
        Stack<Triplet<Vertex, Integer, String>> stack = new Stack<>(); // the first vertex is the current, the second is the depth, i am storing the depth to correctly backtrack the path, the third is the word collected so far (current vertex's label is included)

        for (Vertex neighbor : this.start.getNeighbors()) {
            if (neighbor != null && !neighbor.isWall() && !neighbor.isEnd()) {
                stack.push(new Triplet<>(neighbor, 0, String.valueOf(neighbor.getLabel())));
            }
        }

        while (!stack.isEmpty()) {
            Triplet<Vertex, Integer, String> currentTriplet = stack.pop();
            Vertex currentVertex = currentTriplet.getFirstValue();
            Integer currentDepth = currentTriplet.getSecondValue();
            String currentWord = currentTriplet.getThirdValue();

            currentPath.subList(currentDepth, currentPath.size()).clear();

            if (!this.doesPrefixExist(currentWord)) continue;
            else if (this.dictionary.contains(currentWord)) currentWord = "";

            boolean endIsNeighbor = currentVertex.getNeighbors().contains(this.end);
            if (endIsNeighbor && currentWord.isEmpty()) {
                currentPath.add(currentVertex);
                paths.add(new ArrayList<>(currentPath));
            }
            boolean hasExplorableNeighbors = currentVertex.getNeighbors().stream().anyMatch(v -> v != null && !v.isWall() && !v.isStart() && !v.isEnd() && !currentPath.contains(v));
            if (!hasExplorableNeighbors) continue;

            currentPath.add(currentVertex);
            for (Vertex neighbor : currentVertex.getNeighbors()) {
                if (neighbor != null && !neighbor.isWall() && !neighbor.isStart() && !neighbor.isEnd() && !currentPath.contains(neighbor)) {
                    stack.push(new Triplet<Vertex, Integer, String>(neighbor, currentDepth + 1, currentWord + neighbor.getLabel()));
                }
            }
        }
        return paths;
    }
//Vérifie si le point d'arrivée est accessible depuis un certain sommet avec un mot donné.
    public boolean endIsReachable(Vertex from, String collectedWord) {
        ArrayList<Vertex> currentPath = new ArrayList<>();
        Stack<Triplet<Vertex, Integer, String>> stack = new Stack<>(); // the first vertex is the current, the second is the depth, i am storing the depth to correctly backtrack the path, the third is the word collected so far (current vertex's label is included)

        stack.push(new Triplet<>(from, 0, collectedWord));
        boolean isReachable = false;

        while (!stack.isEmpty() && !isReachable) {
            Triplet<Vertex, Integer, String> currentTriplet = stack.pop();
            Vertex currentVertex = currentTriplet.getFirstValue();
            Integer currentDepth = currentTriplet.getSecondValue();
            String currentWord = currentTriplet.getThirdValue();

            currentPath.subList(currentDepth, currentPath.size()).clear();

            if (!this.doesPrefixExist(currentWord)) continue;
            else if (this.dictionary.contains(currentWord)) currentWord = "";

            boolean endIsNeighbor = currentVertex.getNeighbors().contains(this.end);
            if (endIsNeighbor && currentWord.isEmpty()) isReachable = true;

            boolean hasExplorableNeighbors = currentVertex.getNeighbors().stream().anyMatch(v -> v != null && !v.isWall() && !v.isStart() && !v.isEnd() && !currentPath.contains(v));
            if (!hasExplorableNeighbors) continue;

            currentPath.add(currentVertex);
            for (Vertex neighbor : currentVertex.getNeighbors()) {
                if (neighbor != null && !neighbor.isWall() && !neighbor.isStart() && !neighbor.isEnd() && !currentPath.contains(neighbor)) {
                    stack.push(new Triplet<Vertex, Integer, String>(neighbor, currentDepth + 1, currentWord + neighbor.getLabel()));
                }
            }
        }
        return isReachable;
    }
//Vérifie si un préfixe existe parmi les mots du dictionnaire.
    public boolean doesPrefixExist(String prefix) {
        for (String word : this.dictionary) {
            if (word.startsWith(prefix))
                return true;
        }
        return false;
    }
// Calcule la longueur du chemin le plus court parmi tous les chemins trouvés.
    public int getShortestPathLength(ArrayList<ArrayList<Vertex>> allPaths) {
        if (allPaths.size() == 0) return 0;
        int result = allPaths.get(0).size();
        for (ArrayList<Vertex> path : allPaths) {
            if (path.size() < result) result = path.size();
        }
        return result;
    }
}
