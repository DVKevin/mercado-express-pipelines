package br.com.fiap.mercadoexpress.service;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/** Carrinho guardado na sessão do usuário: produtoId -> quantidade. */
@Component
@SessionScope
public class Carrinho implements Serializable {

    private final Map<Long, Integer> itens = new LinkedHashMap<>();

    public void adicionar(Long produtoId, int quantidade) {
        if (quantidade <= 0) return;
        itens.merge(produtoId, quantidade, Integer::sum);
    }

    public void atualizar(Long produtoId, int quantidade) {
        if (quantidade <= 0) itens.remove(produtoId);
        else itens.put(produtoId, quantidade);
    }

    public void remover(Long produtoId) { itens.remove(produtoId); }

    public void limpar() { itens.clear(); }

    public Map<Long, Integer> getItens() { return new LinkedHashMap<>(itens); }

    public int getQuantidadeTotal() { return itens.values().stream().mapToInt(Integer::intValue).sum(); }

    public boolean isVazio() { return itens.isEmpty(); }
}
