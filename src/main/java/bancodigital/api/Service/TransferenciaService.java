package bancodigital.api.Service;

import bancodigital.api.Exception.ContaInativaException;
import bancodigital.api.Exception.ContaNaoEncontradaException;
import bancodigital.api.Exception.ClienteNaoEncontradoException;
import bancodigital.api.Exception.SaldoInsuficienteException;
import bancodigital.api.Model.Cliente;
import bancodigital.api.Model.Conta;
import bancodigital.api.Model.Transacao;
import bancodigital.api.Repository.ClienteRepository;
import bancodigital.api.Repository.ContaRepository;
import bancodigital.api.Repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransferenciaService {

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final ClienteRepository clienteRepository;

    public TransferenciaService(
            ContaRepository contaRepository,
            TransacaoRepository transacaoRepository,
            ClienteRepository clienteRepository
    ) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Transacao realizarTransferencia(Long contaOrigemId, Long contaDestinoId, BigDecimal valor) {
        if (contaOrigemId == null || contaDestinoId == null) {
            throw new IllegalArgumentException("As contas de origem e destino são obrigatórias.");
        }
        if (valor == null) {
            throw new IllegalArgumentException("O valor da transferência é obrigatório.");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser maior que zero.");
        }
        if (contaOrigemId.equals(contaDestinoId)) {
            throw new IllegalArgumentException("Não é permitido transferir para a mesma conta.");
        }

        // Lock em ordem determinística para evitar deadlocks em transferências simultâneas.
        Long primeiroId = Math.min(contaOrigemId, contaDestinoId);
        Long segundoId = Math.max(contaOrigemId, contaDestinoId);
        Conta primeira = carregarContaComLock(
                primeiroId,
                primeiroId.equals(contaOrigemId) ? "origem" : "destino"
        );
        Conta segunda = carregarContaComLock(
                segundoId,
                segundoId.equals(contaOrigemId) ? "origem" : "destino"
        );
        Conta origem = contaOrigemId.equals(primeiroId) ? primeira : segunda;
        Conta destino = contaDestinoId.equals(primeiroId) ? primeira : segunda;

        if (!"ATIVA".equals(origem.getStatus())) {
            throw new ContaInativaException("A conta de origem está inativa.");
        }

        if (origem.getSaldo().compareTo(valor) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente para realizar a transferência.");
        }

        if (!"ATIVA".equals(destino.getStatus())) {
            throw new ContaInativaException("A conta de destino está inativa.");
        }

        Cliente beneficiario = clienteRepository.findById(destino.getClienteId())
                .orElseThrow(() -> new ClienteNaoEncontradoException(destino.getClienteId()));

        // Efetua as atualizações de saldo
        origem.setSaldo(origem.getSaldo().subtract(valor));
        destino.setSaldo(destino.getSaldo().add(valor));

        contaRepository.save(origem);
        contaRepository.save(destino);

        // Registra a transação no ledger
        Transacao transacao = new Transacao();
        transacao.setContaOrigemId(contaOrigemId);
        transacao.setContaDestinoId(contaDestinoId);
        transacao.setBeneficiarioNome(beneficiario.getNome());
        transacao.setNumeroContaDestino(destino.getNumeroConta());
        transacao.setTipo("TRANSFERENCIA");
        transacao.setValor(valor);

        return transacaoRepository.save(transacao);
    }

    private Conta carregarContaComLock(Long id, String tipoConta) {
        return contaRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ContaNaoEncontradaException(id, tipoConta));
    }
}