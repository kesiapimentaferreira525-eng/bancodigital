package bancodigital.api.Service;

import bancodigital.api.DTO.CadastroRequest;
import bancodigital.api.DTO.CadastroResponse;
import bancodigital.api.Exception.CadastroDuplicadoException;
import bancodigital.api.Model.Cliente;
import bancodigital.api.Model.Conta;
import bancodigital.api.Model.DadosDemoConta;
import bancodigital.api.Repository.ClienteRepository;
import bancodigital.api.Repository.ContaRepository;
import bancodigital.api.Repository.DadosDemoContaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CadastroClienteService {

    private final ClienteRepository clienteRepository;
    private final ContaRepository contaRepository;
    private final DadosDemoContaRepository dadosDemoContaRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastroClienteService(
            ClienteRepository clienteRepository,
            ContaRepository contaRepository,
            DadosDemoContaRepository dadosDemoContaRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.clienteRepository = clienteRepository;
        this.contaRepository = contaRepository;
        this.dadosDemoContaRepository = dadosDemoContaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CadastroResponse cadastrar(CadastroRequest request) {
        if (clienteRepository.findByCpf(request.cpf()).isPresent()
                || clienteRepository.findByEmail(request.email()).isPresent()) {
            throw new CadastroDuplicadoException();
        }

        Cliente cliente = new Cliente();
        cliente.setNome(request.nome().trim());
        cliente.setCpf(request.cpf());
        cliente.setEmail(request.email().trim());
        cliente.setTelefone(request.telefone());
        cliente.setSenhaHash(passwordEncoder.encode(request.senha()));
        Cliente clienteSalvo = clienteRepository.save(cliente);

        Conta conta = new Conta();
        conta.setClienteId(clienteSalvo.getId());
        conta.setAgencia(request.conta().agencia());
        conta.setTipo(request.conta().tipo());
        conta.setChavePix(request.conta().chavePix());
        conta.setSaldo(valorDemo(100_000, 999_999));
        conta.setLimiteConta(valorDemo(50_000, 250_000));
        conta.setNumeroConta(UUID.randomUUID().toString().replace("-", ""));
        Conta contaSalva = contaRepository.save(conta);
        contaSalva.setNumeroConta(String.format("%010d", contaSalva.getId()));
        contaSalva = contaRepository.save(contaSalva);

        BigDecimal limiteCredito = valorDemo(300_000, 1_000_000);
        DadosDemoConta dadosDemo = new DadosDemoConta();
        dadosDemo.setContaId(contaSalva.getId());
        dadosDemo.setLimiteCredito(limiteCredito);
        dadosDemo.setFaturaAtual(valorDemo(0, limiteCredito.movePointRight(2).longValueExact()));
        dadosDemo.setCartaoFinal(String.format(
                Locale.ROOT,
                "%04d",
                ThreadLocalRandom.current().nextInt(10_000)
        ));
        dadosDemo.setChavePixDemo("demo-" + contaSalva.getId() + "@bancodigital.local");
        dadosDemoContaRepository.save(dadosDemo);

        return new CadastroResponse(
                new CadastroResponse.Usuario(clienteSalvo.getId(), clienteSalvo.getNome()),
                new CadastroResponse.Conta(
                        contaSalva.getId(),
                        contaSalva.getAgencia(),
                        contaSalva.getNumeroConta(),
                        contaSalva.getTipo(),
                        contaSalva.getSaldo(),
                        contaSalva.getLimiteConta()
                ),
                new CadastroResponse.Demonstracao(
                        dadosDemo.getLimiteCredito(),
                        dadosDemo.getFaturaAtual(),
                        dadosDemo.getCartaoFinal(),
                        dadosDemo.getChavePixDemo(),
                        dadosDemo.isDadosFicticios()
                )
        );
    }

    private BigDecimal valorDemo(long centavosMinimos, long centavosMaximos) {
        long centavos = ThreadLocalRandom.current().nextLong(centavosMinimos, centavosMaximos + 1);
        return BigDecimal.valueOf(centavos, 2);
    }
}
