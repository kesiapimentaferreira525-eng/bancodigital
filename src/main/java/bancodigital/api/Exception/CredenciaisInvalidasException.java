package bancodigital.api.Exception;

public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("A senha informada não confere.");
    }
}
