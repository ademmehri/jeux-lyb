package src.Exceptions;

public class NoWordsCombinationException extends Exception {
    public NoWordsCombinationException() {
        super("No words combination found.");
    }
    public NoWordsCombinationException(String message) {
        super(message);
    }
}
