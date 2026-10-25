package br.com.fiap.mercadoexpress.service;

import br.com.fiap.mercadoexpress.dto.PagamentoResponse;
import br.com.fiap.mercadoexpress.model.*;
import br.com.fiap.mercadoexpress.repository.PedidoRepository;
import br.com.fiap.mercadoexpress.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Regras de pedido: cria com reserva de estoque, aprova ou recusa conforme a resposta do banco. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    /**
     * Cria o pedido AGUARDANDO_PAGAMENTO e já reserva (baixa) o estoque.
     * Se o banco recusar, o estoque é devolvido em {@link #recusar}.
     */
    @Transactional
    public Pedido criarPedido(Usuario usuario, Map<Long, Integer> itens, String contaBanco) {
        if (itens == null || itens.isEmpty()) throw new IllegalArgumentException("Carrinho vazio");

        Pedido pedido = Pedido.builder().usuario(usuario).status(StatusPedido.AGUARDANDO_PAGAMENTO)
                .contaBanco(contaBanco.trim()).build();
        BigDecimal total = BigDecimal.ZERO, custo = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> e : itens.entrySet()) {
            Produto produto = produtoRepository.findById(e.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Produto " + e.getKey() + " não existe"));
            int qtd = e.getValue();
            if (qtd <= 0) continue;
            if (produto.getEstoque() < qtd) {
                throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome()
                        + " (disponível: " + produto.getEstoque() + ")");
            }
            produto.setEstoque(produto.getEstoque() - qtd);
            pedido.adicionarItem(ItemPedido.builder().produto(produto).quantidade(qtd)
                    .precoUnitario(produto.getPrecoVenda()).custoUnitario(produto.getPrecoCusto()).build());
            total = total.add(produto.getPrecoVenda().multiply(BigDecimal.valueOf(qtd)));
            custo = custo.add(produto.getPrecoCusto().multiply(BigDecimal.valueOf(qtd)));
        }
        if (pedido.getItens().isEmpty()) throw new IllegalArgumentException("Carrinho vazio");

        pedido.setValorTotal(total);
        pedido.setCustoTotal(custo);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Optional<Pedido> aprovar(PagamentoResponse resp) {
        return buscarAguardando(resp.getPedidoId()).map(p -> {
            p.setStatus(StatusPedido.APROVADO);
            p.setCodigoTransacao(resp.getCodigoTransacao());
            p.setMensagemBanco(resp.getMensagem());
            return pedidoRepository.save(p);
        });
    }

    @Transactional
    public Optional<Pedido> recusar(Long pedidoId, String codigo, String mensagem, StatusPedido status) {
        return buscarAguardando(pedidoId).map(p -> {
            devolverEstoque(p);
            p.setStatus(status);
            p.setCodigoTransacao(codigo);
            p.setMensagemBanco(mensagem);
            return pedidoRepository.save(p);
        });
    }

    private Optional<Pedido> buscarAguardando(Long pedidoId) {
        if (pedidoId == null) return Optional.empty();
        Optional<Pedido> opt = pedidoRepository.findById(pedidoId);
        if (opt.isEmpty()) {
            log.warn("Resposta para pedido {} inexistente — ignorada", pedidoId);
            return Optional.empty();
        }
        if (opt.get().getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            log.warn("Pedido {} já está {} — resposta duplicada ignorada", pedidoId, opt.get().getStatus());
            return Optional.empty();
        }
        return opt;
    }

    private void devolverEstoque(Pedido p) {
        for (ItemPedido item : p.getItens()) {
            Produto prod = item.getProduto();
            prod.setEstoque(prod.getEstoque() + item.getQuantidade());
            produtoRepository.save(prod);
        }
    }

    public List<Pedido> doUsuario(Usuario u) { return pedidoRepository.findByUsuarioOrderByCriadoEmDesc(u); }

    public List<Pedido> todos() { return pedidoRepository.findAllByOrderByCriadoEmDesc(); }

    public Pedido buscar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido " + id + " não encontrado"));
    }
}
