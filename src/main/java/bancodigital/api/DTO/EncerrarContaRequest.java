package bancodigital.api.DTO;

import jakarta.validation.constraints.NotBlank;

public record EncerrarContaRequest(@NotBlank String senha) {
}
