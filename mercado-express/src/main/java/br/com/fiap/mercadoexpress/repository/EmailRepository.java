package br.com.fiap.mercadoexpress.repository;

import br.com.fiap.mercadoexpress.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailRepository extends JpaRepository<Email, Long> {
    List<Email> findAllByOrderBySendDateDesc();
}
