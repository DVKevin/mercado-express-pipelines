package br.com.fiap.mercadoexpress.controller;

import br.com.fiap.mercadoexpress.dto.CadastroForm;
import br.com.fiap.mercadoexpress.service.ProdutoService;
import br.com.fiap.mercadoexpress.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UsuarioService usuarioService;
    private final ProdutoService produtoService;

    /** Landing page: index.html */
    @GetMapping({"/", "/index", "/index.html"})
    public String index(Model model) {
        model.addAttribute("destaques", produtoService.listar(null).stream().limit(4).toList());
        return "index";
    }

    /** Tela de login própria (substitui a padrão do Spring Security). */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** Tela de Sign Up. */
    @GetMapping("/cadastro")
    public String cadastro(@RequestParam(required = false) String email, Model model) {
        CadastroForm form = new CadastroForm();
        form.setEmail(email);
        model.addAttribute("form", form);
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("form") CadastroForm form, BindingResult result,
                            RedirectAttributes ra) {
        if (!result.hasFieldErrors("senha") && !form.getSenha().equals(form.getConfirmacaoSenha())) {
            result.rejectValue("confirmacaoSenha", "senha.diferente", "As senhas não conferem");
        }
        if (!result.hasFieldErrors("email") && usuarioService.emailJaCadastrado(form.getEmail())) {
            result.rejectValue("email", "email.duplicado", "Este e-mail já está cadastrado");
        }
        if (result.hasErrors()) return "cadastro";

        usuarioService.cadastrar(form);
        ra.addFlashAttribute("sucesso", "Cadastro realizado! Faça login para continuar.");
        return "redirect:/login";
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "acesso-negado";
    }
}
