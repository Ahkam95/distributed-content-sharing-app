package exception;

public class CommandErrorException extends Exception {
    public CommandErrorException(String message) {
        super(message);
    }
}
