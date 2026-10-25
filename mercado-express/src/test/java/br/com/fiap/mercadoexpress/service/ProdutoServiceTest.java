package br.com.fiap.mercadoexpress.service;

import br.com.fiap.mercadoexpress.model.Produto;
import br.com.fiap.mercadoexpress.repository.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoService - CRUD de produtos")
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produto(Long id, String nome) {
        return Produto.builder().id(id).nome(nome).categoria("Frutas").unidade("kg")
                .precoCusto(new BigDecimal("2.00")).precoVenda(new BigDecimal("4.00")).estoque(10).build();
    }

    @Test
    @DisplayName("CREATE - deve salvar um novo produto")
    void deveSalvarProduto() {
        Produto novo = produto(null, "Kiwi");
        when(produtoRepository.save(novo)).thenReturn(produto(1L, "Kiwi"));

        Produto salvo = produtoService.salvar(novo);

        assertEquals(1L, salvo.getId());
        verify(produtoRepository).save(novo);
    }

    @Test
    @DisplayName("READ - deve listar todos ordenados por nome quando não há busca")
    void deveListarTodos() {
        when(produtoRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(produto(1L, "Banana"), produto(2L, "Maçã")));

        List<Produto> lista = produtoService.listar(null);

        assertEquals(2, lista.size());
        verify(produtoRepository, never()).findByNomeContainingIgnoreCaseOrderByNomeAsc(anyString());
    }

    @Test
    @DisplayName("READ - deve filtrar pelo nome quando há busca")
    void deveBuscarPorNome() {
        when(produtoRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc("ban")).thenReturn(List.of(produto(1L, "Banana")));

        List<Produto> lista = produtoService.listar("  ban  ");

        assertEquals(1, lista.size());
        assertEquals("Banana", lista.get(0).getNome());
    }

    @Test
    @DisplayName("READ - deve lançar erro ao buscar produto inexistente")
    void deveFalharAoBuscarInexistente() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> produtoService.buscar(99L));
        assertTrue(e.getMessage().contains("99"));
    }

    @Test
    @DisplayName("UPDATE - deve atualizar o preço de um produto existente")
    void deveAtualizarProduto() {
        Produto existente = produto(1L, "Banana");
        existente.setPrecoVenda(new BigDecimal("5.50"));
        when(produtoRepository.save(existente)).thenReturn(existente);

        Produto atualizado = produtoService.salvar(existente);

        assertEquals(new BigDecimal("5.50"), atualizado.getPrecoVenda());
    }

    @Test
    @DisplayName("DELETE - deve excluir o produto e forçar o flush")
    void deveExcluirProduto() {
        produtoService.excluir(1L);

        verify(produtoRepository).deleteById(1L);
        verify(produtoRepository).flush();
    }
}
