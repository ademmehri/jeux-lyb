package src;

import java.util.ArrayList;

public abstract class Style {
    public static final String RESET = "\u001B[0m";
    // foreground colors
    public static final String FG_BLACK =   "\u001B[30m";
    public static final String FG_RED =     "\u001B[31m";
    public static final String FG_GREEN =   "\u001B[32m";
    public static final String FG_YELLOW =  "\u001B[33m";
    public static final String FG_BLUE =    "\u001B[34m";
    public static final String FG_MAGENTA = "\u001B[35m";
    public static final String FG_CYAN =    "\u001B[36m";
    public static final String FG_WHITE =   "\u001B[37m";
    // background colors
    public static final String BG_BLACK =   "\u001B[40m";
    public static final String BG_RED =     "\u001B[41m";
    public static final String BG_GREEN =   "\u001B[42m";
    public static final String BG_YELLOW =  "\u001B[43m";
    public static final String BG_BLUE =    "\u001B[44m";
    public static final String BG_MAGENTA = "\u001B[45m";
    public static final String BG_CYAN =    "\u001B[46m";
    public static final String BG_WHITE =   "\u001B[47m";
    // text styles
    public static final String ST_BOLD =      "\u001B[1m";
    public static final String ST_FAINT =     "\u001B[2m";
    public static final String ST_ITALIC =    "\u001B[3m";
    public static final String ST_UNDERLINE = "\u001B[4m";
    public static final String ST_BLINK =     "\u001B[5m";
    public static final String ST_REVERSE =   "\u001B[7m";

    public static void applyStyle(String ...styles) {
        for (String style : styles)
            System.out.print(style);
    }

    public static void applyStyle(ArrayList<String> styles) {
        if (styles == null) return;
        for (String style : styles)
            System.out.print(style);
    }

    public static void resetStyle() {
        System.out.print(Style.RESET);
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
    }
}
