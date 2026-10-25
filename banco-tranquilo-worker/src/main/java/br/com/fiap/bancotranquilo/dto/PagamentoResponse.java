package br.com.fiap.bancotranquilo.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PagamentoResponse {
    private Long pedidoId;
    private boolean aprovado;
    private String codigoTransacao;
    private String mensagem;
    private String processadoEm;
}
