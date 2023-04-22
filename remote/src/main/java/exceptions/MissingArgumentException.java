package exceptions;

public class MissingArgumentException extends Exception {
    public MissingArgumentException(String errorMessage) {
        super(errorMessage);
    }
}
