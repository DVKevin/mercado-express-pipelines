package br.com.fiap.mercadoexpress.service;

import br.com.fiap.mercadoexpress.model.Produto;
import br.com.fiap.mercadoexpress.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public List<Produto> listar(String busca) {
        if (busca == null || busca.isBlank()) return produtoRepository.findAllByOrderByNomeAsc();
        return produtoRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(busca.trim());
    }

    public Produto buscar(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto " + id + " não encontrado"));
    }

    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

    public void excluir(Long id) {
        produtoRepository.deleteById(id);
        produtoRepository.flush(); // força o DELETE agora para capturar erro de FK no controller
    }
}
