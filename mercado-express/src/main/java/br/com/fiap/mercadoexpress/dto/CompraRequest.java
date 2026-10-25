package br.com.fiap.mercadoexpress.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/** Corpo do POST /api/compras (Postman/Insomnia). */
public record CompraRequest(
        @NotBlank(message = "Informe a conta do Banco Tranquilo") String contaBanco,
        @NotEmpty(message = "Informe ao menos um item") List<@Valid Item> itens) {

    public record Item(
            @NotNull Long produtoId,
            @NotNull @Min(1) Integer quantidade) {}
}
