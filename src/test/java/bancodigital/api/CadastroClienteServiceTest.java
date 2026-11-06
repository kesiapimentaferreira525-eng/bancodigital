package bancodigital.api;

import bancodigital.api.DTO.CadastroRequest;
import bancodigital.api.DTO.CadastroResponse;
import bancodigital.api.Exception.CadastroDuplicadoException;
import bancodigital.api.Model.Cliente;
import bancodigital.api.Model.Conta;
import bancodigital.api.Model.DadosDemoConta;
import bancodigital.api.Repository.ClienteRepository;
import bancodigital.api.Repository.ContaRepository;
import bancodigital.api.Repository.DadosDemoContaRepository;
import bancodigital.api.Service.CadastroClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastroClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private DadosDemoContaRepository dadosDemoContaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CadastroClienteService cadastroClienteService;

    @Test
    void criaClienteEContaEArmazenaSomenteSenhaCodificada() {
        CadastroRequest request = request();
        when(clienteRepository.findByCpf(request.cpf())).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.senha())).thenReturn("senha-codificada");
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(12L);
            return cliente;
        });
        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> {
            Conta conta = invocation.getArgument(0);
            if (conta.getId() == null) {
                conta.setId(34L);
            }
            return conta;
        });

        CadastroResponse resposta = cadastroClienteService.cadastrar(request);

        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(clienteCaptor.capture());
        assertEquals("senha-codificada", clienteCaptor.getValue().getSenhaHash());
        assertEquals(12L, resposta.usuario().id());
        assertEquals(34L, resposta.conta().id());
        assertEquals("0000000034", resposta.conta().numero());
        assertDentroDoIntervalo(resposta.conta().saldo(), "1000.00", "9999.99");
        assertDentroDoIntervalo(resposta.conta().limiteConta(), "500.00", "2500.00");
        ArgumentCaptor<DadosDemoConta> dadosDemoCaptor = ArgumentCaptor.forClass(DadosDemoConta.class);
        verify(dadosDemoContaRepository).save(dadosDemoCaptor.capture());
        DadosDemoConta dadosDemo = dadosDemoCaptor.getValue();
        assertEquals("demo-34@bancodigital.local", dadosDemo.getChavePixDemo());
        assertDentroDoIntervalo(resposta.demonstracao().limiteCredito(), "3000.00", "10000.00");
        assertDentroDoIntervalo(
                resposta.demonstracao().faturaAtual(),
                "0.00",
                resposta.demonstracao().limiteCredito().toPlainString()
        );
        assertTrue(resposta.demonstracao().cartaoFinal().matches("\\d{4}"));
        assertTrue(resposta.demonstracao().dadosFicticios());
        assertEquals(resposta.demonstracao().limiteCredito(), dadosDemo.getLimiteCredito());
        assertEquals(resposta.demonstracao().faturaAtual(), dadosDemo.getFaturaAtual());
        assertEquals(resposta.demonstracao().cartaoFinal(), dadosDemo.getCartaoFinal());
        verify(contaRepository, times(2)).save(any(Conta.class));
        verify(contaRepository, times(2)).save(any(Conta.class));
    }

    private void assertDentroDoIntervalo(
            java.math.BigDecimal valor,
            String minimo,
            String maximo
    ) {
        assertTrue(valor.compareTo(new java.math.BigDecimal(minimo)) >= 0);
        assertTrue(valor.compareTo(new java.math.BigDecimal(maximo)) <= 0);
    }

    @Test
    void rejeitaCpfJaCadastradoSemCriarClienteOuConta() {
        CadastroRequest request = request();
        when(clienteRepository.findByCpf(request.cpf())).thenReturn(Optional.of(new Cliente()));

        assertThrows(CadastroDuplicadoException.class, () -> cadastroClienteService.cadastrar(request));

        verify(clienteRepository, never()).save(any(Cliente.class));
        verify(contaRepository, never()).save(any(Conta.class));
    }

    private CadastroRequest request() {
        return new CadastroRequest(
                "Ana Silva",
                "12345678901",
                "ana@example.com",
                "11987654321",
                "senha-forte",
                new CadastroRequest.ContaRequest("0001", "corrente", null)
        );
    }
}
