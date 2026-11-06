package bancodigital.api.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferenciaResponse {

    private Long transacaoId;
    private Long contaOrigemId;
    private Long contaDestinoId;
    private String beneficiarioNome;
    private String numeroContaDestino;
    private BigDecimal valor;
    private String status;
    private LocalDateTime dataHora;

    public TransferenciaResponse(Long transacaoId, Long contaOrigemId, Long contaDestinoId,
                                 String beneficiarioNome, String numeroContaDestino, BigDecimal valor,
                                 String status, LocalDateTime dataHora) {
        this.transacaoId = transacaoId;
        this.contaOrigemId = contaOrigemId;
        this.contaDestinoId = contaDestinoId;
        this.beneficiarioNome = beneficiarioNome;
        this.numeroContaDestino = numeroContaDestino;
        this.valor = valor;
        this.status = status;
        this.dataHora = dataHora;
    }

    // Getters
    public Long getTransacaoId() { return transacaoId; }
    public Long getContaOrigemId() { return contaOrigemId; }
    public Long getContaDestinoId() { return contaDestinoId; }
    public String getBeneficiarioNome() { return beneficiarioNome; }
    public String getNumeroContaDestino() { return numeroContaDestino; }
    public BigDecimal getValor() { return valor; }
    public String getStatus() { return status; }
    public LocalDateTime getDataHora() { return dataHora; }
}