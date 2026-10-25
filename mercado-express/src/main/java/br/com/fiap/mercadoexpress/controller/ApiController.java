package br.com.fiap.mercadoexpress.controller;

import br.com.fiap.mercadoexpress.dto.CompraRequest;
import br.com.fiap.mercadoexpress.dto.PedidoDTO;
import br.com.fiap.mercadoexpress.model.Email;
import br.com.fiap.mercadoexpress.model.Pedido;
import br.com.fiap.mercadoexpress.model.Produto;
import br.com.fiap.mercadoexpress.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** API REST para testes no Postman/Insomnia (autenticação HTTP Basic). */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final ProdutoService produtoService;
    private final CompraService compraService;
    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    private final DashboardService dashboardService;
    private final EmailService emailService;

    @GetMapping("/produtos")
    public List<Produto> produtos() {
        return produtoService.listar(null);
    }

    /** Requisição de compra -> publica no RabbitMQ e responde 202 (processamento assíncrono). */
    @PostMapping("/compras")
    public ResponseEntity<?> comprar(@Valid @RequestBody CompraRequest req, Authentication auth) {
        Map<Long, Integer> itens = new LinkedHashMap<>();
        req.itens().forEach(i -> itens.merge(i.produtoId(), i.quantidade(), Integer::sum));
        try {
            Pedido p = compraService.finalizar(usuarioService.buscarPorEmail(auth.getName()), itens, req.contaBanco());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(PedidoDTO.from(pedidoService.buscar(p.getId())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    /** Consulta o status do pedido (para ver a resposta do banco depois do 202). */
    @GetMapping("/compras/{id}")
    public ResponseEntity<?> compra(@PathVariable Long id, Authentication auth) {
        Pedido p = pedidoService.buscar(id);
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !p.getUsuario().getEmail().equalsIgnoreCase(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "Pedido de outro usuário"));
        }
        return ResponseEntity.ok(PedidoDTO.from(p));
    }

    @GetMapping("/compras")
    public List<PedidoDTO> minhasCompras(Authentication auth) {
        return pedidoService.doUsuario(usuarioService.buscarPorEmail(auth.getName()))
                .stream().map(PedidoDTO::from).toList();
    }

    @GetMapping("/dashboard")
    public DashboardService.Resumo dashboard() {
        return dashboardService.resumo();
    }

    @GetMapping("/emails")
    public List<Email> emails() {
        return emailService.todos();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
    }
}
