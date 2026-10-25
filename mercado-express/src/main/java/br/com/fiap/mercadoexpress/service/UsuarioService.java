package br.com.fiap.mercadoexpress.service;

import br.com.fiap.mercadoexpress.dto.CadastroForm;
import br.com.fiap.mercadoexpress.model.Role;
import br.com.fiap.mercadoexpress.model.Usuario;
import br.com.fiap.mercadoexpress.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean emailJaCadastrado(String email) {
        return usuarioRepository.existsByEmailIgnoreCase(email.trim());
    }

    /** Todo cadastro público entra como CLIENTE; ADMIN só pelo DataLoader/BD. */
    public Usuario cadastrar(CadastroForm form) {
        Usuario u = Usuario.builder()
                .nome(form.getNome().trim())
                .email(form.getEmail().trim().toLowerCase())
                .senha(passwordEncoder.encode(form.getSenha()))
                .role(Role.CLIENTE)
                .build();
        return usuarioRepository.save(u);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }
}
