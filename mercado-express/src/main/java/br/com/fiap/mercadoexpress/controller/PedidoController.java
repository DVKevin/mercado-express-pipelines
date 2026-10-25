package br.com.fiap.mercadoexpress.controller;

import br.com.fiap.mercadoexpress.model.Pedido;
import br.com.fiap.mercadoexpress.model.Usuario;
import br.com.fiap.mercadoexpress.service.PedidoService;
import br.com.fiap.mercadoexpress.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String meusPedidos(Authentication auth, Model model) {
        Usuario u = usuarioService.buscarPorEmail(auth.getName());
        model.addAttribute("pedidos", pedidoService.doUsuario(u));
        return "pedidos/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Authentication auth, Model model) {
        Pedido p = pedidoService.buscar(id);
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !p.getUsuario().getEmail().equalsIgnoreCase(auth.getName())) {
            throw new AccessDeniedException("Pedido de outro usuário");
        }
        model.addAttribute("pedido", p);
        return "pedidos/detalhe";
    }
}
