package br.com.fiap.mercadoexpress.repository;

import br.com.fiap.mercadoexpress.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
    List<Produto> findAllByOrderByNomeAsc();
}
