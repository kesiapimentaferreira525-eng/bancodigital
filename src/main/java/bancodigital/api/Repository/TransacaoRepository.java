package bancodigital.api.Repository;

import bancodigital.api.Model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    List<Transacao> findByContaOrigemIdOrContaDestinoId(Long contaOrigemId, Long contaDestinoId);
}