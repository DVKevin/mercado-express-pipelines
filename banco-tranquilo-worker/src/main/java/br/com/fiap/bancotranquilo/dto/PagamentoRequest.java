package br.com.fiap.bancotranquilo.dto;

import lombok.*;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PagamentoRequest {
    private Long pedidoId;
    private String clienteNome;
    private String clienteEmail;
    private String contaBanco;
    private BigDecimal valor;
    private String dataHora;
}
