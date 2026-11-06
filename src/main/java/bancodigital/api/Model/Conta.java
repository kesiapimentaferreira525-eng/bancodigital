package bancodigital.api.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "contas")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @NotBlank
    @Column(name = "numero_conta", unique = true, nullable = false)
    private String numeroConta;

    @NotBlank
    @Column(nullable = false, length = 4)
    private String agencia;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(name = "chave_pix", length = 120)
    private String chavePix;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false)
    private BigDecimal saldo = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "limite_conta", nullable = false)
    private BigDecimal limiteConta = BigDecimal.ZERO;

    @NotBlank
    @Column(nullable = false)
    private String status = "ATIVA";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getNumeroConta() { return numeroConta; }
    public void setNumeroConta(String numeroConta) { this.numeroConta = numeroConta; }
    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public BigDecimal getLimiteConta() { return limiteConta; }
    public void setLimiteConta(BigDecimal limiteConta) { this.limiteConta = limiteConta; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}