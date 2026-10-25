package br.com.fiap.mercadoexpress.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Carrinho - regras da sessão de compra")
class CarrinhoTest {

    private Carrinho carrinho;

    @BeforeEach
    void setUp() {
        carrinho = new Carrinho();
    }

    @Test
    @DisplayName("Deve somar quantidades do mesmo produto")
    void deveSomarQuantidades() {
        carrinho.adicionar(1L, 2);
        carrinho.adicionar(1L, 3);
        assertEquals(5, carrinho.getItens().get(1L));
        assertEquals(5, carrinho.getQuantidadeTotal());
    }

    @Test
    @DisplayName("Deve ignorar quantidade zero ou negativa")
    void deveIgnorarQuantidadeInvalida() {
        carrinho.adicionar(1L, 0);
        carrinho.adicionar(2L, -1);
        assertTrue(carrinho.isVazio());
    }

    @Test
    @DisplayName("Atualizar para zero deve remover o item")
    void atualizarParaZeroRemove() {
        carrinho.adicionar(1L, 2);
        carrinho.atualizar(1L, 0);
        assertFalse(carrinho.getItens().containsKey(1L));
    }

    @Test
    @DisplayName("Limpar deve esvaziar o carrinho")
    void limparEsvazia() {
        carrinho.adicionar(1L, 2);
        carrinho.adicionar(2L, 1);
        carrinho.limpar();
        assertTrue(carrinho.isVazio());
        assertEquals(0, carrinho.getQuantidadeTotal());
    }
}
