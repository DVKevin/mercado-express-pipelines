package br.com.fiap.mercadoexpress.service;

import br.com.fiap.mercadoexpress.model.Email;
import br.com.fiap.mercadoexpress.model.ItemPedido;
import br.com.fiap.mercadoexpress.model.Pedido;
import br.com.fiap.mercadoexpress.model.StatusEmail;
import br.com.fiap.mercadoexpress.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Envia o e-mail de confirmação e SEMPRE grava o registro na TB_EMAIL (controle de envios). */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;

    @Value("${spring.mail.username:}")
    private String remetente;

    public Email enviarConfirmacaoPagamento(Pedido pedido) {
        String assunto = "Mercado Express - Pagamento confirmado (pedido #" + pedido.getId() + ")";
        Email email = Email.builder()
                .pedidoId(pedido.getId())
                .emailFrom(remetente)
                .emailTo(pedido.getUsuario().getEmail())
                .subject(assunto)
                .text(montarTexto(pedido))
                .sendDate(LocalDateTime.now())
                .build();

        if (remetente == null || remetente.isBlank()) {
            email.setStatus(StatusEmail.SIMULATED);
            email.setErro("SMTP não configurado (MAIL_USERNAME vazio) — e-mail apenas registrado no BD");
            log.warn("[EMAIL] SMTP não configurado; registrando e-mail do pedido {} como SIMULATED", pedido.getId());
        } else {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(remetente);
                msg.setTo(email.getEmailTo());
                msg.setSubject(assunto);
                msg.setText(email.getText());
                mailSender.send(msg);
                email.setStatus(StatusEmail.SENT);
                log.info("[EMAIL] Confirmação do pedido {} enviada para {}", pedido.getId(), email.getEmailTo());
            } catch (MailException e) {
                email.setStatus(StatusEmail.ERROR);
                email.setErro(truncar(e.getMessage()));
                log.error("[EMAIL] Falha ao enviar e-mail do pedido {}: {}", pedido.getId(), e.getMessage());
            }
        }
        return emailRepository.save(email);
    }

    private String montarTexto(Pedido p) {
        StringBuilder sb = new StringBuilder();
        sb.append("Olá, ").append(p.getUsuario().getNome()).append("!\n\n")
          .append("O Banco Tranquilo confirmou o pagamento da sua compra no Mercado Express.\n\n")
          .append("Pedido: #").append(p.getId()).append('\n')
          .append("Data: ").append(p.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append('\n')
          .append("Transação: ").append(p.getCodigoTransacao()).append("\n\nItens:\n");
        for (ItemPedido i : p.getItens()) {
            sb.append(" - ").append(i.getQuantidade()).append(' ').append(i.getProduto().getUnidade())
              .append(" de ").append(i.getProduto().getNome())
              .append(" = R$ ").append(i.getSubtotal()).append('\n');
        }
        sb.append("\nTotal: R$ ").append(p.getValorTotal()).append("\n\nObrigado pela preferência!\nMercado Express");
        return sb.toString();
    }

    private String truncar(String s) {
        if (s == null) return null;
        return s.length() > 500 ? s.substring(0, 500) : s;
    }

    public List<Email> todos() { return emailRepository.findAllByOrderBySendDateDesc(); }
}
