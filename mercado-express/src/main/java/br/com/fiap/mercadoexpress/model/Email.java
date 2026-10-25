package br.com.fiap.mercadoexpress.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Controle de e-mails enviados aos clientes (mesma ideia da TB_EMAIL da aula 16). */
@Entity
@Table(name = "TB_EMAIL")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pedidoId;

    @Column(length = 120)
    private String emailFrom;

    @Column(nullable = false, length = 120)
    private String emailTo;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String text;

    private LocalDateTime sendDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusEmail status;

    @Column(length = 500)
    private String erro;
}
