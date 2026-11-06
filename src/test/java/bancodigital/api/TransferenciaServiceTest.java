package bancodigital.api;

import bancodigital.api.Exception.SaldoInsuficienteException;
import bancodigital.api.Model.Cliente;
import bancodigital.api.Model.Conta;
import bancodigital.api.Model.Transacao;
import bancodigital.api.Repository.ClienteRepository;
import bancodigital.api.Repository.ContaRepository;
import bancodigital.api.Repository.TransacaoRepository;
import bancodigital.api.Service.TransferenciaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private TransferenciaService transferenciaService;

    @Test
    void rejeitaTransferenciaComValorNuloAntesDeAcessarOsRepositorios() {
        assertThrows(IllegalArgumentException.class,
                () -> transferenciaService.realizarTransferencia(1L, 2L, null));

        verify(contaRepository, never()).findByIdWithLock(org.mockito.ArgumentMatchers.anyLong());
        verify(transacaoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejeitaTransferenciaQuandoOSaldoEInsuficiente() {
        Conta origem = conta(1L, "10.00");
        Conta destino = conta(2L, "0.00");
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(origem));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(destino));

        assertThrows(SaldoInsuficienteException.class,
                () -> transferenciaService.realizarTransferencia(1L, 2L, new BigDecimal("10.01")));

        verify(contaRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(transacaoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registraNomeDoBeneficiarioNaTransacao() {
        Conta origem = conta(1L, "50.00");
        Conta destino = conta(2L, "0.00");
        Cliente beneficiario = new Cliente();
        beneficiario.setId(102L);
        beneficiario.setNome("Maria Souza");
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(origem));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(destino));
        when(clienteRepository.findById(102L)).thenReturn(Optional.of(beneficiario));
        when(transacaoRepository.save(org.mockito.ArgumentMatchers.any(Transacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transacao transacao = transferenciaService.realizarTransferencia(
                1L, 2L, new BigDecimal("10.00")
        );

        assertEquals("Maria Souza", transacao.getBeneficiarioNome());
        assertEquals(destino.getNumeroConta(), transacao.getNumeroContaDestino());
    }

    private Conta conta(Long id, String saldo) {
        Conta conta = new Conta();
        conta.setId(id);
        conta.setClienteId(id + 100);
        conta.setNumeroConta(String.format("%010d", id));
        conta.setSaldo(new BigDecimal(saldo));
        conta.setStatus("ATIVA");
        return conta;
    }
}
