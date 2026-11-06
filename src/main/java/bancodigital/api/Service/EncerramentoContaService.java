package bancodigital.api.Service;

import bancodigital.api.Exception.ClienteNaoEncontradoException;
import bancodigital.api.Exception.ContaInativaException;
import bancodigital.api.Exception.ContaNaoEncontradaException;
import bancodigital.api.Exception.CredenciaisInvalidasException;
import bancodigital.api.Model.Conta;
import bancodigital.api.Repository.ClienteRepository;
import bancodigital.api.Repository.ContaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EncerramentoContaService {

    private final ContaRepository contaRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public EncerramentoContaService(
            ContaRepository contaRepository,
            ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.contaRepository = contaRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void encerrar(Long contaId, String senha) {
        Conta conta = contaRepository.findByIdWithLock(contaId)
                .orElseThrow(() -> new ContaNaoEncontradaException(contaId, "informada"));

        if (!"ATIVA".equals(conta.getStatus())) {
            throw new ContaInativaException("Esta conta já está encerrada.");
        }

        var cliente = clienteRepository.findById(conta.getClienteId())
                .orElseThrow(() -> new ClienteNaoEncontradoException(conta.getClienteId()));
        if (!passwordEncoder.matches(senha, cliente.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        conta.setStatus("INATIVA");
        contaRepository.save(conta);
    }
}
