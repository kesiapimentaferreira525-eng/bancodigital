package bancodigital.api.Exception;

public class CadastroDuplicadoException extends RuntimeException {

    public CadastroDuplicadoException() {
        super("Já existe um cadastro com os dados informados.");
    }
}
