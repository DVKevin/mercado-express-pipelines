package br.com.fiap.mercadoexpress.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CadastroForm {
    @NotBlank(message = "Informe seu nome")
    @Size(max = 100)
    private String nome;

    @NotBlank(message = "Informe seu e-mail")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "Informe uma senha")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    private String senha;

    @NotBlank(message = "Confirme a senha")
    private String confirmacaoSenha;
}
