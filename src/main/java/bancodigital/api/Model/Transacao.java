package bancodigital.api.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conta_origem_id")
    private Long contaOrigemId;

    @Column(name = "conta_destino_id")
    private Long contaDestinoId;

    @Column(name = "beneficiario_nome", length = 150)
    private String beneficiarioNome;

    @Column(name = "numero_conta_destino", length = 32)
    private String numeroContaDestino;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String tipo;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContaOrigemId() { return contaOrigemId; }
    public void setContaOrigemId(Long contaOrigemId) { this.contaOrigemId = contaOrigemId; }
    public Long getContaDestinoId() { return contaDestinoId; }
    public void setContaDestinoId(Long contaDestinoId) { this.contaDestinoId = contaDestinoId; }
    public String getBeneficiarioNome() { return beneficiarioNome; }
    public void setBeneficiarioNome(String beneficiarioNome) { this.beneficiarioNome = beneficiarioNome; }
    public String getNumeroContaDestino() { return numeroContaDestino; }
    public void setNumeroContaDestino(String numeroContaDestino) { this.numeroContaDestino = numeroContaDestino; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
