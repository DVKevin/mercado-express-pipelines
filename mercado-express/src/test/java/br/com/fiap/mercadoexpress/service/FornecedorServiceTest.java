package br.com.fiap.mercadoexpress.service;

import br.com.fiap.mercadoexpress.model.Fornecedor;
import br.com.fiap.mercadoexpress.repository.FornecedorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FornecedorService - CRUD de fornecedores")
class FornecedorServiceTest {

    @Mock
    private FornecedorRepository fornecedorRepository;

    @InjectMocks
    private FornecedorService fornecedorService;

    private Fornecedor fornecedor(Long id) {
        return Fornecedor.builder().id(id).nome("Hortifruti Vale Verde").cnpj("11222333000181")
                .email("contato@valeverde.com.br").cidade("Mogi das Cruzes").build();
    }

    @Test
    @DisplayName("CREATE - deve salvar fornecedor com CNPJ novo")
    void deveSalvarFornecedor() {
        Fornecedor novo = fornecedor(null);
        when(fornecedorRepository.existsByCnpj("11222333000181")).thenReturn(false);
        when(fornecedorRepository.save(novo)).thenReturn(fornecedor(1L));

        Fornecedor salvo = fornecedorService.salvar(novo);

        assertEquals(1L, salvo.getId());
    }

    @Test
    @DisplayName("CREATE - não deve permitir CNPJ duplicado")
    void naoDevePermitirCnpjDuplicado() {
        Fornecedor novo = fornecedor(null);
        when(fornecedorRepository.existsByCnpj("11222333000181")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> fornecedorService.salvar(novo));
        verify(fornecedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("READ - deve listar fornecedores ordenados por nome")
    void deveListarFornecedores() {
        when(fornecedorRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(fornecedor(1L), fornecedor(2L)));

        assertEquals(2, fornecedorService.listar().size());
    }

    @Test
    @DisplayName("READ - deve lançar erro ao buscar fornecedor inexistente")
    void deveFalharAoBuscarInexistente() {
        when(fornecedorRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> fornecedorService.buscar(50L));
    }

    @Test
    @DisplayName("UPDATE - deve permitir editar mantendo o próprio CNPJ")
    void deveAtualizarFornecedor() {
        Fornecedor existente = fornecedor(1L);
        existente.setCidade("São Paulo");
        when(fornecedorRepository.existsByCnpjAndIdNot("11222333000181", 1L)).thenReturn(false);
        when(fornecedorRepository.save(existente)).thenReturn(existente);

        Fornecedor atualizado = fornecedorService.salvar(existente);

        assertEquals("São Paulo", atualizado.getCidade());
    }

    @Test
    @DisplayName("DELETE - deve excluir o fornecedor e forçar o flush")
    void deveExcluirFornecedor() {
        fornecedorService.excluir(1L);

        verify(fornecedorRepository).deleteById(1L);
        verify(fornecedorRepository).flush();
    }
}
