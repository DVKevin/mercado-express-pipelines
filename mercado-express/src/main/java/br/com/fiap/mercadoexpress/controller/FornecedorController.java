package br.com.fiap.mercadoexpress.controller;

import br.com.fiap.mercadoexpress.model.Fornecedor;
import br.com.fiap.mercadoexpress.service.FornecedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** CRUD de fornecedores (somente ADMIN — ver SecurityConfig). */
@Controller
@RequestMapping("/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService fornecedorService;

    // READ
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("fornecedores", fornecedorService.listar());
        return "fornecedores/lista";
    }

    // CREATE (form)
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("fornecedor", new Fornecedor());
        return "fornecedores/form";
    }

    // UPDATE (form)
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("fornecedor", fornecedorService.buscar(id));
        return "fornecedores/form";
    }

    // CREATE / UPDATE (submit) — se vier id é update
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("fornecedor") Fornecedor fornecedor, BindingResult result,
                         RedirectAttributes ra) {
        if (!result.hasFieldErrors("cnpj") && fornecedorService.cnpjDuplicado(fornecedor)) {
            result.rejectValue("cnpj", "cnpj.duplicado", "Já existe um fornecedor com este CNPJ");
        }
        if (result.hasErrors()) return "fornecedores/form";
        boolean novo = fornecedor.getId() == null;
        fornecedorService.salvar(fornecedor);
        ra.addFlashAttribute("sucesso", novo ? "Fornecedor cadastrado!" : "Fornecedor atualizado!");
        return "redirect:/fornecedores";
    }

    // DELETE
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try {
            fornecedorService.excluir(id);
            ra.addFlashAttribute("sucesso", "Fornecedor excluído!");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("erro", "Este fornecedor possui produtos vinculados e não pode ser excluído. "
                    + "Troque o fornecedor desses produtos antes.");
        }
        return "redirect:/fornecedores";
    }
}
