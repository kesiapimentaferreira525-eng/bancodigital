package bancodigital.api.Exception;

public class ContaNaoEncontradaException extends RuntimeException {

    public ContaNaoEncontradaException(Long id, String tipoConta) {
        super("Conta de " + tipoConta + " não encontrada (ID: " + id + ").");
    }
}
