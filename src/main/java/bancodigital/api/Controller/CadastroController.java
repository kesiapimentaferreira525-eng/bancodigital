package bancodigital.api.Controller;

import bancodigital.api.DTO.CadastroRequest;
import bancodigital.api.DTO.CadastroResponse;
import bancodigital.api.Service.CadastroClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
public class CadastroController {

    private final CadastroClienteService cadastroClienteService;

    public CadastroController(CadastroClienteService cadastroClienteService) {
        this.cadastroClienteService = cadastroClienteService;
    }

    @PostMapping
    public ResponseEntity<CadastroResponse> cadastrar(@RequestBody @Valid CadastroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastroClienteService.cadastrar(request));
    }
}
