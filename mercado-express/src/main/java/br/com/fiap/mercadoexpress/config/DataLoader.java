package br.com.fiap.mercadoexpress.config;

import br.com.fiap.mercadoexpress.model.*;
import br.com.fiap.mercadoexpress.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/** Popula o banco na primeira execução: 2 usuários (1 por perfil), produtos e (opcional) vendas de exemplo. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final PedidoRepository pedidoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.pedidos-demo:true}")
    private boolean pedidosDemo;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            usuarioRepository.save(Usuario.builder().nome("Gerente Mercado").email("admin@mercado.com")
                    .senha(passwordEncoder.encode("admin123")).role(Role.ADMIN).build());
            usuarioRepository.save(Usuario.builder().nome("Cliente Teste").email("cliente@mercado.com")
                    .senha(passwordEncoder.encode("cliente123")).role(Role.CLIENTE).build());
            log.info("Usuários padrão criados: admin@mercado.com/admin123 e cliente@mercado.com/cliente123");
        }

        if (fornecedorRepository.count() == 0) {
            fornecedorRepository.saveAll(List.of(
                    Fornecedor.builder().nome("Hortifruti Vale Verde").cnpj("11222333000181")
                            .email("contato@valeverde.com.br").telefone("(11) 4002-8922").cidade("Mogi das Cruzes").build(),
                    Fornecedor.builder().nome("Bebidas Naturais SP").cnpj("44555666000172")
                            .email("vendas@bebidasnaturais.com.br").telefone("(11) 3456-7890").cidade("São Paulo").build()));
            log.info("Fornecedores iniciais cadastrados");
        }

        if (produtoRepository.count() == 0) {
            produtoRepository.saveAll(List.of(
                    p("Banana Prata", "Frutas", "Cacho selecionado", "3.20", "6.99", 120, "kg"),
                    p("Maçã Gala", "Frutas", "Maçã nacional tipo exportação", "5.10", "9.90", 80, "kg"),
                    p("Morango", "Frutas", "Bandeja 250 g", "4.50", "8.99", 40, "bandeja"),
                    p("Abacaxi Pérola", "Frutas", "Unidade grande", "4.00", "7.49", 35, "un"),
                    p("Uva Thompson", "Frutas", "Sem semente, 500 g", "7.80", "14.90", 30, "bandeja"),
                    p("Manga Palmer", "Frutas", "Unidade", "2.30", "4.99", 60, "un"),
                    p("Laranja Pera", "Frutas", "Ideal para suco", "2.40", "4.79", 150, "kg"),
                    p("Tomate Italiano", "Legumes", "Maduro para molho", "4.20", "8.49", 70, "kg"),
                    p("Alface Crespa", "Verduras", "Hidropônica", "1.60", "3.49", 50, "un"),
                    p("Suco Natural de Laranja", "Bebidas", "Garrafa 1 L", "6.50", "12.90", 25, "un")));
            // vincula cada produto a um fornecedor (bebidas -> Bebidas Naturais SP; resto -> Hortifruti)
            List<Fornecedor> fornecedores = fornecedorRepository.findAllByOrderByNomeAsc();
            Fornecedor bebidas = fornecedores.stream().filter(f -> f.getNome().startsWith("Bebidas")).findFirst().orElse(null);
            Fornecedor horti = fornecedores.stream().filter(f -> f.getNome().startsWith("Hortifruti")).findFirst().orElse(null);
            produtoRepository.findAll().forEach(prod -> {
                prod.setFornecedor("Bebidas".equals(prod.getCategoria()) ? bebidas : horti);
                produtoRepository.save(prod);
            });
            log.info("Produtos iniciais cadastrados");
        }

        if (pedidosDemo && pedidoRepository.count() == 0) {
            criarVendasDemo();
        }
    }

    private Produto p(String nome, String cat, String desc, String custo, String venda, int estoque, String un) {
        return Produto.builder().nome(nome).categoria(cat).descricao(desc)
                .precoCusto(new BigDecimal(custo)).precoVenda(new BigDecimal(venda))
                .estoque(estoque).unidade(un).build();
    }

    /** Vendas aprovadas nos últimos 14 dias, só para os gráficos não começarem vazios (marcadas como DEMO). */
    private void criarVendasDemo() {
        Usuario cliente = usuarioRepository.findByEmailIgnoreCase("cliente@mercado.com").orElse(null);
        List<Produto> produtos = produtoRepository.findAll();
        if (cliente == null || produtos.isEmpty()) return;

        Random rnd = new Random(42);
        for (int i = 0; i < 18; i++) {
            Pedido pedido = Pedido.builder().usuario(cliente).status(StatusPedido.APROVADO)
                    .contaBanco("00001-0").codigoTransacao("DEMO-" + (i + 1))
                    .mensagemBanco("Venda de demonstração").build();
            BigDecimal total = BigDecimal.ZERO, custo = BigDecimal.ZERO;
            int qtdItens = 1 + rnd.nextInt(3);
            for (int j = 0; j < qtdItens; j++) {
                Produto prod = produtos.get(rnd.nextInt(produtos.size()));
                int qtd = 1 + rnd.nextInt(4);
                pedido.adicionarItem(ItemPedido.builder().produto(prod).quantidade(qtd)
                        .precoUnitario(prod.getPrecoVenda()).custoUnitario(prod.getPrecoCusto()).build());
                total = total.add(prod.getPrecoVenda().multiply(BigDecimal.valueOf(qtd)));
                custo = custo.add(prod.getPrecoCusto().multiply(BigDecimal.valueOf(qtd)));
            }
            pedido.setValorTotal(total);
            pedido.setCustoTotal(custo);
            Pedido salvo = pedidoRepository.save(pedido);
            // espalha as datas nos últimos 14 dias (o @PrePersist grava "agora")
            LocalDateTime data = LocalDateTime.now().minusDays(rnd.nextInt(14)).minusHours(rnd.nextInt(10));
            salvo.setCriadoEm(data);
            pedidoRepository.save(salvo);
        }
        log.info("Vendas de demonstração criadas para o dashboard (codigoTransacao DEMO-*)");
    }
}
