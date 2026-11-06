package bancodigital.api.Controller;

import bancodigital.api.DTO.TransferenciaRequest;
import bancodigital.api.DTO.TransferenciaResponse;
import bancodigital.api.Service.TransferenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transacoes")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @PostMapping("/transferencia")
    public ResponseEntity<TransferenciaResponse> transferir(
            @RequestBody @Valid TransferenciaRequest dto) {
        var transacao = transferenciaService.realizarTransferencia(
                dto.getContaOrigemId(),
                dto.getContaDestinoId(),
                dto.getValor()
        );
        var resposta = new TransferenciaResponse(
                transacao.getId(),
                transacao.getContaOrigemId(),
                transacao.getContaDestinoId(),
                transacao.getBeneficiarioNome(),
                transacao.getNumeroContaDestino(),
                transacao.getValor(),
                "CONCLUIDA",
                transacao.getCriadoEm()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }
}