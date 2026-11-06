package bancodigital.api.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Pattern(regexp = "\\d{11}") String cpf,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Pattern(regexp = "\\d{10,11}") String telefone,
        @NotBlank @Size(min = 8, max = 100) String senha,
        @NotNull @Valid ContaRequest conta
) {
    public record ContaRequest(
            @NotBlank @Pattern(regexp = "\\d{1,4}") String agencia,
            @NotBlank @Pattern(regexp = "corrente|poupanca") String tipo,
            @Size(max = 120) String chavePix
    ) {
    }
}
