package bancodigital.api.Controller;

import bancodigital.api.DTO.EncerrarContaRequest;
import bancodigital.api.Service.EncerramentoContaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/contas")
public class ContaController {

    private final EncerramentoContaService encerramentoContaService;

    public ContaController(EncerramentoContaService encerramentoContaService) {
        this.encerramentoContaService = encerramentoContaService;
    }

    @PostMapping("/{id}/encerramento")
    public ResponseEntity<Void> encerrar(
            @PathVariable @Positive Long id,
            @RequestBody @Valid EncerrarContaRequest request
    ) {
        encerramentoContaService.encerrar(id, request.senha());
        return ResponseEntity.noContent().build();
    }
}
