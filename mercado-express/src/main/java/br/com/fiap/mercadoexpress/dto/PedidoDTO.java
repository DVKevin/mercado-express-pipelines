package br.com.fiap.mercadoexpress.dto;

import br.com.fiap.mercadoexpress.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoDTO(Long id, String cliente, String email, String status, BigDecimal valorTotal,
                        String codigoTransacao, String mensagemBanco, LocalDateTime criadoEm,
                        List<ItemDTO> itens) {

    public record ItemDTO(Long produtoId, String produto, Integer quantidade, BigDecimal precoUnitario) {}

    public static PedidoDTO from(Pedido p) {
        return new PedidoDTO(p.getId(), p.getUsuario().getNome(), p.getUsuario().getEmail(),
                p.getStatus().name(), p.getValorTotal(), p.getCodigoTransacao(), p.getMensagemBanco(),
                p.getCriadoEm(),
                p.getItens().stream()
                        .map(i -> new ItemDTO(i.getProduto().getId(), i.getProduto().getNome(),
                                i.getQuantidade(), i.getPrecoUnitario()))
                        .toList());
    }
}
