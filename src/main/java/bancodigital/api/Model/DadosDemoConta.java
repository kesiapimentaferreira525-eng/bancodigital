package bancodigital.api.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "dados_demo_conta")
public class DadosDemoConta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "conta_id", nullable = false, unique = true)
    private Long contaId;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "limite_credito", nullable = false)
    private BigDecimal limiteCredito;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "fatura_atual", nullable = false)
    private BigDecimal faturaAtual;

    @Size(max = 4)
    @Column(name = "cartao_final", nullable = false, length = 4)
    private String cartaoFinal;

    @Size(max = 120)
    @Column(name = "chave_pix_demo", nullable = false, length = 120)
    private String chavePixDemo;

    @Column(name = "dados_ficticios", nullable = false)
    private boolean dadosFicticios = true;

    public Long getId() { return id; }
    public Long getContaId() { return contaId; }
    public void setContaId(Long contaId) { this.contaId = contaId; }
    public BigDecimal getLimiteCredito() { return limiteCredito; }
    public void setLimiteCredito(BigDecimal limiteCredito) { this.limiteCredito = limiteCredito; }
    public BigDecimal getFaturaAtual() { return faturaAtual; }
    public void setFaturaAtual(BigDecimal faturaAtual) { this.faturaAtual = faturaAtual; }
    public String getCartaoFinal() { return cartaoFinal; }
    public void setCartaoFinal(String cartaoFinal) { this.cartaoFinal = cartaoFinal; }
    public String getChavePixDemo() { return chavePixDemo; }
    public void setChavePixDemo(String chavePixDemo) { this.chavePixDemo = chavePixDemo; }
    public boolean isDadosFicticios() { return dadosFicticios; }
}
