package br.com.fiap.mercadoexpress.messaging;

import br.com.fiap.mercadoexpress.dto.PagamentoResponse;
import br.com.fiap.mercadoexpress.model.StatusPedido;
import br.com.fiap.mercadoexpress.service.EmailService;
import br.com.fiap.mercadoexpress.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consome as respostas do Banco Tranquilo: para consumir basta informar a Queue (aula 17). */
@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoResponseListener {

    private final PedidoService pedidoService;
    private final EmailService emailService;

    @RabbitListener(queues = "${app.rabbit.sucesso-queue}")
    public void onSucesso(PagamentoResponse resp) {
        log.info("[RABBIT] Pagamento APROVADO recebido: {}", resp);
        pedidoService.aprovar(resp).ifPresent(emailService::enviarConfirmacaoPagamento);
    }

    @RabbitListener(queues = "${app.rabbit.erro-queue}")
    public void onErro(PagamentoResponse resp) {
        log.info("[RABBIT] Pagamento RECUSADO recebido: {}", resp);
        pedidoService.recusar(resp.getPedidoId(), resp.getCodigoTransacao(), resp.getMensagem(),
                StatusPedido.RECUSADO);
    }
}
