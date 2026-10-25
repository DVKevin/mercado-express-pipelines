package br.com.fiap.mercadoexpress.service;

import br.com.fiap.mercadoexpress.model.Fornecedor;
import br.com.fiap.mercadoexpress.repository.FornecedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    public List<Fornecedor> listar() {
        return fornecedorRepository.findAllByOrderByNomeAsc();
    }

    public Fornecedor buscar(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fornecedor " + id + " não encontrado"));
    }

    /** CNPJ é único: no cadastro não pode existir; na edição não pode pertencer a outro fornecedor. */
    public boolean cnpjDuplicado(Fornecedor fornecedor) {
        if (fornecedor.getCnpj() == null) return false;
        return fornecedor.getId() == null
                ? fornecedorRepository.existsByCnpj(fornecedor.getCnpj())
                : fornecedorRepository.existsByCnpjAndIdNot(fornecedor.getCnpj(), fornecedor.getId());
    }

    public Fornecedor salvar(Fornecedor fornecedor) {
        if (cnpjDuplicado(fornecedor)) {
            throw new IllegalArgumentException("Já existe um fornecedor com o CNPJ " + fornecedor.getCnpj());
        }
        return fornecedorRepository.save(fornecedor);
    }

    public void excluir(Long id) {
        fornecedorRepository.deleteById(id);
        fornecedorRepository.flush(); // força o DELETE agora para capturar erro de FK no controller
    }
}
