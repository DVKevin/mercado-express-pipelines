package br.com.fiap.mercadoexpress.controller;

import br.com.fiap.mercadoexpress.model.Pedido;
import br.com.fiap.mercadoexpress.model.Produto;
import br.com.fiap.mercadoexpress.model.StatusPedido;
import br.com.fiap.mercadoexpress.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/carrinho")
@RequiredArgsConstructor
public class CarrinhoController {

    private final Carrinho carrinho;
    private final ProdutoService produtoService;
    private final UsuarioService usuarioService;
    private final CompraService compraService;

    public record Linha(Produto produto, int quantidade, BigDecimal subtotal) {}

    @GetMapping
    public String ver(Model model) {
        List<Linha> linhas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> e : carrinho.getItens().entrySet()) {
            try {
                Produto p = produtoService.buscar(e.getKey());
                BigDecimal sub = p.getPrecoVenda().multiply(BigDecimal.valueOf(e.getValue()));
                linhas.add(new Linha(p, e.getValue(), sub));
                total = total.add(sub);
            } catch (IllegalArgumentException ex) {
                carrinho.remover(e.getKey()); // produto foi excluído
            }
        }
        model.addAttribute("linhas", linhas);
        model.addAttribute("total", total);
        return "carrinho/carrinho";
    }

    @PostMapping("/adicionar")
    public String adicionar(@RequestParam Long produtoId, @RequestParam(defaultValue = "1") int quantidade,
                            RedirectAttributes ra) {
        Produto p = produtoService.buscar(produtoId);
        carrinho.adicionar(produtoId, quantidade);
        ra.addFlashAttribute("sucesso", p.getNome() + " adicionado ao carrinho");
        return "redirect:/produtos";
    }

    @PostMapping("/atualizar")
    public String atualizar(@RequestParam Long produtoId, @RequestParam int quantidade) {
        carrinho.atualizar(produtoId, quantidade);
        return "redirect:/carrinho";
    }

    @PostMapping("/remover")
    public String remover(@RequestParam Long produtoId) {
        carrinho.remover(produtoId);
        return "redirect:/carrinho";
    }

    /** Finaliza a compra: cria o pedido e dispara a integração com o Banco Tranquilo via RabbitMQ. */
    @PostMapping("/finalizar")
    public String finalizar(@RequestParam String contaBanco, Authentication auth, RedirectAttributes ra) {
        if (carrinho.isVazio()) {
            ra.addFlashAttribute("erro", "Seu carrinho está vazio");
            return "redirect:/carrinho";
        }
        if (contaBanco == null || contaBanco.isBlank()) {
            ra.addFlashAttribute("erro", "Informe sua conta do Banco Tranquilo");
            return "redirect:/carrinho";
        }
        try {
            Pedido pedido = compraService.finalizar(usuarioService.buscarPorEmail(auth.getName()),
                    carrinho.getItens(), contaBanco);
            carrinho.limpar();
            if (pedido.getStatus() == StatusPedido.ERRO_ENVIO) {
                ra.addFlashAttribute("erro", "Não foi possível falar com o Banco Tranquilo agora. Tente novamente.");
            } else {
                ra.addFlashAttribute("sucesso", "Pedido #" + pedido.getId()
                        + " enviado ao Banco Tranquilo. Aguardando confirmação do pagamento...");
            }
            return "redirect:/pedidos/" + pedido.getId();
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/carrinho";
        }
    }
}
