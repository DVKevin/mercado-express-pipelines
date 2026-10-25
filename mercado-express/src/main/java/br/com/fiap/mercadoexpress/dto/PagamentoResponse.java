package br.com.fiap.mercadoexpress.dto;

import lombok.*;

/** Resposta do Banco Tranquilo (chega na fila de sucesso ou na fila de erro). */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PagamentoResponse {
    private Long pedidoId;
    private boolean aprovado;
    private String codigoTransacao;
    private String mensagem;
    private String processadoEm;
}
