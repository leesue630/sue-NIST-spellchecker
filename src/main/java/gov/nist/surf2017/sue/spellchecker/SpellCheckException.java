package gov.nist.surf2017.sue.spellchecker;

public class SpellCheckException extends RuntimeException {

    public SpellCheckException() {
    }

    public SpellCheckException(String message) {
        super(message);
    }

    public SpellCheckException(Throwable cause) {
        super(cause);
    }

    public SpellCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}