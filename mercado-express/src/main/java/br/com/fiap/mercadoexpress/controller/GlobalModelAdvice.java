package br.com.fiap.mercadoexpress.controller;

import br.com.fiap.mercadoexpress.service.Carrinho;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Disponibiliza a quantidade do carrinho para o menu de todas as páginas. */
@ControllerAdvice(assignableTypes = {HomeController.class, ProdutoController.class,
        CarrinhoController.class, PedidoController.class, AdminController.class, FornecedorController.class})
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final Carrinho carrinho;

    @ModelAttribute("qtdCarrinho")
    public int qtdCarrinho() {
        return carrinho.getQuantidadeTotal();
    }
}
