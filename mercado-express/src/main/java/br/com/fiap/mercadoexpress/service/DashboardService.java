package br.com.fiap.mercadoexpress.service;

import br.com.fiap.mercadoexpress.model.*;
import br.com.fiap.mercadoexpress.repository.PedidoRepository;
import br.com.fiap.mercadoexpress.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Indicadores da gerência: só pedidos APROVADOS entram em vendas e lucro. */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public record Resumo(
            BigDecimal faturamento, BigDecimal lucro, long pedidosAprovados, long pedidosRecusados,
            long pedidosAguardando, BigDecimal ticketMedio, int itensEmEstoque, long produtosEstoqueBaixo,
            List<String> diasLabels, List<BigDecimal> vendasPorDia, List<BigDecimal> lucroPorDia,
            List<String> produtosLabels, List<BigDecimal> receitaPorProduto, List<BigDecimal> lucroPorProduto,
            List<String> estoqueLabels, List<Integer> estoqueValores,
            List<String> statusLabels, List<Long> statusValores) {}

    public Resumo resumo() {
        List<Pedido> todos = pedidoRepository.findAll();
        List<Pedido> aprovados = todos.stream().filter(p -> p.getStatus() == StatusPedido.APROVADO).toList();

        BigDecimal faturamento = soma(aprovados.stream().map(Pedido::getValorTotal).toList());
        BigDecimal lucro = soma(aprovados.stream().map(Pedido::getLucro).toList());
        BigDecimal ticket = aprovados.isEmpty() ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(aprovados.size()), 2, RoundingMode.HALF_UP);

        // ---- vendas e lucro por dia (últimos 14 dias) ----
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        LinkedHashMap<LocalDate, BigDecimal[]> porDia = new LinkedHashMap<>();
        LocalDate hoje = LocalDate.now();
        for (int i = 13; i >= 0; i--) porDia.put(hoje.minusDays(i), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        for (Pedido p : aprovados) {
            BigDecimal[] v = porDia.get(p.getCriadoEm().toLocalDate());
            if (v != null) { v[0] = v[0].add(p.getValorTotal()); v[1] = v[1].add(p.getLucro()); }
        }
        List<String> dias = porDia.keySet().stream().map(fmt::format).toList();
        List<BigDecimal> vendasDia = porDia.values().stream().map(v -> v[0]).toList();
        List<BigDecimal> lucroDia = porDia.values().stream().map(v -> v[1]).toList();

        // ---- receita e lucro por produto ----
        Map<String, BigDecimal[]> porProduto = new TreeMap<>();
        for (Pedido p : aprovados) {
            for (ItemPedido i : p.getItens()) {
                BigDecimal q = BigDecimal.valueOf(i.getQuantidade());
                BigDecimal receita = i.getPrecoUnitario().multiply(q);
                BigDecimal luc = i.getPrecoUnitario().subtract(i.getCustoUnitario()).multiply(q);
                porProduto.merge(i.getProduto().getNome(), new BigDecimal[]{receita, luc},
                        (a, b) -> new BigDecimal[]{a[0].add(b[0]), a[1].add(b[1])});
            }
        }

        // ---- estoque ----
        List<Produto> produtos = produtoRepository.findAllByOrderByNomeAsc();
        int totalEstoque = produtos.stream().mapToInt(Produto::getEstoque).sum();
        long baixo = produtos.stream().filter(p -> p.getEstoque() < 20).count();

        // ---- status ----
        List<String> statusLabels = new ArrayList<>();
        List<Long> statusValores = new ArrayList<>();
        for (StatusPedido s : StatusPedido.values()) {
            statusLabels.add(s.name());
            statusValores.add(todos.stream().filter(p -> p.getStatus() == s).count());
        }

        return new Resumo(faturamento, lucro, aprovados.size(),
                contar(todos, StatusPedido.RECUSADO), contar(todos, StatusPedido.AGUARDANDO_PAGAMENTO), ticket,
                totalEstoque, baixo,
                dias, vendasDia, lucroDia,
                new ArrayList<>(porProduto.keySet()),
                porProduto.values().stream().map(v -> v[0]).toList(),
                porProduto.values().stream().map(v -> v[1]).toList(),
                produtos.stream().map(Produto::getNome).toList(),
                produtos.stream().map(Produto::getEstoque).toList(),
                statusLabels, statusValores);
    }

    private long contar(List<Pedido> l, StatusPedido s) { return l.stream().filter(p -> p.getStatus() == s).count(); }

    private BigDecimal soma(List<BigDecimal> valores) {
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
