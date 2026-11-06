package bancodigital.api.DTO;

import java.math.BigDecimal;

public record CadastroResponse(Usuario usuario, Conta conta, Demonstracao demonstracao) {
    public record Usuario(Long id, String nome) {
    }

    public record Conta(
            Long id,
            String agencia,
            String numero,
            String tipo,
            BigDecimal saldo,
            BigDecimal limiteConta
    ) {
    }

    public record Demonstracao(
            BigDecimal limiteCredito,
            BigDecimal faturaAtual,
            String cartaoFinal,
            String chavePix,
            boolean dadosFicticios
    ) {
    }
}
