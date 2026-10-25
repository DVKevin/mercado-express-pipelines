package br.com.fiap.mercadoexpress.dto;

import lombok.*;

import java.math.BigDecimal;

/** Mensagem publicada na pagamento-request-exchange e consumida pelo Banco Tranquilo. */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PagamentoRequest {
    private Long pedidoId;
    private String clienteNome;
    private String clienteEmail;
    private String contaBanco;
    private BigDecimal valor;
    private String dataHora; // ISO-8601, string para facilitar testes manuais no RabbitMQ
}
