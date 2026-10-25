package br.com.fiap.mercadoexpress.controller;

import br.com.fiap.mercadoexpress.service.DashboardService;
import br.com.fiap.mercadoexpress.service.EmailService;
import br.com.fiap.mercadoexpress.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Área da gerência (ROLE_ADMIN). */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;
    private final PedidoService pedidoService;
    private final EmailService emailService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("r", dashboardService.resumo());
        model.addAttribute("ultimosPedidos", pedidoService.todos().stream().limit(10).toList());
        return "admin/dashboard";
    }

    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        model.addAttribute("pedidos", pedidoService.todos());
        return "admin/pedidos";
    }

    @GetMapping("/emails")
    public String emails(Model model) {
        model.addAttribute("emails", emailService.todos());
        return "admin/emails";
    }
}
