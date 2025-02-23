package src;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import src.Exceptions.InvalidMoveException;

public class Game {
    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    private boolean isRunning;
    private Game.Difficulty difficulty;
    private Labyrinth map;
    private Player player;

    // in game
    private String collectedWord;
    private String message;

    // getters and setters
    public boolean isRunning() { return this.isRunning; }
    public void setRunning(boolean isRunning) { this.isRunning = isRunning; }

    public Game.Difficulty getDifficulty() { return this.difficulty; }
    public void setDifficulty(Game.Difficulty difficulty) { this.difficulty = difficulty; }

    public Labyrinth getMap() { return this.map; }
    public void setMap(Labyrinth map) { this.map = map; }

    public Player getPlayer() { return this.player; }
    public void setPlayer(Player player) { this.player = player; }



    public Game(String filePath) throws IOException, InterruptedException, ExecutionException {
        this(filePath, Game.Difficulty.EASY);
    }

    public Game(String filePath, Game.Difficulty difficulty) throws IOException, InterruptedException, ExecutionException {
        this.isRunning = false;
        this.difficulty = difficulty;
        this.map = new Labyrinth(filePath, difficulty);
        this.player = new Player(this.map.getStart(), Style.BG_RED, Style.ST_BOLD);

        this.collectedWord = "";
        this.message = "";
    }
    // Permet le lancement du jeu
    public void start() throws IOException, InterruptedException {
        // Indique que le jeu est en cours
        this.isRunning = true;
        // Crée un scanner pour lire l'entrée de l'utilisateur
        Scanner scanner = new Scanner(System.in);
        // Récupère tous les chemins valides du labyrinthe
        ArrayList<ArrayList<Vertex>> allPaths = this.map.getAllValidPaths();

        // Récupère la longueur du plus court chemin
        int shortestPathLength = this.map.getShortestPathLength(allPaths);
        // Initialise le compteur d'étapes
        int steps = 0;

        // Tant que le jeu est en cours, la boucle se poursuit
        while (this.isRunning) {
            // Rendu graphique du jeu (par exemple, afficher la carte et les informations)
            this.render();

            // Lecture de la commande du joueur
            String moves = scanner.nextLine().trim().toLowerCase();

            // Si le joueur demande de l'aide
            if (moves.startsWith("help")) {
                // Affiche l'aide et continue la boucle
                this.help();
                continue;
            }

            // Boucle pour traiter chaque caractère du mouvement
            while (!moves.isEmpty()) {
                // Récupère la direction du premier caractère de la commande
                char direction = moves.charAt(0);
                // Sauvegarde la position actuelle du joueur avant de bouger
                Vertex oldPosition = this.player.getPosition();

                try {
                    // Essaie de déplacer le joueur dans la direction donnée
                    this.player.move(direction);
                } catch (InvalidMoveException e) {
                    // Si le mouvement est invalide, pénalise le joueur de 5 points par lettre du mouvement invalide
                    this.player.decreaseScore(moves.length() * 5);
                    // Affiche un message d'erreur pour le mouvement invalide
                    this.message = "invalid move!" + e.getMessage();
                    break;  // Sort de la boucle dès qu'une erreur se produit
                }

                // Vérifie si le joueur a atteint la fin
                if (this.player.getPosition() == this.map.getEnd()) {
                    // Le jeu est terminé, le joueur a gagné
                    this.isRunning = false;

                    // Si le joueur a trouvé le plus court chemin
                    if (steps == shortestPathLength) {
                        // Donne un bonus de 200 points
                        this.message = "Congratulations! you found the shortest path! , here is a 200 points bonus!";
                        this.player.increaseScore(200);
                    }

                    // Affiche le message de fin, avec le score du joueur
                    this.message = this.player.getScore() < 0 ? "You lost the game! , your score is: " + this.player.getScore() : "Congratulations! you won the game!, your score is: " + this.player.getScore();
                    break;  // Sort de la boucle du jeu
                }

                // Ajoute la lettre de la case actuelle à `collectedWord`
                this.collectedWord += this.player.getPosition().getLabel();

                // Vérifie si la fin est atteignable avec le mot collecté
                if (this.map.endIsReachable(this.player.getPosition(), this.collectedWord)) {
                    steps++;  // Augmente le compteur d'étapes

                    // Si le mot formé est valide et existe dans le dictionnaire
                    if (this.map.getDictionary().contains(this.collectedWord)) {
                        // Félicite le joueur et augmente son score
                        this.message = "Congratulations!, you found the word: `" + this.collectedWord + "`! keep it up ";
                        this.player.increaseScore(this.collectedWord.length() * 10);
                        // Réinitialise le mot collecté
                        this.collectedWord = "";
                    } else {
                        // Si le mot n'est pas valide, encourage le joueur à continuer
                        this.message = "Keep going! (≧▽≦)";
                    }
                } else {
                    // Si le mot collecté mène à une impasse, vérifie si un préfixe valide existe
                    if (this.map.doesPrefixExist(this.collectedWord)) {
                        // Si un préfixe existe, pénalise le joueur de 5 points
                        this.player.decreaseScore(5);
                        this.message = "that path leads to a dead end! ";
                    } else {
                        // Si aucun préfixe valide n'existe, pénalise le joueur de 10 points
                        this.player.decreaseScore(10);
                        this.message = "No word starts with: `" + this.collectedWord + "` try again please";
                    }
                    // Remet la position du joueur à son ancienne position
                    this.player.setPosition(oldPosition);
                    // Supprime la dernière lettre du mot collecté
                    this.collectedWord = this.collectedWord.substring(0, this.collectedWord.length() - 1);
                }

                // Ajoute un style visuel à la position actuelle du joueur
                this.player.getPosition().addStyles(Style.BG_CYAN);

                // Rafraîchit l'affichage du jeu
                this.render();

                // Supprime le premier caractère de la commande pour traiter le mouvement suivant
                moves = moves.substring(1);
            }
        }

        // Ferme le scanner une fois le jeu terminé
        scanner.close();
    }

//lors le utilisareur fait demande de help on affichier les chemin possible pour une periode de temps
    public void help() throws IOException, InterruptedException {
        int helpLevel;
        if      (this.difficulty == Game.Difficulty.EASY)   helpLevel = 3;
        else if (this.difficulty == Game.Difficulty.MEDIUM) helpLevel = 2;
        else                                                helpLevel = 1;

        this.player.decreaseScore(30);
        
        for (int i = 0; i < this.map.getDistinctPaths().size(); i++) {
            for (Vertex v : this.map.getDistinctPaths().get(i).getKey()) {
                v.addStyles("\u001B[4" + (1 + i) + "m");
            }
            this.render();
            Thread.sleep(500 * helpLevel);
            for (Vertex v : this.map.getDistinctPaths().get(i).getKey()) {
                v.removeStyles("\u001B[4" + (1 + i) + "m");
            }
        }
    }
    public void render() {
        /* level: MEDIUM/HARD
          ┌───┐  ┌───┐  ┌───┐
          │ A ├──┤ B ├──┤ D │
          └─┬─┘  └─┬─┘  └───┘
            │   ╳  │   ╱
          ┌─┴─┐  ┌─┴─┐
          │ C ├──┤ E │
          └───┘  └───┘
         */
        /* level: EASY
          ┌─────┐    ┌─────┐    ┌─────┐
          │     │    │     │    │     │
          │  A  ├────┤  B  ├────┤  D  │
          └──┬──┘    └──┬──┘    └─────┘
             │    ╲╱    │
             │    ╱╲    │
          ┌──┴──┐    ┌──┴──┐
          │     │    │     │
          │  C  ├────┤  E  │
          └─────┘    └─────┘
         */
        Style.clearScreen();
        char[][] matrix = this.map.getGraph().toMatrix();
        String horizontalLine = "\u2500"; // ─
        String verticalLine   = "\u2502"; // │

        String topLeftCorner     = "\u250c"; // ┌
        String topRightCorner    = "\u2510"; // ┐
        String bottomLeftCorner  = "\u2514"; // └
        String bottomRightCorner = "\u2518"; // ┘

        String middleLeft   = "\u251c"; // ├
        String middleRight  = "\u2524"; // ┤
        String middleTop    = "\u252c"; // ┬
        String middleBottom = "\u2534"; // ┴

        String slash     = "\u2571"; // ╱
        String backSlash = "\u2572"; // ╲
        String cross     = "\u2573"; // ╳

        String space = " ";

        int scalar = this.map.getScalar();

        for (int y = 0; y < matrix.length; y++) {
            for (int x = 0; x < matrix[y].length; x++) {
                if (matrix[y][x] == '\0') {
                    System.out.print(space); // for the top left corner
                    System.out.print(space.repeat(scalar)); // for the horizontal line
                    System.out.print(space); // for the middle bottom line
                    System.out.print(space.repeat(scalar)); // for the horizontal line
                    System.out.print(space); // for the top right corner
                } else {
                    Vertex currentVertex = this.map.getGraph().getVertexAt(x, y);
                    Style.applyStyle(currentVertex == this.player.getPosition() ? this.player.getStyle() : currentVertex.getStyle());
                    System.out.print(topLeftCorner);
                    System.out.print(horizontalLine.repeat(scalar));
                    if (y != 0 && matrix[y - 1][x] != '\0') {
                        System.out.print(middleBottom);
                    } else {
                        System.out.print(horizontalLine);
                    }
                    System.out.print(horizontalLine.repeat(scalar));
                    System.out.print(topRightCorner);
                }
                Style.resetStyle();
                System.out.print(space.repeat(2).repeat(scalar));
            }
            System.out.println();

            for (int i = 0; i < scalar; i++) {
                for (int x = 0; x < matrix[y].length; x++) {
                    if (matrix[y][x] == '\0') {
                        System.out.print(space); // for the middle right / vertical line
                        System.out.print(space.repeat(scalar)); // for the space
                        System.out.print(space); // for the label
                        System.out.print(space.repeat(scalar)); // for the space
                        System.out.print(space); // for the middle left / vertical line
                    } else {
                        Vertex currentVertex = this.map.getGraph().getVertexAt(x, y);
                        Style.applyStyle(currentVertex == this.player.getPosition() ? this.player.getStyle() : currentVertex.getStyle());

                        if (x != 0 && matrix[y][x - 1] != '\0' && i == scalar / 2) {
                            System.out.print(middleRight);
                        } else {
                            System.out.print(verticalLine);
                        }
                        System.out.print(space.repeat(scalar));
                        if (i == scalar / 2) {
                            System.out.print(matrix[y][x]);
                        } else {
                            System.out.print(space);
                        }
                        System.out.print(space.repeat(scalar));
                        if (x != matrix[y].length - 1 && matrix[y][x + 1] != '\0' && i == scalar / 2) {
                            System.out.print(middleLeft);
                        } else {
                            System.out.print(verticalLine);
                        }
                    }
                    Style.resetStyle();
                    if (matrix[y][x] != '\0' && x != matrix[y].length - 1 && matrix[y][x + 1] != '\0' && i == scalar / 2) {
                        System.out.print(horizontalLine.repeat(2).repeat(scalar));
                    } else {
                        System.out.print(space.repeat(2).repeat(scalar));
                    }
                }
                System.out.println();
            }

            for (int x = 0; x < matrix[y].length; x++) {
                if (matrix[y][x] == '\0') {
                    System.out.print(space); // for the bottom left corner
                    System.out.print(space.repeat(scalar)); // for the horizontal line
                    System.out.print(space); // for the middle top / horizontal line
                    System.out.print(space.repeat(scalar)); // for the horizontal line
                    System.out.print(space); // for the bottom right corner
                } else {
                    Vertex currentVertex = this.map.getGraph().getVertexAt(x, y);
                    Style.applyStyle(currentVertex == this.player.getPosition() ? this.player.getStyle() : currentVertex.getStyle());

                    System.out.print(bottomLeftCorner);
                    System.out.print(horizontalLine.repeat(scalar));
                    if (y != matrix.length - 1 && matrix[y + 1][x] != '\0') {
                        System.out.print(middleTop);
                    } else {
                        System.out.print(horizontalLine);
                    }
                    System.out.print(horizontalLine.repeat(scalar));
                    System.out.print(bottomRightCorner);
                }
                Style.resetStyle();
                System.out.print(space.repeat(2).repeat(scalar));
            }
            System.out.println();

        /* level: MEDIUM/HARD
          ┌───┐  ┌───┐  ┌───┐
          │ A ├──┤ B ├──┤ D │
          └─┬─┘  └─┬─┘  └───┘
            │   ╳  │   ╱
          ┌─┴─┐  ┌─┴─┐
          │ C ├──┤ E │
          └───┘  └───┘
         */
        /* level: EASY
          ┌─────┐    ┌─────┐    ┌─────┐
          │     │    │     │    │     │
          │  A  ├────┤  B  ├────┤  D  │
          └──┬──┘    └──┬──┘    └─────┘
             │    ╲╱    │
             │    ╱╲    │
          ┌──┴──┐    ┌──┴──┐
          │     │    │     │
          │  C  ├────┤  E  │
          └─────┘    └─────┘
         */

            for (int i = 0; i < scalar; i++) {
                for (int x = 0; x < matrix[y].length; x++) {
                    System.out.print(space); // for the left corner
                    System.out.print(space.repeat(scalar)); // for the horizontal line(s)
                    if (matrix[y][x] != '\0' && y != matrix.length - 1 && matrix[y + 1][x] != '\0') {
                        System.out.print(verticalLine);
                    } else {
                        System.out.print(space);
                    }
                    System.out.print(space.repeat(scalar)); // for the horizontal line(s)
                    System.out.print(space); // for the right corner
                    System.out.print(space);

                    int crosses = 0b0000; // this variable is used to determine the type of cross to print
                    if (matrix[y][x] != '\0') crosses |= 0b0001;
                    if (y != matrix.length - 1 && matrix[y + 1][x] != '\0') crosses |= 0b0010;
                    if (x != matrix[y].length - 1 && matrix[y][x + 1] != '\0') crosses |= 0b0100;
                    if (matrix[y][x] != '\0' && y != matrix.length - 1 && matrix[y + 1][x] != '\0' && x != matrix[y].length - 1 && matrix[y][x + 1] != '\0') crosses |= 0b1000;
                    if (scalar == 1) { // hardh code this
                        if (crosses == 0b1111) System.out.print(cross);
                        else if (crosses == 0b1001 || crosses == 0b1011 || crosses == 0b1101) System.out.print(backSlash);
                        else if (crosses == 0b0110 || crosses == 0b0111 || crosses == 0b1110) System.out.print(slash);
                        else System.out.print(space);
                    } else if (scalar == 2) {
                        if (i == 0) {
                            if (crosses == 0b1111 || crosses == 0b1001 || crosses == 0b1011 || crosses == 0b1101) System.out.print(backSlash);
                            else System.out.print(space);
                        } else if (i == 1) {
                            if (crosses == 0b1111 || crosses == 0b0110 || crosses == 0b0111 || crosses == 0b1110) System.out.print(slash);
                            else System.out.print(space);
                        }
                        if (i == 0) {
                            if (crosses == 0b1111 || crosses == 0b0110 || crosses == 0b0111 || crosses == 0b1110) System.out.print(slash);
                            else System.out.print(space);
                        } else if (i == 1) {
                            if (crosses == 0b1111 || crosses == 0b1001 || crosses == 0b1011 || crosses == 0b1101) System.out.print(backSlash);
                            else System.out.print(space);
                        }
                        System.out.print(space);
                    }
                }
                System.out.println();
            }
        }

        // print the player's score and the collected word
        System.out.print("Score: " + this.player.getScore());
        System.out.print("        collected word: " + this.collectedWord);
        System.out.println("        message: " + this.message);
        System.out.print("enter your move(s): ");
    }
    public void displayWelcomeScreen() {
        // Efface l'écran (selon la plateforme, ici on utilise une méthode custom)
        Style.clearScreen();

        // Affichage du titre
        System.out.println("****************************************");
        System.out.println("*           MON JEU CONSOLE            *");
        System.out.println("****************************************");
        System.out.println();

        // Instructions de jeu
        System.out.println("Comment jouer :");
        System.out.println(" - Utilisez les flèches directionnelles pour vous déplacer.");
        System.out.println(" - Appuyez sur 'Espace' pour interagir.");
        System.out.println();

        // Description du concept et du niveau de jeu
        System.out.println("Concept du jeu :");
        System.out.println(" Une aventure captivante dans un labyrinthe à explorer.");
        System.out.println();
        System.out.println("Niveau de difficulté : Moyen");
        System.out.println();

        // Invitation à démarrer
        System.out.println("Appuyez sur 'Entrée' pour commencer...");

        // Attente de l'entrée utilisateur
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
