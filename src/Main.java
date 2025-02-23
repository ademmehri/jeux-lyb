package src;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {
    // Codes ANSI pour les couleurs et le nettoyage de l'écran
    public static final String ANSI_CLS    = "\033[H\033[2J";
    public static final String ANSI_RESET  = "\033[0m";
    public static final String ANSI_GREEN  = "\033[32m";
    public static final String ANSI_YELLOW = "\033[33m";
    public static final String ANSI_CYAN   = "\033[36m";

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        // Affichage de l'écran d'accueil (Splash Screen)
        displaySplashScreen();

        // Affichage des règles/instructions
        displayInstructions();

        // Menu interactif de sélection des niveaux
        int niveauChoisi = displayLevelMenu();

        // Lancement du jeu selon le niveau sélectionné
        clearConsole();
        int consoleWidth = 80;
        String lancement = "Lancement du jeu en niveau " + niveauChoisi + "...";
        System.out.println("\n".repeat(5) + centerText(ANSI_GREEN + lancement + ANSI_RESET, consoleWidth));
        Thread.sleep(2000);
        // Ici, tu lanceras la boucle principale du jeu
        Game.Difficulty selectedDifficulty;
        switch (niveauChoisi) {
            case 1:
                selectedDifficulty = Game.Difficulty.EASY;
                break;
            case 2:
                selectedDifficulty = Game.Difficulty.MEDIUM;
                break;
            case 3:
                selectedDifficulty = Game.Difficulty.HARD;
                break;
            default:
                selectedDifficulty = Game.Difficulty.MEDIUM;
                break;
        }

        Game game = new Game("footballers.txt", selectedDifficulty);
        game.start();
    }

    // Méthode pour centrer du texte par rapport à une largeur donnée
    public static String centerText(String text, int consoleWidth) {
        int padding = (consoleWidth - stripAnsi(text).length()) / 2;
        if (padding < 0) padding = 0;
        return " ".repeat(padding) + text;
    }

    // Méthode utilitaire pour enlever les codes ANSI (utile pour le calcul du centrage)
    public static String stripAnsi(String text) {
        return text.replaceAll("\033\\[[;\\d]*m", "");
    }

    // Méthode pour effacer la console
    public static void clearConsole() {
        System.out.print(ANSI_CLS);
        System.out.flush();
    }

    // Affichage de l'écran d'accueil (Splash Screen)
    public static void displaySplashScreen() throws IOException {
        clearConsole();
        int consoleWidth = 170;
        String titre = ANSI_GREEN + " lABYRINTHE DE MOTS" + ANSI_RESET;
        System.out.println("\n".repeat(5) + centerText(titre, consoleWidth) + "\n");
        System.out.println(centerText(ANSI_YELLOW + "Appuyez sur une touche pour continuer..." + ANSI_RESET, consoleWidth));
        System.in.read();
        flushInput();
    }

    // Affichage des instructions/règles du jeu
    public static void displayInstructions() throws IOException {
        clearConsole();
        int consoleWidth = 170;
        System.out.println(centerText(ANSI_CYAN + "=== RÈGLES ET INSTRUCTIONS ===" + ANSI_RESET, consoleWidth));
        System.out.println();
        System.out.println(centerText("Utilisez les touches fléchées (ou W/S pour naviguer) pour changer d'option.", consoleWidth));
        System.out.println(centerText("Appuyez sur 'Entrée' pour valider votre choix.", consoleWidth));
        System.out.println(centerText("Le but du jeu est de progresser dans un labyrinthe.", consoleWidth));
        System.out.println(centerText("Chaque écran se valide par une touche.", consoleWidth));
        System.out.println();
        System.out.println(centerText(ANSI_YELLOW + "Appuyez sur une touche pour continuer..." + ANSI_RESET, consoleWidth));
        System.in.read();
        flushInput();
    }

    // Menu de sélection des niveaux avec navigation via W (haut) et S (bas)
    public static int displayLevelMenu() throws IOException {
        String[] niveaux = {"Facile", "Moyen", "Difficile"};
        int selection = 0;
        boolean selectionDone = false;
        int consoleWidth = 170;

        while (!selectionDone) {
            clearConsole();
            System.out.println(centerText(ANSI_CYAN + "=== SÉLECTIONNEZ LE NIVEAU ===" + ANSI_RESET, consoleWidth));
            System.out.println();
            for (int i = 0; i < niveaux.length; i++) {
                String ligne;
                if (i == selection) {
                    ligne = ANSI_GREEN + "> " + niveaux[i] + ANSI_RESET;
                } else {
                    ligne = "  " + niveaux[i];
                }
                System.out.println(centerText(ligne, consoleWidth));
            }
            System.out.println();
            System.out.println(centerText("Utilisez 'W' (haut) et 'S' (bas) pour naviguer, puis appuyez sur 'Entrée' pour sélectionner.", consoleWidth));

            int key = System.in.read();
            if (key == '\n' || key == '\r') {
                selectionDone = true;
            } else if (key == 'w' || key == 'W') {
                selection = (selection - 1 + niveaux.length) % niveaux.length;
            } else if (key == 's' || key == 'S') {
                selection = (selection + 1) % niveaux.length;
            }
            flushInput();
        }
        return selection + 1; // 1 pour Facile, 2 pour Moyen, 3 pour Difficile
    }

    // Vider le flux d'entrée pour éviter les caractères en trop
    public static void flushInput() throws IOException {
        while (System.in.available() > 0) {
            System.in.read();
        }
    }
}
