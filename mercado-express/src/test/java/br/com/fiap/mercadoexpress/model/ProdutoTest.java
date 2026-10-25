package br.com.fiap.mercadoexpress.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Produto - regras do modelo")
class ProdutoTest {

    private Produto banana() {
        return Produto.builder().nome("Banana Prata").categoria("Frutas").unidade("kg")
                .precoCusto(new BigDecimal("3.20")).precoVenda(new BigDecimal("6.99")).estoque(120).build();
    }

    @Test
    @DisplayName("Deve criar produto com todos os campos preenchidos pelo builder")
    void deveCriarProdutoPeloBuilder() {
        Produto p = banana();
        assertEquals("Banana Prata", p.getNome());
        assertEquals("Frutas", p.getCategoria());
        assertEquals(new BigDecimal("6.99"), p.getPrecoVenda());
        assertEquals(120, p.getEstoque());
    }

    @Test
    @DisplayName("Preço de venda deve ser maior que o preço de custo nos produtos iniciais")
    void precoVendaMaiorQueCusto() {
        Produto p = banana();
        assertTrue(p.getPrecoVenda().compareTo(p.getPrecoCusto()) > 0);
    }

    @Test
    @DisplayName("Deve relacionar o produto ao seu fornecedor (N:1)")
    void deveRelacionarProdutoAoFornecedor() {
        Fornecedor f = Fornecedor.builder().id(1L).nome("Hortifruti Vale Verde").cnpj("11222333000181")
                .cidade("Mogi das Cruzes").build();
        Produto p = banana();
        p.setFornecedor(f);
        assertNotNull(p.getFornecedor());
        assertEquals("Hortifruti Vale Verde", p.getFornecedor().getNome());
        assertEquals(1L, p.getFornecedor().getId());
    }
}
