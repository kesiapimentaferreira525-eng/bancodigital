package bancodigital.api.Exception;

public class ContaInativaException extends RuntimeException {

    public ContaInativaException(String mensagem) {
        super(mensagem);
    }
}
