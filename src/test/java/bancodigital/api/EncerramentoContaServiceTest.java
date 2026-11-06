package bancodigital.api;

import bancodigital.api.Exception.CredenciaisInvalidasException;
import bancodigital.api.Model.Cliente;
import bancodigital.api.Model.Conta;
import bancodigital.api.Repository.ClienteRepository;
import bancodigital.api.Repository.ContaRepository;
import bancodigital.api.Service.EncerramentoContaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncerramentoContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EncerramentoContaService encerramentoContaService;

    @Test
    void encerraContaMantendoSaldoERegistro() {
        Conta conta = conta();
        Cliente cliente = new Cliente();
        cliente.setId(12L);
        cliente.setSenhaHash("hash");
        when(contaRepository.findByIdWithLock(3L)).thenReturn(Optional.of(conta));
        when(clienteRepository.findById(12L)).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("senha-correta", "hash")).thenReturn(true);

        encerramentoContaService.encerrar(3L, "senha-correta");

        assertEquals("INATIVA", conta.getStatus());
        assertEquals(new BigDecimal("25.00"), conta.getSaldo());
        verify(contaRepository).save(conta);
    }

    @Test
    void naoEncerraContaQuandoSenhaNaoConfere() {
        Conta conta = conta();
        Cliente cliente = new Cliente();
        cliente.setId(12L);
        cliente.setSenhaHash("hash");
        when(contaRepository.findByIdWithLock(3L)).thenReturn(Optional.of(conta));
        when(clienteRepository.findById(12L)).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("senha-errada", "hash")).thenReturn(false);

        assertThrows(CredenciaisInvalidasException.class,
                () -> encerramentoContaService.encerrar(3L, "senha-errada"));

        assertEquals("ATIVA", conta.getStatus());
        verify(contaRepository, never()).save(conta);
    }

    private Conta conta() {
        Conta conta = new Conta();
        conta.setId(3L);
        conta.setClienteId(12L);
        conta.setSaldo(new BigDecimal("25.00"));
        conta.setStatus("ATIVA");
        return conta;
    }
}
